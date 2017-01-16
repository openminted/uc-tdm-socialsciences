package eu.openminted.uc.socialsciences.ner.helper;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringOptionHandler;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;

public class BinaryCasToStanfordTsvConverter {

	private static final Logger logger = Logger.getLogger(BinaryCasToStanfordTsvConverter.class);

	private static final String DEFAULT_LANGUAGE = "en";
	private static final String DEFAULT_OUTPUT = "stanfordTrain.tsv";

	@Option(name = "-i", handler=StringOptionHandler.class, usage = "input directory containing binary CAS files to be converted", required = true)
	private String inputPath = null;

	@Option(name = "-o", usage = "[optional] path to save the converted file to")
	private String outputPath = DEFAULT_OUTPUT;

	@Option(name = "-l", usage = "[optional] language of input")
	private String inputLanguage = DEFAULT_LANGUAGE;

	public static void main(String[] args) {
		new BinaryCasToStanfordTsvConverter().run(args);
	}

	protected void run(String[] args) {
		new CommandLineArgumentHandler().parseInput(args, this);

		logger.info("Reading training file from [" + inputPath + "]");

		if (outputPath.equals(DEFAULT_OUTPUT)) {
			logger.info("No path for saving the trained model was specified. Default value will be used.");
		}
		logger.info("Will write the trained model to [" + outputPath + "]");

		/*
		 * TODO: filter by language, i.e. read language from metadata and create
		 * output separately for each language!
		 */
		try {
			runPipeline(
					createReaderDescription(BinaryCasReader.class,
							BinaryCasReader.PARAM_SOURCE_LOCATION, inputPath,
							BinaryCasReader.PARAM_PATTERNS, "/**/*.bin",
							BinaryCasReader.PARAM_LANGUAGE, inputLanguage),
					createEngineDescription(MyStanfordTsvWriter.class,
							MyStanfordTsvWriter.PARAM_TARGET_LOCATION, outputPath,
							MyStanfordTsvWriter.PARAM_USE_SUBTYPES, true,
							MyStanfordTsvWriter.PARAM_SINGULAR_TARGET, true, MyStanfordTsvWriter.PARAM_OVERWRITE,
							true));
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}
	}
}