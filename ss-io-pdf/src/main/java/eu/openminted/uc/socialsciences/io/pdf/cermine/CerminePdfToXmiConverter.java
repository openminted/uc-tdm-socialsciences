package eu.openminted.uc.socialsciences.io.pdf.cermine;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import eu.openminted.uc.socialsciences.io.pdfx.CasValidatorComponent;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

/**
 * This class is responsible for converting the output of pdfx, which is XML
 * with a custom schema, into UIMA XMI format.
 */
public class CerminePdfToXmiConverter
{
	private static final Logger logger = Logger.getLogger(CerminePdfToXmiConverter.class);

	public static final String GERMAN_WORDS_DICTIONARY_FILENAME = "german-words.dic";
	public static final String ENGLISH_WORDS_DICTIONARY_FILENAME = "english-words.dic";

	public static final String LANGUAGE_CODE_EN = "en";
	public static final String LANGUAGE_CODE_DE = "de";

	private String germanDictionaryPath;
	private String englishDictionaryPath;

	@Option(name = "-i", required = true, usage = "input directory containing pdfx XML files")
	private String inputPath = null;

	@Option(name = "-o", required = true, usage = "output directory to save converted XMI files")
	private String outputPathXmi = null;

	@Option(name = "-lang", usage = "language of input documents.")
	private String inputLanguage = LANGUAGE_CODE_EN;

	@Option(name = "-home", required = true, usage = "Path to application home where required files (e.g. dictionary " +
			"files) are located")
	private String homePath = null;

	@Option(name = "-overwrite", usage = "(Optional) if set to true, program will overwrite XMI files " +
			" that already exist in output directory. If not set, program will throw an exception if a file already " +
			"exists in the output directory.")
	private boolean overwriteOutput = false;

	public CerminePdfToXmiConverter(String homePath, boolean overwriteOutput) {
		setHomePath(homePath);
		setOverwriteOutput(overwriteOutput);
	}

	public CerminePdfToXmiConverter() {
	}

	public void setOverwriteOutput(boolean overwriteOutput) {
		this.overwriteOutput = overwriteOutput;
		logger.info(String.format("overwrite output set to [%s]", overwriteOutput));
	}

	public void setHomePath(String homePath) {
		this.homePath = homePath;
		logger.info(String.format("homePath: %s", homePath));

		germanDictionaryPath = new File(homePath, GERMAN_WORDS_DICTIONARY_FILENAME).getPath();
		englishDictionaryPath = new File(homePath, ENGLISH_WORDS_DICTIONARY_FILENAME).getPath();

		logger.debug("German Dictionary path: " + germanDictionaryPath);
		logger.debug("English Dictionary path: " + englishDictionaryPath);
	}

	/**
	 * Main method to run the converter from command line.
	 * @param args program arguments
	 *
	 * @throws UIMAException uima exception
	 * @throws IOException io exception
	 */
	public static void main(String[] args)
			throws UIMAException, IOException
	{
		new CerminePdfToXmiConverter().run(args);
	}

	private void run(String[] args)
			throws IOException, UIMAException
	{
		new CommandLineArgumentHandler().parseInput(args, this);

		germanDictionaryPath = new File(homePath, GERMAN_WORDS_DICTIONARY_FILENAME).getPath();
		englishDictionaryPath = new File(homePath, ENGLISH_WORDS_DICTIONARY_FILENAME).getPath();

		logger.debug("German Dictionary path: " + germanDictionaryPath);
		logger.debug("English Dictionary path: " + englishDictionaryPath);

		logger.info("Input path: " + inputPath);
		logger.info("Output xmi path: " + outputPathXmi);
		logger.info("Input language: " + inputLanguage);

		logger.info("Conversion started...");
		convertToXmi(inputPath, outputPathXmi, inputLanguage);
		logger.info("Process finished.");

		//Create Cas Dump files
		/*Path inputDir = Paths.get(inputPath);
		String outputDir = inputDir.toString();
		for (Path xml : getXmlListFromDirectory(inputDir)) {
			String inputResource = xml.toString();
			String outputResourceCasDump = Paths.get(outputDir, FilenameUtils.getBaseName(inputResource) + ".cas.dump")
					.toString();
			createCasDump(inputResource, outputResourceCasDump, inputLanguage);
		}*/
	}

	/**
	 * Converts a given PDF file into UIMA XMI format and stores it at the given location.
	 *
	 * @param inputResource
	 *            The path to the XML file to be converted
	 * @param outputResource
	 *            The path of the output file
	 * @param language language of input documents
	 * @throws UIMAException uima exception
	 * @throws IOException ui exception
	 */
	public void convertToXmi(String inputResource, String outputResource, String language)
			throws UIMAException, IOException
	{
		String dictionaryPath;
		switch (language){
			case LANGUAGE_CODE_EN:
				dictionaryPath = englishDictionaryPath;
				break;
			case LANGUAGE_CODE_DE:
				dictionaryPath = germanDictionaryPath;
				break;
			default:
				throw new IllegalArgumentException("Unknown language selected.");
		}

		runPipeline(
				createReaderDescription(CerminePdfReader.class,
						CerminePdfReader.PARAM_LANGUAGE, language,
						CerminePdfReader.PARAM_SOURCE_LOCATION, inputResource,
						CerminePdfReader.PARAM_PATTERNS, "[+]*.pdf"),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, dictionaryPath,
						HyphenationRemover.PARAM_MODEL_ENCODING, "utf8",
						HyphenationRemover.PARAM_TYPES_TO_COPY,
						new String[]{"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
								"webanno.custom.Reference"}),
				createEngineDescription(OpenNlpSegmenter.class,
						OpenNlpSegmenter.PARAM_WRITE_SENTENCE, true,
						OpenNlpSegmenter.PARAM_WRITE_TOKEN, true,
						OpenNlpSegmenter.PARAM_STRICT_ZONING, true
						/*OpenNlpSegmenter.PARAM_ZONE_TYPES,
						new String[]{
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph"
						}*/),
				createEngineDescription(CasValidatorComponent.class,
						CasValidatorComponent.PARAM_STRICT_CHECK, true),
				createEngineDescription(XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, outputResource,
						XmiWriter.PARAM_OVERWRITE, overwriteOutput,
						XmiWriter.PARAM_STRIP_EXTENSION, true));
	}

	/**
	 * Converts a given PDF file into UIMA XMI format and stores the result as CAS dump at the given
	 * output location.
	 *
	 * @param inputResource
	 *            The path to the XML file to be converted
	 * @param outputResource
	 *            The path of the output file for the dump
	 * @param language language of input document
	 * @throws UIMAException uima exception
	 * @throws IOException io exception
	 */
	public void createCasDump(String inputResource, String outputResource, String language)
			throws UIMAException, IOException
	{
		String dictionaryPath;
		switch (language){
			case LANGUAGE_CODE_EN:
				dictionaryPath = englishDictionaryPath;
				break;
			case LANGUAGE_CODE_DE:
				dictionaryPath = germanDictionaryPath;
				break;
			default:
				throw new IllegalArgumentException("Unknown language selected.");
		}

		runPipeline(
				createReaderDescription(CerminePdfReader.class,
						CerminePdfReader.PARAM_LANGUAGE, language,
						CerminePdfReader.PARAM_SOURCE_LOCATION, inputResource,
						CerminePdfReader.PARAM_PATTERNS, "[+]*.xml"),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, dictionaryPath,
						HyphenationRemover.PARAM_MODEL_ENCODING, "utf8",
						HyphenationRemover.PARAM_TYPES_TO_COPY,
						new String[]{"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
								"webanno.custom.Reference"}),
				createEngineDescription(OpenNlpSegmenter.class,
						OpenNlpSegmenter.PARAM_WRITE_SENTENCE, true,
						OpenNlpSegmenter.PARAM_WRITE_TOKEN, true,
						OpenNlpSegmenter.PARAM_STRICT_ZONING, true
						/*OpenNlpSegmenter.PARAM_ZONE_TYPES,
						new String[]{
								"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph"
						}*/),
				createEngineDescription(CasValidatorComponent.class,
						CasValidatorComponent.PARAM_STRICT_CHECK, true),
				createEngineDescription(CasDumpWriter.class,
						CasDumpWriter.PARAM_TARGET_LOCATION, outputResource,
						CasDumpWriter.PARAM_SORT, true));
	}
}
