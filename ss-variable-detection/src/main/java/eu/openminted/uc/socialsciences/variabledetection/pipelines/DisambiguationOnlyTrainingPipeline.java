package eu.openminted.uc.socialsciences.variabledetection.pipelines;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import eu.openminted.uc.socialsciences.variabledetection.features.CharacterNGramIdfValuesGenerator;
import eu.openminted.uc.socialsciences.variabledetection.features.FeatureGeneration;
import eu.openminted.uc.socialsciences.variabledetection.features.WordIdfValuesGenerator;
import eu.openminted.uc.socialsciences.variabledetection.pipelines.VariableDisambiguationConstants.Dataset;
import eu.openminted.uc.socialsciences.variabledetection.pipelines.VariableDisambiguationConstants.Mode;
import eu.openminted.uc.socialsciences.variabledetection.similarity.LinearRegressionSimilarityMeasure;
import eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff;

/**
 * Trains a text similarity model using the SemEval 2012 Datasets included in DKPro Similarity
 * and writes the results to the target folder specified on the command line.
 */
public class DisambiguationOnlyTrainingPipeline
{
    public static final String DATASET_DIR = "classpath:/datasets/semeval-2012";
    public static final String GOLDSTANDARD_DIR = "classpath:/goldstandards/semeval-2012";

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.err.println("Provide path to save model as the first argument!");
            System.exit(1);
        }
        
        new DisambiguationOnlyTrainingPipeline().run(args[0]);
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
