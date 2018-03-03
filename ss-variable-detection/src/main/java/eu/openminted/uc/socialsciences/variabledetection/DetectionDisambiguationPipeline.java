package eu.openminted.uc.socialsciences.variabledetection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

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
    private static final String DETECTION_MODEL_LOCATION = "../data/models/variable-detection/";
    private static final String DISAMBIGUATION_MODEL_LOCATION = "../data/models/variable-disambiguation/variable-disambiguation-model.ser";
    private static final String COPRUS_FILEPATH_TEST = "../data/datasets/Full_ALLDOCS_English.xml";
    private static final String LANGUAGE_CODE = "en";
    private static final File PREDICTION_PATH = new File("target/prediction");

    public static void main(String[] args) throws Exception
    {
        // Route logging through log4j
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");

        runPipeline(
                createReaderDescription(
                        XmlCorpusAllDocsReader.class, 
                        XmlCorpusAllDocsReader.PARAM_INCLUDE_GOLD, true,
                        XmlCorpusAllDocsReader.PARAM_SOURCE_LOCATION, COPRUS_FILEPATH_TEST, 
                        XmlCorpusAllDocsReader.PARAM_LANGUAGE, LANGUAGE_CODE),
                //Preprocessing should be the same as the one used for model training
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(StanfordLemmatizer.class),
                createEngineDescription(OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, "classpath:/stopwords/english.txt"),
                createEngineDescription(VariableMentionDetector.class,
                        VariableMentionDetector.PARAM_MODEL_LOCATION, DETECTION_MODEL_LOCATION),
                createEngineDescription(VariableMentionDisambiguator.class,
                        VariableMentionDisambiguator.PARAM_DISAMBIGUATE_ALL_MENTIONS, true,
                        VariableMentionDisambiguator.PARAM_MODEL_LOCATION, DISAMBIGUATION_MODEL_LOCATION,
                        VariableMentionDisambiguator.PARAM_VARIABLE_FILE_LOCATION, "../data/datasets/Variables_English.xml"),
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, PREDICTION_PATH,
                        XmiWriter.PARAM_OVERWRITE, true));
    }
}
