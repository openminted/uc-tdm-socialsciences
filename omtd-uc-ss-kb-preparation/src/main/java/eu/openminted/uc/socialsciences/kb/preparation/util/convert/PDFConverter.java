package eu.openminted.uc.socialsciences.kb.preparation.util.convert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

/*
 * This class was used before we decided for a pdf converter. We can get rid of
 * it if we replace all uses with the call to the pdf converter class from
 * ss-io-pdfx-xml module.
 */

public class PDFConverter {

	private static final Logger logger = Logger.getLogger(PDFConverter.class);

	public static String convert(File docFile, Converter converter) {
		return convert(docFile.toPath(), converter);
	}

	public static String convert(Path docPath, Converter converter) {
		Path fileName = docPath.getFileName();
		if (fileName == null || !Files.isRegularFile(docPath)) {
			return null;
		}
		String text = null;

		switch (converter) {
		case GROBID:
			text = convertWithGrobid(docPath);
			break;
		case JPOD:
			text = convertWithJPod(docPath);
			break;
		case PDFBOX:
			text = convertWithPdfBox(docPath);
			break;
		case TIKA:
			text = convertWithTika(docPath);
			break;
		default:
			break;
		}

		return text;
	}

	private static String convertWithTika(Path docPath) {
		// Create a Tika instance with the default configuration
		Tika tika = new Tika();
		tika.setMaxStringLength(-1); // disable max length

		// Parse file
		String text = null;
		try {
			text = tika.parseToString(docPath);
		} catch (IOException | TikaException e) {

		}
		return text;
	}

	private static String convertWithPdfBox(Path docPath) {
		return null;

	}

	private static String convertWithJPod(Path docPath) {
		return null;

	}

	private static String convertWithGrobid(Path docPath) {
		return null;

	}

	public static Map<String, String> convert(List<Path> docPaths, Converter converter) {
		Map<String, String> result = new HashMap<>();

		for (Path path : docPaths) {
			result.put(path.getFileName().toString(), convert(path, converter));
		}
		return result;
	}

}
