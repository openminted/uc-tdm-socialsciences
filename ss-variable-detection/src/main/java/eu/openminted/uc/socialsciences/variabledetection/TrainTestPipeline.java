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
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.entityrecognition.NEFeatureExtractor;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.features.ngram.LuceneSkipNGram;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.features.LuceneLemmaNGram;
import eu.openminted.uc.socialsciences.variabledetection.features.TheSozFeatures;
import eu.openminted.uc.socialsciences.variabledetection.features.WordnetFeatures;
import eu.openminted.uc.socialsciences.variabledetection.io.TextDatasetReader;
import eu.openminted.uc.socialsciences.variabledetection.resource.TheSozResource;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;

public class TrainTestPipeline
    implements Constants
{
    private static final String CORPUS_FILEPATH_TRAIN = "/home/local/UKP/kiaeeha/workspace/Datasets/"
            + "openminted/uc-ss/variable-detection/2017-08-22-SurveyVariables_E/train";
    private static final String COPRUS_FILEPATH_TEST = "/home/local/UKP/kiaeeha/workspace/Datasets/"
            + "openminted/uc-ss/variable-detection/2017-08-22-SurveyVariables_E/test";
    private static final String LANGUAGE_CODE = "en";
    private static final String EXPERIMENT_NAME = "AllbusVariableDetection";

    /**
     * Starts the experiment.
     */
    public static void main(String[] args) throws Exception
    {
        setDkproHome(TrainTestPipeline.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace();
        TrainTestPipeline experiment = new TrainTestPipeline();
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
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

        // We configure 3 different classifiers, which will be swept, each with a special
        // configuration.
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                // "-C": complexity, "-K": kernel
                asList(new String[] { SMO.class.getName(), "-C", "1.0", "-K",
                        PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
                asList(new String[] { NaiveBayes.class.getName(), "-K" }),
                // "W": base classifier
                asList(new String[] { Bagging.class.getName(), "-I", "2", "-W", J48.class.getName(),
                        "--", "-C", "0.5", "-M", "2" }));

        // We configure 1 set of feature extractors, consisting of 1 extractor
        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(LuceneLemmaNGram.class,
                        LuceneLemmaNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneLemmaNGram.PARAM_NGRAM_MIN_N, 2, LuceneLemmaNGram.PARAM_NGRAM_MAX_N,
                        3),
                TcFeatureFactory.create(LuceneCharacterNGram.class,
                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50,
                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 1,
                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 3),
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
                        WordnetFeatures.PARAM_SYNONYM_FEATURE, true,
                        WordnetFeatures.PARAM_HYPERNYM_FEATURE, false),
                TcFeatureFactory.create(TheSozFeatures.class,
                        TheSozFeatures.PARAM_RESOURCE_NAME, TheSozResource.NAME,
                        TheSozFeatures.PARAM_NGRAM_MIN_N, 1,
                        TheSozFeatures.PARAM_NGRAM_MAX_N, 3)));

        // single-label feature selection (Weka specific options), reduces the feature set to 10
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
                asList(new String[] { Ranker.class.getName(), "-N", "100" }));
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
                dimClassificationArgs,
                Dimension.createBundle("featureSelection", dimFeatureSelection));

        return pSpace;
    }

    protected void runTrainTest(ParameterSpace pSpace) throws Exception
    {
        ExperimentTrainTest batch = new ExperimentTrainTest(EXPERIMENT_NAME,
                WekaClassificationAdapter.class);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.addReport(BatchTrainTestReport.class);

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
                createEngineDescription(OpenNlpNamedEntityRecognizer.class));
    }

    /**
     * Set the DKPRO_HOME environment variable to some folder in "target".
     * 
     * If DKPRO_HOME is already set, nothing is done (in order not to override already working
     * environments).
     * 
     * @param experimentName
     *            name of the experiment (will be used as folder name, no slashes/backslashes,
     *            better avoid spaces)
     * @return True if DKPRO_HOME was correctly set and false if nothing was done.
     */
    public static boolean setDkproHome(String experimentName)
    {
        String dkproHome = "DKPRO_HOME";
        Map<String, String> env = System.getenv();
        if (!env.containsKey(dkproHome)) {
            System.out.println("DKPRO_HOME not set.");

            File folder = new File("target/results/" + experimentName);
            folder.mkdirs();

            System.setProperty(dkproHome, folder.getPath());
            System.out.println("Setting DKPRO_HOME to: " + folder.getPath());

            return true;
        }
        else {
            System.out.println("DKPRO_HOME already set to: " + env.get(dkproHome));
            System.out.println("Keeping those settings.");

            return false;
        }
    }
}
