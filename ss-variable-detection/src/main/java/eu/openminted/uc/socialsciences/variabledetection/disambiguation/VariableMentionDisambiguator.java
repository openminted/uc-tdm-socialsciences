package eu.openminted.uc.socialsciences.variabledetection.disambiguation;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
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
import eu.openminted.uc.socialsciences.similarity.algorithms.ml.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.io.VariableFileReader;
import eu.openminted.uc.socialsciences.variabledetection.type.GoldVariableMention;
import eu.openminted.uc.socialsciences.variabledetection.type.VariableMention;
import eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff;
import weka.core.Instance;

public class VariableMentionDisambiguator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION)
    private String modelLocation;

    public static final String PARAM_VARIABLE_FILE_LOCATION = "variableFileLocation";
    @ConfigurationParameter(name = PARAM_VARIABLE_FILE_LOCATION)
    private String variableFilePath;

    /**
     * If set to {@code false}, only {@code correct="Yes"} mentions are disambiguated, otherwise
     * all mentions are disambiguated.
     */
    public static final String PARAM_DISAMBIGUATE_ALL_MENTIONS = "disambiguateAllMentions";
    @ConfigurationParameter(name = PARAM_DISAMBIGUATE_ALL_MENTIONS, defaultValue = "false")
    private boolean disambiguateAllMentions;
    
    private LinearRegressionSimilarityMeasure classifier;
    private FeatureGeneration featureGeneration;
    private Map<String, String> variableMap;
    
    private int total = 0;
    private int[] matchAtRank = new int[100];
    private int[] cumulativeMatchAtRank = new int[100];

    private PrintWriter logwriter;
    
    @Override
    public void initialize(final UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            classifier = loadClassifier(modelLocation);
            variableMap = VariableFileReader.getVariables(variableFilePath);
            featureGeneration = new FeatureGeneration();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
        try {
            logwriter = new PrintWriter(new FileWriter("target/log.csv"));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        DocumentMetaData meta = DocumentMetaData.get(aJCas);
        
        for (VariableMention mention : select(aJCas, VariableMention.class)) {
            if (mention.getCorrect().equals("Yes") || disambiguateAllMentions) {
                getLogger().info(
                        "Disambiguating variable candidate in [" + mention.getCoveredText() + "]");

                List<Match> matches;
                try {
                    matches = getMatches(mention.getCoveredText(), variableMap);
                    Collections.sort(matches, (a, b) -> {
                        return Double.compare(b.score, a.score);
                    });
                }
                catch (Exception e) {
                    getLogger().error("Disambiguation failed: " + e.getMessage());
                    throw new AnalysisEngineProcessException(e);
                }
                
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
                
                total++;
                
                mention.setVariableId(matches.get(0).id);
                mention.setScore(matches.get(0).score);
                
                int r = 1;
                for (Match m : matches) {
                    logwriter.printf("%s;%d;%d;%s;%d;%f;%d%n", meta.getDocumentId(),
                            "Yes".equals(mention.getCorrect()) ? 1 : 0,
                            goldVariables.isEmpty() ? 0 : 1, m.id, r, m.score,
                            goldVariables.contains(m.id) ? 1 : 0);
                    r++;
                }
                logwriter.flush();
                
                getLogger().info("Best match [" + matches.get(0) + "]");
            }
        }
    }
    
    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        for (int i = 0; i < matchAtRank.length; i++) {
            getLogger().info("Matches at " + (i + 1) + ": " + matchAtRank[i] + " - "
                    + cumulativeMatchAtRank[i]);
        }
        
        logwriter.close();
    }

    private static LinearRegressionSimilarityMeasure loadClassifier(String aFilename)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        LinearRegressionSimilarityMeasure classifier;
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(aFilename))) {
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
        
        int i = 0;
        for (String varId : aVariableMap.keySet()) {
            featureGeneration.generateFeaturesAsFiles(aSentence, aVariableMap.get(varId));

            String fileName = Features2Arff.toArffFile(VariableDisambiguationConstants.Mode.TEMP,
                    VariableDisambiguationConstants.Dataset.TEMP, null);

            Instance instance = classifier.getInstance(new File(fileName));

//            Instance instance2 = featureGeneration.generateFeatures(aSentence,
//                    aVariableMap.get(varId));
//            if (classifier.isUseLogFilter()) {
//                Filter logFilter = new LogFilter();
//                logFilter.input(instance2);
//                logFilter.batchFinished();
//                instance = logFilter.output();
//            }
            
            //System.out.println("Masoud: " + instance);
//            System.out.println("REC:    " + instance2);
            if (i % 25 == 0) {
                System.out.print(".");
            }
            
            matches.add(new Match(varId, classifier.getSimilarity(instance)));
            i++;
         }
        System.out.println();

        return matches;
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
