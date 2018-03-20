package eu.openminted.uc.socialsciences.variabledetection.pipelines;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

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
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.features.LuceneLemmaNGram;
import eu.openminted.uc.socialsciences.variabledetection.uima.io.XmlCorpusAllDocsReader;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;

/**
 * Pipeline for training a machine learning model using the training data and evaluating the model
 * on test data. The pipeline uses several textual features to train a model using
 * <a href="https://github.com/dkpro/dkpro-tc">DKPro-TC</a>.
 */
public class DetectionOnlyTrainAndTestPipeline
    extends AbstractPipeline
    implements Constants
{
    private static final String CORPUS_FILEPATH_TRAIN = "../data/datasets/Full_ALLDOCS_English-train.xml";
    private static final String COPRUS_FILEPATH_TEST = "../data/datasets/Full_ALLDOCS_English-test.xml";
    private static final String LANGUAGE_CODE = "en";
    private static final String EXPERIMENT_NAME = "AllbusVariableDetection";

    /**
     * Starts the experiment.
     */
    public static void main(String[] args) throws Exception
    {
        assertDkproHomeVariableIsSet();

        ParameterSpace pSpace = getParameterSpace();
        DetectionOnlyTrainAndTestPipeline experiment = new DetectionOnlyTrainAndTestPipeline();
        experiment.runTrainTest(pSpace);
    }

    /**
     * Creates the the parameter space ({@link ParameterSpace}) for the experiment. The parameter
     * space describes the experiment input(s), classifier(s), feature extractor(s), and feature
     * selector(s).
     */
    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        return new ParameterSpace(
                createReadersDimension(),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), 
                createFeatureExtractorsDimension(),
                createClassifiersDimension()
                // createFeatureSelectionDimension()
                );
    }

    private static Dimension<Map<String, Object>> createFeatureSelectionDimension()
    {
        // single-label feature selection (Weka specific options), reduces the feature set to N
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
                asList(new String[] { Ranker.class.getName(), "-N", "100" }));
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);
        return Dimension.createBundle("featureSelection", dimFeatureSelection);
    }

    private static Dimension<TcFeatureSet> createFeatureExtractorsDimension()
    {
        return Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(LuceneNGram.class,
                        LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3),
                TcFeatureFactory.create(LuceneLemmaNGram.class,
                        LuceneLemmaNGram.PARAM_NGRAM_MIN_N, 3, LuceneLemmaNGram.PARAM_NGRAM_MAX_N, 3)
//                TcFeatureFactory.create(NEFeatureExtractor.class),
//                TcFeatureFactory.create(WordnetFeatures.class, WordnetFeatures.PARAM_RESOURCE_NAME,
//                        WordnetFeatures.WORDNET_FIELD, WordnetFeatures.PARAM_RESOURCE_LANGUAGE, "en",
//                        WordnetFeatures.PARAM_NGRAM_MIN_N, 1,
//                        WordnetFeatures.PARAM_NGRAM_MAX_N, 4,
//                        WordnetFeatures.PARAM_SYNONYM_FEATURE, false,
//                        WordnetFeatures.PARAM_HYPERNYM_FEATURE, false),
//                TcFeatureFactory.create(TheSozFeatures.class,
//                        TheSozFeatures.PARAM_NGRAM_MIN_N, 1,
//                        TheSozFeatures.PARAM_NGRAM_MAX_N, 3)
                ));
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
                XmlCorpusAllDocsReader.class, 
                XmlCorpusAllDocsReader.PARAM_INCLUDE_TARGET_AND_OUTCOME, true,
                XmlCorpusAllDocsReader.PARAM_SOURCE_LOCATION, CORPUS_FILEPATH_TRAIN, 
                XmlCorpusAllDocsReader.PARAM_LANGUAGE, LANGUAGE_CODE);
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                XmlCorpusAllDocsReader.class, 
                XmlCorpusAllDocsReader.PARAM_INCLUDE_TARGET_AND_OUTCOME, true,
                XmlCorpusAllDocsReader.PARAM_SOURCE_LOCATION, COPRUS_FILEPATH_TEST, 
                XmlCorpusAllDocsReader.PARAM_LANGUAGE, LANGUAGE_CODE);
        dimReaders.put(DIM_READER_TEST, readerTest);
        return Dimension.createBundle("readers", dimReaders);
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
//                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, "classpath:/stopwords/english.txt"));
    }
}
