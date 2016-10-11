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
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * This class is responsible for converting the output of pdfx, which is XML
 * with an own schema, into UIMA XMI format.
 *
 * @author
 */
public class PdfxXmlToXmiConverter {
	// todo fix me
	public static final String WORD_DICTIONARY_PATH = PdfxXmlToXmiConverter.class.getClassLoader()
			.getResource("german-words-dictionary.txt").getFile();

	private static final Logger logger = Logger.getLogger(PdfxXmlToXmiConverter.class);

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
		logger.debug("WORD DICTIONARY: " + WORD_DICTIONARY_PATH);

		String inputPath = null;
		if (args.length == 1) {
			inputPath = args[0];
		}

		Scanner scanner = new Scanner(System.in);
		while (null == inputPath || inputPath.length() < 1) {
			System.out.println("Please provide path to input directory containing pdfx-xml files:");
			inputPath = scanner.nextLine();
		}
		scanner.close();
		Path inputDir = Paths.get(inputPath);

		for (Path xml : getXmlListFromDirectory(inputDir)) {
			String inputResource = xml.toString();
			String outputResource = FilenameUtils.getBaseName(inputResource) + ".cas.xmi";
			String outputResourceCasDump = FilenameUtils.getBaseName(inputResource) + ".cas.dump";
			convert(inputResource, outputResource);
			createCasDump(inputResource, outputResourceCasDump);
		}
	}

	private static List<Path> getXmlListFromDirectory(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter((p) -> p.toString().endsWith(".xml"))
					.forEach(toProcess::add);
		} catch (IOException e) {
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
	public static void convert(String inputResource, String outputResource) throws UIMAException, IOException {
		runPipeline(
				createReaderDescription(PdfxXmlReader.class,
						PdfxXmlReader.PARAM_LANGUAGE, "en",
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, WORD_DICTIONARY_PATH,
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
						XmiWriter.PARAM_OVERWRITE, true));
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
	public static void createCasDump(String inputResource, String outputResource) throws UIMAException, IOException {
		runPipeline(
				createReaderDescription(PdfxXmlReader.class,
						PdfxXmlReader.PARAM_LANGUAGE, "en",
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class,
						HyphenationRemover.PARAM_MODEL_LOCATION, WORD_DICTIONARY_PATH,
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
