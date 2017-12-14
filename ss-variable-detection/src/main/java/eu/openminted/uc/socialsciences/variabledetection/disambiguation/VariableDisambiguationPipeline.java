package eu.openminted.uc.socialsciences.variabledetection.disambiguation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    // TODO DKPRO-HOME should be set

    public static final String TESTDATA_DIR = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/06-1 VariableCorpus_FinalEnglish/";
    public static final String VARIABLE_LIST_FILE = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/variable-detection/Variables_english_NoIntend.xml";
    public static final String PREDICTION_DIR = "target/prediction";
    public static final String OUTPUT_MODEL = "target/variable-disambiguation-model.ser";

    private LinearRegressionSimilarityMeasure classifier;
    private FeatureGeneration featureGeneration;

    public static void main(String[] args) throws Exception
    {
        new VariableDisambiguationPipeline().run();
    }

    public void run() throws Exception
    {
        classifier = loadClassifier(OUTPUT_MODEL);

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

    public static LinearRegressionSimilarityMeasure trainLinearRegression(
            VariableDisambiguationConstants.Dataset train)
        throws Exception
    {
        File trainingFile = new File(VariableDisambiguationConstants.MODELS_DIR + "/train/"
                + train.toString() + ".arff");
        LinearRegressionSimilarityMeasure classifier = new LinearRegressionSimilarityMeasure(
                trainingFile, true);
        return classifier;
    }
}
