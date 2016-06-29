package util.convert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import datamodel.Document;

public class PDFConverter {

	public static Document convert(File docFile, Converter converter) {
		return convert(docFile.toPath(), converter);
	}

	public static Document convert(Path docPath, Converter converter) {
		Path fileName = docPath.getFileName();
		if (fileName == null || !Files.isRegularFile(docPath)) {
			return null;
		}
		Document doc = new Document(fileName.toString());

		switch (converter) {
		case GROBID:
			convertWithGrobid(docPath, doc);
			break;
		case JPOD:
			convertWithJPod(docPath, doc);
			break;
		case PDFBOX:
			convertWithPdfBox(docPath, doc);
			break;
		case TIKA:
			convertWithTika(docPath, doc);
			break;
		default:
			break;
		}

		return doc;
	}

	private static void convertWithTika(Path docPath, Document doc) {
		// Create a Tika instance with the default configuration
		Tika tika = new Tika();
		tika.setMaxStringLength(-1); // disable max length

		// Parse file
		String text = null;
		try {
			text = tika.parseToString(docPath);
		} catch (IOException | TikaException e) {

		}
		doc.setText(text);
	}

	private static void convertWithPdfBox(Path docPath, Document doc) {

	}

	private static void convertWithJPod(Path docPath, Document doc) {

	}

	private static void convertWithGrobid(Path docPath, Document doc) {

	}

	public static List<Document> convert(List<Path> docPaths, Converter converter) {
		List<Document> result = new ArrayList<Document>();

		for (Path path : docPaths) {
			result.add(convert(path, converter));
		}
		return result;
	}

}
