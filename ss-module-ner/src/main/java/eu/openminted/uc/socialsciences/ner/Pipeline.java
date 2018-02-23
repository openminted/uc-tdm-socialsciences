package eu.openminted.uc.socialsciences.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;

public class Pipeline
{

    private static final Logger logger = LogManager.getLogger(Pipeline.class);

    @Option(name="-i", usage="input pattern for input data to be labeled", required = true)
	private String input = null;

    @Option(name="-o", usage="path for output", required = true)
	private String output = null;

    @Option(name="-standardModel", handler=BooleanOptionHandler.class, usage="[optional] Use standard stanford model " +
			"flag. If this flag is set, standard Stanford models will be used instead of the custom models trained on " +
			"social sciences data.")
    private boolean useStanfordModels = false;

	public static void main(String[] args) {
		new Pipeline().run(args);
	}

	private void run(String[] args)
	{
		new CommandLineArgumentHandler().parseInput(args, this);

		runInternal();
	}

	public void run()
	{
		assertFields();
		runInternal();
	}

	private void assertFields() {
		if(input == null) {
            throw new IllegalArgumentException("input can not be empty!");
        }
		if(output == null) {
            throw new IllegalArgumentException("output can not be empty!");
        }
	}

	private void runInternal() {
		final String modelVariant = "openminted_ss_model.crf";
		try {
			CollectionReaderDescription reader;
			reader = createReaderDescription(XmiReader.class,
					XmiReader.PARAM_SOURCE_LOCATION, input,
					XmiReader.PARAM_LENIENT, true);
			
            AnalysisEngineDescription preprocessing = createEngineDescription(
                    createEngineDescription(OpenNlpSegmenter.class,
                            OpenNlpSegmenter.PARAM_WRITE_SENTENCE, true,
                            OpenNlpSegmenter.PARAM_WRITE_TOKEN, true,
                            OpenNlpSegmenter.PARAM_STRICT_ZONING, true));

			AnalysisEngineDescription ner = useStanfordModels ?
					createEngineDescription(StanfordNamedEntityRecognizer.class)
					:
					createEngineDescription(StanfordNamedEntityRecognizer.class,
							StanfordNamedEntityRecognizer.PARAM_VARIANT,
							modelVariant)
					;

			AnalysisEngineDescription xmiWriter = createEngineDescription(
					XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, output,
					XmiWriter.PARAM_OVERWRITE, true,
					XmiWriter.PARAM_STRIP_EXTENSION, true,
					XmiWriter.PARAM_USE_DOCUMENT_ID, true);
			runPipeline(reader, preprocessing, ner, xmiWriter);
		} catch (UIMAException | IOException e) {
			logger.error("An error has occurred.", e);
			throw new IllegalStateException(e);
		}
	}

	public void setInput(String input)
	{
		this.input = input;
	}

	public String getInput()
	{
		return input;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

	public String getOutput()
	{
		return output;
	}

	public void setUseStanfordModels(boolean value)
	{
		useStanfordModels = value;
	}

	public boolean isUseStanfordModels()
	{
		return useStanfordModels;
	}
}