package eu.openminted.uc.socialsciences.ner.helper;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.StringOptionHandler;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;

public class BinaryCasToTsvConverter {

	private static final Logger logger = LogManager.getLogger(BinaryCasToTsvConverter.class);

	private static final String DEFAULT_OUTPUT = "stanfordTrain.tsv";

	@Option(name = "-i", handler = StringOptionHandler.class,
			usage = "input directory containing binary CAS files to be converted", required = true)
	private String inputPath = null;

	@Option(name = "-o", usage = "[optional] path to save the converted file to")
	private String outputPath = DEFAULT_OUTPUT;

	@Option(name = "-subtypes", handler = BooleanOptionHandler.class,
			usage = "[optional] useSubTypes flag. If set, value and modifier of an annotation will be merged to create more fine-grained classes.")
	private boolean useSubTypes = false;

	public static void main(String[] args) {
		new BinaryCasToTsvConverter().run(args);
	}

	public void run()
	{
		assertFields();
		runInternal();
	}

	private void assertFields() {
		if(inputPath==null)
			throw new IllegalArgumentException("inputPath can not be null!");
		if(outputPath==null)
			throw new IllegalArgumentException("outputPath can not be null!");
	}

	private void run(String[] args) {
		new CommandLineArgumentHandler().parseInput(args, this);

		runInternal();
	}

	private void runInternal() {
		logger.info("Reading input files from [" + inputPath + "]");

		if (outputPath.equals(DEFAULT_OUTPUT)) {
			logger.info("No path for saving the tsv files was specified. Default value will be used.");
		}
		logger.info("Will write the output to [" + outputPath + "]");

		try {
			runPipeline(
					createReaderDescription(BinaryCasReader.class,
							BinaryCasReader.PARAM_SOURCE_LOCATION, inputPath,
							BinaryCasReader.PARAM_PATTERNS, "/**/*.bin"),
					createEngineDescription(MyStanfordTsvWriter.class,
							MyStanfordTsvWriter.PARAM_TARGET_LOCATION, outputPath,
							MyStanfordTsvWriter.PARAM_USE_SUBTYPES, useSubTypes,
							MyStanfordTsvWriter.PARAM_SINGULAR_TARGET, true, MyStanfordTsvWriter.PARAM_OVERWRITE,
							true));

			logger.info("Process complete.");
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}
	}

	public void setInputPath(String inputPath)
	{
		this.inputPath = inputPath;
	}
	public String getInputPath()
	{
		return inputPath;
	}
	public void setOutputPath(String outputPath)
	{
		this.outputPath = outputPath;
	}
	public String getOutputPath()
	{
		return outputPath;
	}
	public void setUseSubTypes(boolean value)
	{
		useSubTypes = value;
	}
	public boolean isUseSubTypes()
	{
		return useSubTypes;
	}
}