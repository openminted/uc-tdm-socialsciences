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
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false, defaultValue = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/model.ser")
    private String modelLocation;

    public static final String PARAM_VARIABLE_FILE_LOCATION = "variableFileLocation";
    @ConfigurationParameter(name = PARAM_VARIABLE_FILE_LOCATION, mandatory = false, defaultValue = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/Variables_english_NoIntend.xml")
    private String variableFilePath;

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
        boolean found = false;
        for (VariableMention mention : JCasUtil.select(aJCas, VariableMention.class)) {
            if (found) {
                mention.setCorrect("No");
                continue;
            }
            if (mention.getCorrect().equals("Yes")) {
                String sentence = mention.getCoveredText();
                String matchId;
                try {
                    matchId = findMatchingVariable(sentence, variableMap);
                }
                catch (Exception e) {
                    throw new AnalysisEngineProcessException(e);
                }
                mention.setVariableId(matchId);
                found = true;
            }
        }
    }

    private LinearRegressionSimilarityMeasure loadClassifier(String aFilename)
        throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(aFilename));
        LinearRegressionSimilarityMeasure classifier = (LinearRegressionSimilarityMeasure) input
                .readObject();
        input.close();
        return classifier;
    }

    private String findMatchingVariable(String aSentence, Map<String, String> aVariableMap)
        throws Exception
    {
        String result = "";
        double similarity = 0;

        for (String variableId : aVariableMap.keySet()) {
            featureGeneration.generateFeatures(aSentence, aVariableMap.get(variableId));
            String fileName = Features2Arff.toArffFile(VariableDisambiguationConstants.Mode.TEMP,
                    VariableDisambiguationConstants.Dataset.TEMP, null);
            Instance instance = classifier.getInstance(new File(fileName));
            double tempSimilarity = classifier.getSimilarity(instance);
            if (tempSimilarity > similarity) {
                similarity = tempSimilarity;
                result = variableId;
            }
        }

        return result;
    }
}