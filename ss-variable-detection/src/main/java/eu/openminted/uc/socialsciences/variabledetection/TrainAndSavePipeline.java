package eu.openminted.uc.socialsciences.variabledetection;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.entityrecognition.NEFeatureExtractor;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.features.ngram.LuceneSkipNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.features.LuceneLemmaNGram;
import eu.openminted.uc.socialsciences.variabledetection.features.TheSozFeatures;
import eu.openminted.uc.socialsciences.variabledetection.features.WordnetFeatures;
import eu.openminted.uc.socialsciences.variabledetection.io.TextDatasetReader;
import eu.openminted.uc.socialsciences.variabledetection.resource.TheSozResource;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;

/**
 * Pipeline for training a machine learning model using the training data and saving the model.
 * The pipeline uses several textual features to train a model using
 * <a href="https://github.com/dkpro/dkpro-tc">DKPro-TC</a>.
 */
public class TrainAndSavePipeline
    extends AbstractPipeline
    implements Constants
{
    private static final String CORPUS_FILEPATH_TRAIN = "/home/local/UKP/kiaeeha/workspace/Datasets/"
            + "openminted/uc-ss/variable-detection/2017-08-22-SurveyVariables_E/train";
    private static final String COPRUS_FILEPATH_TEST = "/home/local/UKP/kiaeeha/workspace/Datasets/"
            + "openminted/uc-ss/variable-detection/2017-08-22-SurveyVariables_E/test";
    private static final String LANGUAGE_CODE = "en";
    private static final String EXPERIMENT_NAME = "AllbusVariableDetection";
    public static final File modelPath = new File("target/model");

    /**
     * Starts the experiment.
     */
    public static void main(String[] args) throws Exception
    {
        assertDkproHomeVariableIsSet();

        ParameterSpace pSpace = getParameterSpace();
        TrainAndSavePipeline experiment = new TrainAndSavePipeline();
        experiment.runTrainSaveModel(pSpace);
    }

    /**
     * Creates the the parameter space ({@link ParameterSpace}) for the experiment. The parameter
     * space describes the experiment input(s), classifier(s), feature extractor(s), and feature selector(s).
     */
    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        
        Dimension<Map<String, Object>> dimReaders = createReadersDimension();
        Dimension<List<String>> dimClassificationArgs = createClassifiersDimension();
        Dimension<TcFeatureSet> dimFeatureSets = createFeatureExtractorsDimension();

        ParameterSpace pSpace = new ParameterSpace(dimReaders,
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    private static Dimension<TcFeatureSet> createFeatureExtractorsDimension()
    {
        return Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(LuceneLemmaNGram.class,
                        LuceneLemmaNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneLemmaNGram.PARAM_NGRAM_MIN_N, 2, LuceneLemmaNGram.PARAM_NGRAM_MAX_N,
                        3),
//                TcFeatureFactory.create(LuceneCharacterNGram.class,
//                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50,
//                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 1,
//                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 3),
                // TcFeatureFactory.create(LucenePhoneticNGram.class,
                // LucenePhoneticNGram.PARAM_NGRAM_USE_TOP_K, 50,
                // LucenePhoneticNGram.PARAM_NGRAM_MIN_N, 1,
                // LucenePhoneticNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(LuceneSkipNGram.class,
                        LuceneSkipNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneSkipNGram.PARAM_NGRAM_MIN_N, 2, LuceneSkipNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(NEFeatureExtractor.class),
                TcFeatureFactory.create(WordnetFeatures.class, WordnetFeatures.PARAM_RESOURCE_NAME,
                        WordnetFeatures.WORDNET_FIELD, WordnetFeatures.PARAM_RESOURCE_LANGUAGE, "en",
                        WordnetFeatures.PARAM_NGRAM_MIN_N, 1,
                        WordnetFeatures.PARAM_NGRAM_MAX_N, 4,
                        WordnetFeatures.PARAM_NGRAM_USE_TOP_K, Integer.MAX_VALUE,
                        WordnetFeatures.PARAM_SYNONYM_FEATURE, true,
                        WordnetFeatures.PARAM_HYPERNYM_FEATURE, false),
                TcFeatureFactory.create(TheSozFeatures.class,
                        TheSozFeatures.PARAM_RESOURCE_NAME, TheSozResource.NAME,
                        TheSozFeatures.PARAM_NGRAM_MIN_N, 1,
                        TheSozFeatures.PARAM_NGRAM_MAX_N, 3)));
    }

    @SuppressWarnings("unchecked")
    private static Dimension<List<String>> createClassifiersDimension()
    {
        // We configure 3 different classifiers, which will be swept, each with a special
        // configuration.
        return Dimension.create(DIM_CLASSIFICATION_ARGS,
                // "-C": complexity, "-K": kernel
                asList(new String[] { SMO.class.getName(), "-C", "1.0", "-K",
                        PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
                asList(new String[] { NaiveBayes.class.getName(), "-K" }),
                // "W": base classifier
                asList(new String[] { Bagging.class.getName(), "-I", "2", "-W", J48.class.getName(),
                        "--", "-C", "0.5", "-M", "2" }));
    }

    private static Dimension<Map<String, Object>> createReadersDimension()
        throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TextDatasetReader.class, TextDatasetReader.PARAM_SOURCE_LOCATION,
                CORPUS_FILEPATH_TRAIN, TextDatasetReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TextDatasetReader.PARAM_PATTERNS,
                Arrays.asList(TextDatasetReader.INCLUDE_PREFIX + "**/*.txt"));
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                TextDatasetReader.class, TextDatasetReader.PARAM_SOURCE_LOCATION,
                COPRUS_FILEPATH_TEST, TextDatasetReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TextDatasetReader.PARAM_PATTERNS,
                Arrays.asList(TextDatasetReader.INCLUDE_PREFIX + "**/*.txt"));
        dimReaders.put(DIM_READER_TEST, readerTest);
        return Dimension.createBundle("readers", dimReaders);
    }

    protected void runTrainSaveModel(ParameterSpace pSpace) throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel(EXPERIMENT_NAME + "-TrainSave",
                WekaClassificationAdapter.class, modelPath);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class,
                        BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(StanfordLemmatizer.class),
                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, getClass().getResource("/stopwords/english.txt").toString()));
    }
}
