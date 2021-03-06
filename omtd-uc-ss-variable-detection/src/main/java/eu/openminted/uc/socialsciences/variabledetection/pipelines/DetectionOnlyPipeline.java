package eu.openminted.uc.socialsciences.variabledetection.pipelines;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.uima.VariableMentionDetector;
import eu.openminted.uc.socialsciences.variabledetection.uima.io.XmlCorpusAllDocsReader;

/**
 * Pipeline for loading a <a href="https://github.com/dkpro/dkpro-tc">DKPro-TC</a> model and applying
 * it to input data. 
 */
public class DetectionOnlyPipeline
    implements Constants
{
    private static final String COPRUS_FILEPATH_TEST = "/home/local/UKP/kiaeeha/workspace/Datasets"
            + "/openminted/uc-ss/variable-detection/detection/Full_ALLDOCS.xml";
    private static final String DETECTION_MODEL_LOCATION = "../data/models/variable-detection/";
    private static final String LANGUAGE_CODE = "en";
    public static final File PREDICTION_PATH = new File("target/prediction");

    public static void main(String[] args) throws Exception
    {
        DetectionOnlyPipeline experiment = new DetectionOnlyPipeline();
        experiment.applyStoredModel();
    }

    protected void applyStoredModel()
        throws ResourceInitializationException, UIMAException, IOException
    {
        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                XmlCorpusAllDocsReader.class, 
                XmlCorpusAllDocsReader.PARAM_SOURCE_LOCATION, COPRUS_FILEPATH_TEST, 
                XmlCorpusAllDocsReader.PARAM_LANGUAGE, LANGUAGE_CODE);
        
        SimplePipeline.runPipeline(
                readerTest,
                //Preprocessing should be the same as the one used for model training
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(StanfordLemmatizer.class),
//                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, "classpath:/stopwords/english.txt"),
                createEngineDescription(VariableMentionDetector.class,
                        VariableMentionDetector.PARAM_MODEL_LOCATION, DETECTION_MODEL_LOCATION),
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH,
                        XmiWriter.PARAM_OVERWRITE, true));
    }
}
