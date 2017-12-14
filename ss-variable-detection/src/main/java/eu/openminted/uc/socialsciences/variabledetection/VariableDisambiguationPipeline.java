package eu.openminted.uc.socialsciences.variabledetection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import eu.openminted.uc.socialsciences.annotation.VariableMention;
import eu.openminted.uc.socialsciences.similarity.algorithms.ml.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.io.VariableFileReader;
import eu.openminted.uc.socialsciences.variabledetection.io.XmlCorpusReader;
import eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff;
import weka.core.Instance;

public class VariableDisambiguationPipeline
{
    public enum Mode
    {
        TRAIN, TEST, TEMP
    }

    public enum Dataset
    {
        ALL, MSRpar, MSRvid, SMTeuroparl, TEMP
    }

    public enum EvaluationMetric
    {
        PearsonAll, PearsonMean
    }

    public static final String DATASET_DIR = "classpath:/datasets/semeval-2012";
    public static final String GOLDSTANDARD_DIR = "classpath:/goldstandards/semeval-2012";
    public static final String TESTDATA_DIR = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/06-1 VariableCorpus_FinalEnglish/";
    public static final String VARIABLE_LIST_FILE = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/Variables_english_NoIntend.xml";
    public static final String PREDICTION_DIR = "target/prediction";

    public static final String FEATURES_DIR = "target/features";
    public static final String MODELS_DIR = "target/models";
    public static final String UTILS_DIR = "target/utils";
    public static final String OUTPUT_DIR = "target/output";

    public static final String[] variables = new String[] { "variable 1", "variable 2" };
    private LinearRegressionSimilarityMeasure classifier;
    private FeatureGeneration featureGeneration;

    public static void main(String[] args) throws Exception
    {
        new VariableDisambiguationPipeline().run();
    }
    
    public void run() throws Exception
    {
        // Generate the features for training data
        FeatureGeneration.generateFeatures(Dataset.MSRpar, Mode.TRAIN);
        FeatureGeneration.generateFeatures(Dataset.MSRvid, Mode.TRAIN);
        FeatureGeneration.generateFeatures(Dataset.SMTeuroparl, Mode.TRAIN);

        // Generate the features for test data

        // Concatenate all training data
        FeatureGeneration.combineFeatureSets(Mode.TRAIN, Dataset.ALL, Dataset.MSRpar,
                Dataset.MSRvid, Dataset.SMTeuroparl);

        // Package features in arff files
        Features2Arff.toArffFile(Mode.TRAIN, Dataset.ALL);

        // Run the classifer
        classifier = trainLinearRegression(Dataset.ALL);
        
        Map<String, String> variableMap = VariableFileReader.getVariables(VARIABLE_LIST_FILE);
        featureGeneration = new FeatureGeneration();

        CollectionReaderDescription reader = createReaderDescription(XmlCorpusReader.class,
                XmlCorpusReader.PARAM_PATTERNS, TESTDATA_DIR + "**/*.xml");
        for (JCas jcas : SimplePipeline.iteratePipeline(reader)) {
            boolean found = false;
            for (VariableMention mention : JCasUtil.select(jcas, VariableMention.class)) {
                if (found) {
                    mention.setCorrect("No");
                    continue;
                }
                if (mention.getCorrect().equals("Yes")) {
                    String sentence = mention.getCoveredText();
                    String matchId = findMatchingVariable(sentence, variableMap);
                    mention.setVariableId(matchId);
                    found = true;
                }
            }
            
            AnalysisEngineDescription writer = createEngineDescription(XmiWriter.class,
                    XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_DIR + "/",
                    XmiWriter.PARAM_OVERWRITE, true);
            AnalysisEngine engine = createEngine(writer);
            engine.process(jcas);
        }
    }
    
    private String findMatchingVariable(String aSentence, Map<String, String> aVariableMap) throws Exception
    {
        String result = "";
        double similarity = 0;
        
        for (String variableId : aVariableMap.keySet()) {
            featureGeneration.generateFeatures(aSentence, aVariableMap.get(variableId));
            String fileName = Features2Arff.toArffFile(Mode.TEMP, Dataset.TEMP, null);
            Instance instance = classifier.getInstance(new File(fileName));
            double tempSimilarity = classifier.getSimilarity(instance);
            if (tempSimilarity > similarity) {
                similarity = tempSimilarity;
                result = variableId;
            }
        }
        
        return result;
    }
    
    public static LinearRegressionSimilarityMeasure trainLinearRegression(Dataset train) throws Exception
    {
        File trainingFile = new File(MODELS_DIR + "/train/" + train.toString() + ".arff");
        LinearRegressionSimilarityMeasure classifier = new LinearRegressionSimilarityMeasure(
                trainingFile, true);
        return classifier;
    }
}
