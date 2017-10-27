package eu.openminted.uc.socialsciences.variabledetection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.io.TextDatasetReader;

/**
 * Pipeline for loading a <a href="https://github.com/dkpro/dkpro-tc">DKPro-TC</a> model and applying
 * it to input data. 
 */
public class LoadAndApplyPipeline
    extends AbstractPipeline
    implements Constants
{
    private static final String COPRUS_FILEPATH_TEST = "/home/local/UKP/kiaeeha/workspace/Datasets/"
            + "openminted/uc-ss/variable-detection/2017-08-22-SurveyVariables_E/test";
    private static final String LANGUAGE_CODE = "en";
    public static final File modelPath = new File("target/model");
    public static final File PREDICTION_PATH = new File("target/prediction");

    /**
     * Starts the experiment.
     */
    public static void main(String[] args) throws Exception
    {
        assertDkproHomeVariableIsSet();

        LoadAndApplyPipeline experiment = new LoadAndApplyPipeline();
        experiment.applyStoredModel();
    }

    protected void applyStoredModel()
        throws ResourceInitializationException, UIMAException, IOException
    {
        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                TextDatasetReader.class, TextDatasetReader.PARAM_SOURCE_LOCATION,
                COPRUS_FILEPATH_TEST, TextDatasetReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TextDatasetReader.PARAM_PATTERNS,
                Arrays.asList(TextDatasetReader.INCLUDE_PREFIX + "**/*.txt"));
        
        SimplePipeline.runPipeline(
                readerTest,
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(StanfordLemmatizer.class),
                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, getClass().getResource("/stopwords/english.txt").toString()),
                AnalysisEngineFactory.createEngineDescription(TcAnnotator.class,
                        TcAnnotator.PARAM_TC_MODEL_LOCATION, modelPath),
                AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH,
                        XmiWriter.PARAM_OVERWRITE, true));
    }
}
