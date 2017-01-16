package eu.openminted.uc.socialsciences.ner.main;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

public class Pipeline
{

    private static final Logger logger = Logger.getLogger(Pipeline.class);

    @Option(name="-i", usage="input pattern for input data to be labeled", required = true)
	private String input = null;

    @Option(name="-o", usage="path for output", required = true)
	private String output = null;

    @Option(name="-standardModel", handler=BooleanOptionHandler.class, usage="(Optional) Use standard stanford model " +
			"flag. If this flag is set, standard Stanford models will be used instead of the custom models trained on " +
			"social sciences data.")
    private boolean useStanfordModels = false;

	public static void main(String[] args) {
		new Pipeline().run(args);
	}

	private void run(String[] args)
	{
		new CommandLineArgumentHandler().parseInput(args, this);

		final String modelVariant = "ss_model.crf";
		//fixme currently model files should be located on the classpath i.e.
		//		 	target/classes
		//		 so that the pipeline works.
		try {
			CollectionReaderDescription reader;
			reader = createReaderDescription(XmiReader.class,
					XmiReader.PARAM_SOURCE_LOCATION, input);

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
					XmiWriter.PARAM_STRIP_EXTENSION, true);
			runPipeline(reader, ner, xmiWriter);
		} catch (UIMAException | IOException e) {
			logger.error("An error has occurred.", e);
			throw new IllegalStateException(e);
		}
	}
}