package eu.openminted.uc.socialsciences.variabledetection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.detection.VariableMentionDetector;
import eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableMentionDisambiguator;
import eu.openminted.uc.socialsciences.variabledetection.io.XmlCorpusAllDocsReader;

public class DetectionDisambiguationPipeline
{
    private static final String DETECTION_MODEL_LOCATION = "/home/local/UKP/kiaeeha/git/uc-tdm-socialsciences/data/models/variable-detection/";
    private static final String DISAMBIGUATION_MODEL_LOCATION = "/home/local/UKP/kiaeeha/git/uc-tdm-socialsciences/data/models/variable-disambiguation/variable-disambiguation-model.ser";
    private static final String COPRUS_FILEPATH_TEST = "/home/local/UKP/kiaeeha/workspace/Datasets"
            + "/openminted/uc-ss/variable-detection/detection/Full_ALLDOCS.xml";
    private static final String LANGUAGE_CODE = "en";
    public static final File PREDICTION_PATH = new File("target/prediction");

    public static void main(String[] args) throws Exception
    {
        DetectionDisambiguationPipeline experiment = new DetectionDisambiguationPipeline();
        experiment.run();
    }

    protected void run()
        throws ResourceInitializationException, UIMAException, IOException
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XmlCorpusAllDocsReader.class, XmlCorpusAllDocsReader.PARAM_SOURCE_LOCATION,
                COPRUS_FILEPATH_TEST, XmlCorpusAllDocsReader.PARAM_LANGUAGE, LANGUAGE_CODE);
        
        SimplePipeline.runPipeline(
                reader,
                //Preprocessing should be the same as the one used for model training
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(StanfordLemmatizer.class),
                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, getClass().getResource("/stopwords/english.txt").toString()),
                createEngineDescription(VariableMentionDetector.class,
                        VariableMentionDetector.PARAM_MODEL_LOCATION, DETECTION_MODEL_LOCATION),
                createEngineDescription(VariableMentionDisambiguator.class,
                        VariableMentionDisambiguator.PARAM_MODEL_LOCATION, DISAMBIGUATION_MODEL_LOCATION),
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH,
                        XmiWriter.PARAM_OVERWRITE, true));
    }
}
