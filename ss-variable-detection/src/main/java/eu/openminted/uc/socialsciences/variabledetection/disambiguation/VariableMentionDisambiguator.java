package eu.openminted.uc.socialsciences.variabledetection.disambiguation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import eu.openminted.uc.socialsciences.annotation.VariableMention;
import eu.openminted.uc.socialsciences.similarity.algorithms.ml.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.io.VariableFileReader;
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
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        for (VariableMention mention : JCasUtil.select(aJCas, VariableMention.class)) {
            if (mention.getCorrect().equals("Yes") || disambiguateAllMentions) {
                getLogger().info(
                        "Disambiguating variable candidate in [" + mention.getCoveredText() + "]");

                Match match;
                try {
                    match = findMatchingVariable(mention.getCoveredText(), variableMap);
                }
                catch (Exception e) {
                    getLogger().error("Disambiguation failed: " + e.getMessage());
                    throw new AnalysisEngineProcessException(e);
                }
                mention.setVariableId(match.id);
                mention.setScore(match.score);
                
                getLogger().info("Disambiguating suggests [" + match + "]");
            }
        }
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
    private Match findMatchingVariable(String aSentence, Map<String, String> aVariableMap)
        throws Exception
    {
        String matchingVarId = "";
        double similarity = 0;

        for (String varId : aVariableMap.keySet()) {
            featureGeneration.generateFeatures(aSentence, aVariableMap.get(varId));
            String fileName = Features2Arff.toArffFile(VariableDisambiguationConstants.Mode.TEMP,
                    VariableDisambiguationConstants.Dataset.TEMP, null);

            Instance instance = classifier.getInstance(new File(fileName));

            double tempSimilarity = classifier.getSimilarity(instance);

            getLogger().info(new Match(varId, tempSimilarity));
            
            if (tempSimilarity > similarity) {
                similarity = tempSimilarity;
                matchingVarId = varId;
            }
        }

        return new Match(matchingVarId, similarity);
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
