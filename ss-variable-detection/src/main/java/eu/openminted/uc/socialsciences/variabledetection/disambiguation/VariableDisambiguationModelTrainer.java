package eu.openminted.uc.socialsciences.variabledetection.disambiguation;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import eu.openminted.uc.socialsciences.similarity.algorithms.ml.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationConstants.Dataset;
import eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationConstants.Mode;
import eu.openminted.uc.socialsciences.variabledetection.features.CharacterNGramIdfValuesGenerator;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.features.WordIdfValuesGenerator;
import eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff;

public class VariableDisambiguationModelTrainer
{
    public static final String DATASET_DIR = "classpath:/datasets/semeval-2012";
    public static final String GOLDSTANDARD_DIR = "classpath:/goldstandards/semeval-2012";

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.err.println("Provide path to save model as the first argument!");
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
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(aFilename))) {
            output.writeObject(classifier);
        }
    }

    private void generateFeaturesForTrainingData() throws Exception, IOException
    {
        Mode mode = Mode.TRAIN;
        List<Dataset> datasets = asList(Dataset.MSRpar, Dataset.MSRvid, Dataset.SMTeuroparl);
        
        for (int n : FeatureGeneration.CHAR_NGRAMS_N) {
            CharacterNGramIdfValuesGenerator.computeIdfScores(Dataset.ALL, mode, datasets, n);
        }
        
        WordIdfValuesGenerator.computeIdfScores(Dataset.ALL, mode, datasets);
        
        // Generate the features for training data
        FeatureGeneration.generateFeatures(Dataset.ALL, datasets, mode);

//        // Concatenate all training data
//        FeatureGeneration.combineFeatureSets(Mode.TRAIN,
//                Dataset.ALL, Dataset.MSRpar, Dataset.MSRvid, Dataset.SMTeuroparl);

        // Package features in arff files
        Features2Arff.toArffFile(Mode.TRAIN, Dataset.ALL);
    }

    public static LinearRegressionSimilarityMeasure trainClassifier() throws Exception
    {
        File trainingFile = new File(VariableDisambiguationConstants.MODELS_DIR + "/train/"
                + Dataset.ALL.toString() + ".arff");
        LinearRegressionSimilarityMeasure classifier = new LinearRegressionSimilarityMeasure(
                trainingFile, true);
        return classifier;
    }
}
