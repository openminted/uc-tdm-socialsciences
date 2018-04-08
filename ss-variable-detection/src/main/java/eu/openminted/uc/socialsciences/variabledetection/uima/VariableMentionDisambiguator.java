package eu.openminted.uc.socialsciences.variabledetection.uima;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.similarity.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.type.GoldVariableMention;
import eu.openminted.uc.socialsciences.variabledetection.type.VariableMention;
import eu.openminted.uc.socialsciences.variabledetection.uima.io.VariableFileReader;
import weka.core.Instance;

public class VariableMentionDisambiguator
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;
    
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    public static final String PARAM_VARIABLE_FILE_LOCATION = "variableFileLocation";
    @ConfigurationParameter(name = PARAM_VARIABLE_FILE_LOCATION)
    private String variableFilePath;
    private Map<String, String> variableMap;

    /**
     * If set to {@code false}, only {@code correct="Yes"} mentions are disambiguated, otherwise
     * all mentions are disambiguated.
     */
    public static final String PARAM_DISAMBIGUATE_ALL_MENTIONS = "disambiguateAllMentions";
    @ConfigurationParameter(name = PARAM_DISAMBIGUATE_ALL_MENTIONS, defaultValue = "true")
    private boolean disambiguateAllMentions;
    
    public static final String PARAM_WRITE_LOG = "writeLog";
    @ConfigurationParameter(name = PARAM_WRITE_LOG, defaultValue = "false")
    private boolean writeLog;
    
    // The scores returned from the model trained on the SemEval data should be in the range
    // between 0 and 5 - so 2.5 is in the middle.
    /**
     * Minimum similarity score to a variable require to count as a match (0-5).
     */
    public static final String PARAM_SCORE_THRESHOLD = "scoreThreshold";
    @ConfigurationParameter(name = PARAM_SCORE_THRESHOLD, defaultValue = "2.5")
    private double scoreThreshold;

    /**
     * Maximum number of variables linked.
     */
    public static final String PARAM_MAX_MENTIONS = "maxMentions";
    @ConfigurationParameter(name = PARAM_MAX_MENTIONS, defaultValue = "3")
    private int maxMentions;
    
    private int[] matchAtRank = new int[100];
    private int[] cumulativeMatchAtRank = new int[100];

    private PrintWriter logwriter;
    
    private ModelProviderBase<Model> modelProvider;
    
    @Override
    public void initialize(final UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<Model>(this, "variable-detection", "disambiguation")
        {
//            {
//                setContextObject(FlexTagPosTagger.this);
//
//                setDefault(ARTIFACT_ID, "${groupId}.flextag-model-${language}-${variant}");
//                setDefault(LOCATION,
//                        "classpath:/${package}/lib/tagger-${language}-${variant}.properties");
//
//                setOverride(LOCATION, modelLocation);
//                setOverride(LANGUAGE, language);
//                setOverride(VARIANT, variant);
//            }

            @Override
            protected Model produceResource(URL aUrl)
                throws IOException
            {
                try {
                    Model model = new Model();
                    model.classifier = loadClassifier(aUrl.toString()
                            + "/variable-disambiguation/variable-disambiguation-model.ser");
                    model.featureGeneration = new FeatureGeneration(aUrl.toString());

                    return model;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };        
        
        try {
            variableMap = VariableFileReader.getVariables(variableFilePath);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        
        if (writeLog) {
            try {
                logwriter = new PrintWriter(new FileWriter("target/log.csv"));
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        DocumentMetaData meta = DocumentMetaData.get(aJCas);
        
        modelProvider.configure(aJCas.getCas());
        
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<VariableMention> mentions = selectCovered(VariableMention.class, sentence);
            VariableMention mention = mentions.isEmpty() ? null : mentions.get(0);
            
            // Check if we should disambiguate here
            if (!disambiguateAllMentions
                    || (mention != null && !"Yes".equals(mention.getCorrect()))) {
                continue;
            }
            
            getLogger().info(
                    "Disambiguating variable candidate in [" + sentence.getCoveredText() + "]");

            List<Match> matches;
            try {
                matches = getMatches(sentence.getCoveredText(), variableMap);
                Collections.sort(matches, (a, b) -> {
                    return Double.compare(b.score, a.score);
                });
            }
            catch (Exception e) {
                getLogger().error("Disambiguation failed: " + e.getMessage());
                throw new AnalysisEngineProcessException(e);
            }
            
            // Add disambiguated mentions to the CAS
            int matchCount = 0;
            for (Match m : matches) {
                if (matchCount >= maxMentions) {
                    break;
                }
                
                if (m.score < scoreThreshold) {
                    continue;
                }
                
                if (mention == null) {
                    mention = new VariableMention(aJCas, sentence.getBegin(), sentence.getEnd());
                    mention.addToIndexes();
                }
                
                mention.setVariableId(m.id);
                mention.setScore(m.score);
                
                matchCount++;
                
                mention = null;
            }
            
            
            // If the CAS contains GoldVariableMentions, then log matches.
            Set<String> goldVariables = new HashSet<>();
            for (GoldVariableMention gold : select(aJCas, GoldVariableMention.class)) {
                goldVariables.add(gold.getVariableId());
                
                Match match = matches.stream().filter(m -> gold.getVariableId().equals(m.id))
                        .findFirst().orElse(null);
                if (match == null) {
                    getLogger().warn("Variable ID not found in list [" + gold.getVariableId()
                            + "] - SKIPPING");
                    continue;
                }
                
                int rank = matches.indexOf(match);
                if (rank < matchAtRank.length) {
                    matchAtRank[rank] ++;
                }
                
                getLogger().info("Gold " + gold.getVariableId() + " at rank " + (rank + 1)
                        + " with score " + match.score);
                
                for (int i = 0; i < cumulativeMatchAtRank.length; i++) {
                    if (rank <= i) {
                        cumulativeMatchAtRank[i] ++;
                    }
                }
            }
            
            if (writeLog) {
                int r = 1;
                for (Match m : matches) {
                    logwriter.printf("%s;%d;%s;%d;%f;%d%n", meta.getDocumentId(),
                            goldVariables.isEmpty() ? 0 : 1, m.id, r, m.score,
                            goldVariables.contains(m.id) ? 1 : 0);
                    r++;
                }
                logwriter.flush();
            }
        }
    }
    
    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        if (logwriter != null) {
            for (int i = 0; i < matchAtRank.length; i++) {
                getLogger().info("Matches at " + (i + 1) + ": " + matchAtRank[i] + " - "
                        + cumulativeMatchAtRank[i]);
            }
            
            logwriter.close();
        }
    }

    private static LinearRegressionSimilarityMeasure loadClassifier(String aFilename)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        LinearRegressionSimilarityMeasure classifier;
        try (ObjectInputStream input = new ObjectInputStream(
                ResourceUtils.resolveLocation(aFilename).openStream())) {
            classifier = (LinearRegressionSimilarityMeasure) input.readObject();
        }
        return classifier;
    }

    /**
     * Finds the matching variable with the highest score.
     */
    private List<Match> getMatches(String aSentence, Map<String, String> aVariableMap)
        throws Exception
    {
        List<Match> matches = new ArrayList<>();
        
        Model model = modelProvider.getResource();
        
        long t = System.currentTimeMillis();
        int i = 0;
        for (String varId : aVariableMap.keySet()) {
//            featureGeneration.generateFeaturesAsFiles(aSentence, aVariableMap.get(varId));
//            String fileName = Features2Arff.toArffFile(VariableDisambiguationConstants.Mode.TEMP,
//                    VariableDisambiguationConstants.Dataset.TEMP, null);
//            Instance instance = classifier.getInstance(new File(fileName));

            Instance instance = model.featureGeneration.generateFeatures(aSentence,
                    aVariableMap.get(varId), model.classifier.isUseLogFilter());
            
//            if (i % 25 == 0) {
//                System.out.print(".");
//            }
            
            double similarity = model.classifier.getSimilarity(instance);
            
            matches.add(new Match(varId, similarity));
            i++;
        }
        t = System.currentTimeMillis() - t;
        getLogger().debug(String.format(" %d (%d avg per item)%n", t, t / aVariableMap.size()));

        return matches;
    }
    
    private static class Model
    {
        private LinearRegressionSimilarityMeasure classifier;
        private FeatureGeneration featureGeneration;
    }
    
    private static class Match
    {
        final String id;
        final double score;
        
        public Match(String aId, double aScore)
        {
            super();
            id = aId;
            score = aScore;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("Match [id=");
            builder.append(id);
            builder.append(", score=");
            builder.append(score);
            builder.append("]");
            return builder.toString();
        }
    }
}
