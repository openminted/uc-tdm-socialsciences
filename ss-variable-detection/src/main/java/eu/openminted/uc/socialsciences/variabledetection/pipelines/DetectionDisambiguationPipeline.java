package eu.openminted.uc.socialsciences.variabledetection.pipelines;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.kohsuke.args4j.Option;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import eu.openminted.uc.socialsciences.variabledetection.uima.VariableMentionDisambiguator;

public class DetectionDisambiguationPipeline
{
    @Option(name = "-i", aliases = "--input", usage = "input pattern for input data to be labeled", required = true)
    private String input = null;

    @Option(name = "-if", aliases = "--input-format", usage = "input format (by default XMI)")
    private String inputFormat = "xmi";

    @Option(name = "-o", aliases = "--output", usage = "path for output", required = true)
    private String output = null;

    @Option(name = "--min-score", usage = "minimum similarity score", required = false)
    private float minScore = 3.0f;

    @Option(name = "--max-var", usage = "maximum variable count", required = false)
    private int maxVars = 3;

    @Option(name = "--var-file", usage = "variable specification", required = true)
    private String varFile;

    private static final String DETECTION_MODEL_LOCATION = "classpath:/models/variable-detection/";
    private static final String DISAMBIGUATION_MODEL_LOCATION = "classpath:/models/variable-disambiguation/variable-disambiguation-model.ser";
    private static final String LANGUAGE_CODE = "en";

    public static void main(String[] args) throws Exception
    {
        new DetectionDisambiguationPipeline().run(args);
    }

    private void run(String[] args) throws Exception
    {
        new CommandLineArgumentHandler().parseInput(args, this);

        // Route logging through log4j
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");

        runPipeline(
                createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_OVERRIDE_DOCUMENT_METADATA, true,
                        XmiReader.PARAM_SOURCE_LOCATION, input, 
                        XmiReader.PARAM_LANGUAGE, LANGUAGE_CODE),
                //Preprocessing should be the same as the one used for model training
                createEngineDescription(
                        BreakIteratorSegmenter.class),
                createEngineDescription(
                        OpenNlpPosTagger.class),
                createEngineDescription(
                        StanfordLemmatizer.class),
                // Not sure where we would need named entities
//                createEngineDescription(
//                        OpenNlpNamedEntityRecognizer.class),
                createEngineDescription(
                        StopWordRemover.class,
                        StopWordRemover.PARAM_MODEL_LOCATION, 
                                "classpath:/stopwords/stopwords_english_punctuation.txt"),
                // The variable mention detection doesn't work well so we only run the
                // disambiguator and use a threshold to determine which variables will be
                // recorded
//                createEngineDescription(
//                        VariableMentionDetector.class,
//                        VariableMentionDetector.PARAM_MODEL_LOCATION, DETECTION_MODEL_LOCATION),
                createEngineDescription(
                        VariableMentionDisambiguator.class,
                        VariableMentionDisambiguator.PARAM_SCORE_THRESHOLD, minScore,
                        VariableMentionDisambiguator.PARAM_MAX_MENTIONS, maxVars,
                        VariableMentionDisambiguator.PARAM_DISAMBIGUATE_ALL_MENTIONS, true,
                        VariableMentionDisambiguator.PARAM_MODEL_LOCATION, DISAMBIGUATION_MODEL_LOCATION,
                        VariableMentionDisambiguator.PARAM_VARIABLE_FILE_LOCATION, varFile),
                createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, output,
                        XmiWriter.PARAM_OVERWRITE, true));
    }
}
