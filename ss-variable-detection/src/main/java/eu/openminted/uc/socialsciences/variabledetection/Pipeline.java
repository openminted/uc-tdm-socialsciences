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
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.io.CsvReader;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

public class Pipeline
    implements Constants
{
    private static final String CORPUS_FILEPATH_TRAIN = "src/main/resources/data/train";
    private static final String COPRUS_FILEPATH_TEST = "src/main/resources/data/test";
    private static final String LANGUAGE_CODE = "en";
    private static final String EXPERIMENT_NAME = "AllbusVariableDetection";
    
    /**
     * Starts the experiment.
     */
    public static void main(String[] args)
        throws Exception
    {
        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        setDkproHome(Pipeline.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace();
        Pipeline experiment = new Pipeline();
        experiment.runTrainTest(pSpace);
    }
    
    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
        throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        
        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                CsvReader.class,
                CsvReader.PARAM_SOURCE_LOCATION, CORPUS_FILEPATH_TRAIN,
                CsvReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                CsvReader.PARAM_PATTERNS,
                Arrays.asList(CsvReader.INCLUDE_PREFIX + "*/*.csv"));
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        
        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                CsvReader.class,
                CsvReader.PARAM_SOURCE_LOCATION, COPRUS_FILEPATH_TEST,
                CsvReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                CsvReader.PARAM_PATTERNS,
                Arrays.asList(CsvReader.INCLUDE_PREFIX + "*/*.csv"));
        dimReaders.put(DIM_READER_TEST, readerTest);

     // We configure 3 different classifiers, which will be swept, each with a special
        // configuration.
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                // "-C": complexity, "-K": kernel
                asList(new String[] { SMO.class.getName(), "-C", "1.0", "-K",
                        PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
                // "-I": number of trees
                asList(new String[] { RandomForest.class.getName(), "-I", "5" }),
                // "W": base classifier
                asList(new String[] { Bagging.class.getName(), "-I", "2", "-W", J48.class.getName(),
                        "--", "-C", "0.5", "-M", "2" }));
        
        // We configure 1 set of feature extractors, consisting of 1 extractor
        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,                
                new TcFeatureSet(TcFeatureFactory.create(LuceneNGram.class,
                        LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50, LuceneNGram.PARAM_NGRAM_MIN_N, 1,
                        LuceneNGram.PARAM_NGRAM_MAX_N, 3)));
        
     // single-label feature selection (Weka specific options), reduces the feature set to 10
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
                asList(new String[] { Ranker.class.getName(), "-N", "10" }));
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
    
    protected void runTrainTest(ParameterSpace pSpace)
            throws Exception
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
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class,
                BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
    
    /**
     * Set the DKPRO_HOME environment variable to some folder in "target". This is mainly used to
     * ensure that demo experiments run even if people have not set DKPRO_HOME before.
     * 
     * If DKPRO_HOME is already set, nothing is done (in order not to override already working
     * environments).
     * 
     * It is highly recommended not to use that anywhere else than in the demo experiments, as
     * DKPRO_HOME is usually also used to store other data required for (real) experiments.
     * 
     * @param experimentName
     *            name of the experiment (will be used as folder name, no slashes/backslashes,
     *            better avoid spaces)
     * @return True if DKPRO_HOME was correctly set and false if nothing was done.
     */
    public static boolean setDkproHome(String experimentName) {
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
