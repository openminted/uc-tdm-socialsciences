package eu.openminted.uc.socialsciences.variabledetection.disambiguation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import eu.openminted.uc.socialsciences.similarity.algorithms.ml.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff;

public class VariableDisambiguationModelTrainer
{
    // TODO DKPRO-HOME should be set
    public static final String DATASET_DIR = "classpath:/datasets/semeval-2012";
    public static final String GOLDSTANDARD_DIR = "classpath:/goldstandards/semeval-2012";

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.err.println("Provide path to save model as the first argument!");
            System.exit(1);
        }
        if (System.getenv("DKPRO_HOME") == null) {
            System.err.println("Set the envronment variable [DKPRO_HOME]!");
            System.exit(1);
        }
        
        new VariableDisambiguationModelTrainer().run(args[0]);
    }

    public void run(String modelFileName) throws Exception
    {
        generateFeaturesForTrainingData();
        LinearRegressionSimilarityMeasure classifier = trainClassifier();
        saveClassifier(classifier, modelFileName);
    }

    private void saveClassifier(LinearRegressionSimilarityMeasure classifier, String aFilename)
        throws Exception, IOException, FileNotFoundException
    {
        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(aFilename));
        output.writeObject(classifier);
        output.close();
    }

    private void generateFeaturesForTrainingData() throws Exception, IOException
    {
        // Generate the features for training data
        FeatureGeneration.generateFeatures(VariableDisambiguationConstants.Dataset.MSRpar,
                VariableDisambiguationConstants.Mode.TRAIN);
        FeatureGeneration.generateFeatures(VariableDisambiguationConstants.Dataset.MSRvid,
                VariableDisambiguationConstants.Mode.TRAIN);
        FeatureGeneration.generateFeatures(VariableDisambiguationConstants.Dataset.SMTeuroparl,
                VariableDisambiguationConstants.Mode.TRAIN);

        // Concatenate all training data
        FeatureGeneration.combineFeatureSets(VariableDisambiguationConstants.Mode.TRAIN,
                VariableDisambiguationConstants.Dataset.ALL,
                VariableDisambiguationConstants.Dataset.MSRpar,
                VariableDisambiguationConstants.Dataset.MSRvid,
                VariableDisambiguationConstants.Dataset.SMTeuroparl);

        // Package features in arff files
        Features2Arff.toArffFile(VariableDisambiguationConstants.Mode.TRAIN,
                VariableDisambiguationConstants.Dataset.ALL);
    }

    public static LinearRegressionSimilarityMeasure trainClassifier() throws Exception
    {
        File trainingFile = new File(VariableDisambiguationConstants.MODELS_DIR + "/train/"
                + VariableDisambiguationConstants.Dataset.ALL.toString() + ".arff");
        LinearRegressionSimilarityMeasure classifier = new LinearRegressionSimilarityMeasure(
                trainingFile, true);
        return classifier;
    }
}
