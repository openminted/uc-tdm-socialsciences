package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;

/**
 * This class is responsible for converting the output of pdfx, which is XML
 * with an own schema, into UIMA XMI format.
 */
public class PdfxXmlToXmiConverter {
	private static final Logger logger = Logger.getLogger(PdfxXmlToXmiConverter.class);

	public static final String GERMAN_WORDS_DICTIONARY_FILENAME = "german-words-dictionary.txt";
	public static final String ENGLISH_WORDS_DICTIONARY_FILENAME = "english-words-dictionary.txt";
	private String GERMAN_DICTIONARY_PATH;
	private String ENGLISH_DICTIONARY_PATH;

	public static final String LANGUAGE_CODE_EN = "en";
	public static final String LANGUAGE_CODE_DE = "de";

	private String inputPath = null;
	private String inputLanguage = LANGUAGE_CODE_EN;

	// todo pipeline is only configured for English
	// TODO do not throw exceptions from main method
	/**
	 * Main method to run the converter from command line. Input directory
	 * containing XML files may be provided as parameter, otherwise it will be
	 * prompted for.
	 *
	 * @param args
	 *            (optional) path to directory containing XML files
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static void main(String[] args) throws UIMAException, IOException {
		new PdfxXmlToXmiConverter().process(args);
	}

	protected void process(String[] args) throws UIMAException, IOException {
		initialize();

		processArguments(args);

		Path inputDir = Paths.get(inputPath);
		String outputDir = inputDir.toString();

		for (Path xml : getXmlListFromDirectory(inputDir)) {
			String inputResource = xml.toString();
			String outputResource = Paths.get(outputDir, FilenameUtils.getBaseName(inputResource) + ".cas.xmi")
					.toString();
			String outputResourceCasDump = Paths.get(outputDir, FilenameUtils.getBaseName(inputResource) + ".cas.dump")
					.toString();

			convertToXmi(inputResource, outputResource, inputLanguage);
			createCasDump(inputResource, outputResourceCasDump, inputLanguage);
		}
	}

	@SuppressWarnings("ConstantConditions")
	protected void initialize() {
		GERMAN_DICTIONARY_PATH = PdfxXmlToXmiConverter.class.getClassLoader()
				.getResource(GERMAN_WORDS_DICTIONARY_FILENAME).getFile();
		ENGLISH_DICTIONARY_PATH = PdfxXmlToXmiConverter.class.getClassLoader()
				.getResource(ENGLISH_WORDS_DICTIONARY_FILENAME).getFile();

		logger.debug("German Dictionary path: " + GERMAN_DICTIONARY_PATH);
		logger.debug("English Dictionary path: " + ENGLISH_DICTIONARY_PATH);
	}

	protected void processArguments(String[] args) {
		if (args.length >= 1) {
			inputPath = args[0];
		}
		if (args.length >= 2) {
			if(args[1].equalsIgnoreCase(LANGUAGE_CODE_DE) || args[1].equalsIgnoreCase(LANGUAGE_CODE_EN))
				inputLanguage = args[1].toLowerCase();
			else
				logger.warn("Undefined input language was provided, default value [" + inputLanguage + "] will be used");
		}else
			logger.warn("Input language was not provided, default value [" + inputLanguage + "] will be used");

		Scanner scanner = new Scanner(System.in);
		while (null == inputPath || inputPath.length() < 1) {
			System.out.println("Please provide path to input directory containing pdfx-xml files:");
			inputPath = scanner.nextLine();
		}
		scanner.close();

		logger.info("Input path: " + inputPath);
		logger.info("Input language: " + inputLanguage);
	}

	public static List<Path> getXmlListFromDirectory(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter((p) -> p.toString().endsWith(".xml"))
					.forEach(toProcess::add);
		} catch (IOException e) {
			logger.error("Exception occurred in reading the directory: " + inputDir.toUri());
			e.printStackTrace();
		}
		return toProcess;
	}

	/**
	 * Converts a given XML file (which is output of pdf2xml conversion via
	 * pdfx) into UIMA XMI format and stores it at the given location.
	 *
	 * @param inputResource
	 *            The path to the XML file to be converted
	 * @param outputResource
	 *            The path of the output file
	 * @throws UIMAException
	 * @throws IOException
	 */
	public void convertToXmi(String inputResource, String outputResource, String language) throws UIMAException, IOException {
		String dictionaryPath;
		switch (language){
			case LANGUAGE_CODE_EN:
				dictionaryPath = ENGLISH_DICTIONARY_PATH;
				break;
			case LANGUAGE_CODE_DE:
				dictionaryPath = GERMAN_DICTIONARY_PATH;
				break;
			default:
				throw new IllegalArgumentException("Unknown language selected.");
		}

		runPipeline(
				createReaderDescription(PdfxXmlReader.class,
						PdfxXmlReader.PARAM_LANGUAGE, language,
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, dictionaryPath,
						HyphenationRemover.PARAM_MODEL_ENCODING, "utf8",
						HyphenationRemover.PARAM_TYPES_TO_COPY,
						new String[] { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
								"webanno.custom.Reference" }),
				// uncomment the following to get Token annotations
				/*
				 * createEngineDescription(BreakIteratorSegmenter.class,
				 * BreakIteratorSegmenter.PARAM_WRITE_SENTENCE, false,
				 * BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
				 */
				createEngineDescription(XmiWriter.class, XmiWriter.PARAM_TARGET_LOCATION, outputResource,
						XmiWriter.PARAM_OVERWRITE, true,
						XmiWriter.PARAM_STRIP_EXTENSION, true,
						XmiWriter.PARAM_SINGULAR_TARGET, true));
	}

	/**
	 * Converts a given XML file (which is output of pdf2xml conversion via
	 * pdfx) into UIMA XMI format and stores the result as CAS dump at the given
	 * output location.
	 *
	 * @param inputResource
	 *            The path to the XML file to be converted
	 * @param outputResource
	 *            The path of the output file for the dump
	 * @throws UIMAException
	 * @throws IOException
	 */
	public void createCasDump(String inputResource, String outputResource, String language) throws UIMAException, IOException {
		String dictionaryPath;
		switch (language){
			case LANGUAGE_CODE_EN:
				dictionaryPath = ENGLISH_DICTIONARY_PATH;
				break;
			case LANGUAGE_CODE_DE:
				dictionaryPath = GERMAN_DICTIONARY_PATH;
				break;
			default:
				throw new IllegalArgumentException("Unknown language selected.");
		}

		runPipeline(
				createReaderDescription(PdfxXmlReader.class,
						PdfxXmlReader.PARAM_LANGUAGE, language,
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, dictionaryPath,
						HyphenationRemover.PARAM_MODEL_ENCODING, "utf8",
						HyphenationRemover.PARAM_TYPES_TO_COPY,
						new String[] { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
								"webanno.custom.Reference" }),
				// uncomment the following to get Token annotations
				/*
				 * createEngineDescription(BreakIteratorSegmenter.class,
				 * BreakIteratorSegmenter.PARAM_WRITE_SENTENCE, false,
				 * BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
				 */
				createEngineDescription(CasDumpWriter.class,
						CasDumpWriter.PARAM_TARGET_LOCATION, outputResource,
						CasDumpWriter.PARAM_SORT, true));
	}
}
