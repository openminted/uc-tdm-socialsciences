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
    private LinearRegressionSimilarityMeasure classifier;
    private FeatureGeneration featureGeneration;
    private String modelPath;
    private String predictionDirectory;
    private String inputDirectory;
    private String variableFilePath;

    public static void main(String[] args) throws Exception
    {
        VariableDisambiguationPipeline pipeline = new VariableDisambiguationPipeline();
        pipeline.setModelPath(args[0]);
        pipeline.setPredictionDirectory(args[1]);
        pipeline.setInputDirectory(args[2]);
        pipeline.setVariableFilePath(args[3]);
        pipeline.run();
    }

    public void run() throws Exception
    {
        classifier = loadClassifier(modelPath);

        Map<String, String> variableMap = VariableFileReader.getVariables(variableFilePath);
        featureGeneration = new FeatureGeneration();

        CollectionReaderDescription reader = createReaderDescription(XmlCorpusReader.class,
                XmlCorpusReader.PARAM_PATTERNS, inputDirectory + "**/*.xml");
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
                    XmiWriter.PARAM_TARGET_LOCATION, predictionDirectory + "/",
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

    public String getModelPath()
    {
        return modelPath;
    }

    public void setModelPath(String modelPath)
    {
        this.modelPath = modelPath;
    }

    public String getPredictionDirectory()
    {
        return predictionDirectory;
    }

    public void setPredictionDirectory(String predictionDirectory)
    {
        this.predictionDirectory = predictionDirectory;
    }

    public String getInputDirectory()
    {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory)
    {
        this.inputDirectory = inputDirectory;
    }

    public String getVariableFilePath()
    {
        return variableFilePath;
    }

    public void setVariableFilePath(String variableFilePath)
    {
        this.variableFilePath = variableFilePath;
    }
}
