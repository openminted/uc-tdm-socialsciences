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

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PdfxXmlToXmiConverter {
	public static final String WORD_DICTIONARY_PATH = "src/main/resources/german-words-dictionary.txt";

	private static final Logger logger = Logger.getLogger(PdfxXmlToXmiConverter.class);

	public static void main(String[] args) throws UIMAException, IOException {
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
			String outputResource = inputResource.substring(0, inputResource.lastIndexOf('.')) + ".cas.xmi";
			String outputResourceCasDump = inputResource.substring(0, inputResource.lastIndexOf('.')) + ".cas.dump";
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

	public static void convert(String inputResource, String outputResource) throws UIMAException, IOException {
		runPipeline(
				createReaderDescription(PdfxXmlReader.class, PdfxXmlReader.PARAM_LANGUAGE, "en",
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class, HyphenationRemover.PARAM_MODEL_LOCATION,
						WORD_DICTIONARY_PATH, HyphenationRemover.PARAM_MODEL_ENCODING, "utf8"),
				createEngineDescription(BreakIteratorSegmenter.class, BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
				createEngineDescription(XmiWriter.class, XmiWriter.PARAM_TARGET_LOCATION, outputResource,
						XmiWriter.PARAM_OVERWRITE, true));
	}

	public static void createCasDump(String inputResource, String outputResource) throws UIMAException, IOException {
		runPipeline(
				createReaderDescription(PdfxXmlReader.class, PdfxXmlReader.PARAM_LANGUAGE, "en",
						PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
				createEngineDescription(HyphenationRemover.class, HyphenationRemover.PARAM_MODEL_LOCATION,
						WORD_DICTIONARY_PATH, HyphenationRemover.PARAM_MODEL_ENCODING, "utf8"),
				createEngineDescription(BreakIteratorSegmenter.class, BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
				createEngineDescription(CasDumpWriter.class, CasDumpWriter.PARAM_TARGET_LOCATION, outputResource));
	}
}
