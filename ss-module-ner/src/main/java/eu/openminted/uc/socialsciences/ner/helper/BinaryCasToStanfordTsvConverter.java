package eu.openminted.uc.socialsciences.ner.helper;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class BinaryCasToStanfordTsvConverter {

	private static final Logger logger = Logger.getLogger(BinaryCasToStanfordTsvConverter.class);

	public static final String LANGUAGE_CODE_EN = "en";
	public static final String LANGUAGE_CODE_DE = "de";

	private String inputPath = null;
	private String outputPath = null;
	private String inputLanguage = LANGUAGE_CODE_EN;

	public static void main(String[] args) {
		try {
			new BinaryCasToStanfordTsvConverter().process(args);
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}
	}

	protected void process(String[] args) throws UIMAException, IOException {
		processArguments(args);

		convertToTsv(inputPath, outputPath, inputLanguage);
	}

	private void convertToTsv(String inputResource, String outputResource, String language)
			throws ResourceInitializationException, UIMAException, IOException {

		/*
		 * TODO: filter by language, i.e. read language from metadata and create
		 * output separately for each language!
		 */
		runPipeline(createReaderDescription(BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, inputResource,
				BinaryCasReader.PARAM_PATTERNS, "/**/*.bin",
				BinaryCasReader.PARAM_LANGUAGE, language),
				createEngineDescription(MyStanfordTsvWriter.class,
						MyStanfordTsvWriter.PARAM_TARGET_LOCATION, outputResource,
						MyStanfordTsvWriter.PARAM_USE_SUBTYPES, true,
						MyStanfordTsvWriter.PARAM_SINGULAR_TARGET, true, MyStanfordTsvWriter.PARAM_OVERWRITE, true));
	}

	// TODO rewrite this using Apache CLI
	// https://commons.apache.org/proper/commons-cli/usage.html
	protected void processArguments(String[] args) {
		if (args.length >= 1) {
			inputPath = args[0];
		}
		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase(LANGUAGE_CODE_DE) || args[1].equalsIgnoreCase(LANGUAGE_CODE_EN)) {
				inputLanguage = args[1].toLowerCase();
			} else {
				logger.warn(
						"Undefined input language was provided, default value [" + inputLanguage + "] will be used");
			}
		} else {
			logger.warn("Input language was not provided, default value [" + inputLanguage + "] will be used");
		}

		Scanner scanner = new Scanner(System.in);
		while (null == inputPath || inputPath.length() < 1) {
			System.out.println("Please provide path to input directory containing binary CAS files:");
			inputPath = scanner.nextLine();
		}
		scanner.close();
		outputPath = inputPath + File.separator + "stanfordTrain.tsv";

		logger.info("Input path: " + inputPath);
		logger.info("Output path: " + outputPath);
		logger.info("Input language: " + inputLanguage);
	}
}