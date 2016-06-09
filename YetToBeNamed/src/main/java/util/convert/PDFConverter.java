package util.convert;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import datamodel.Document;

public class PDFConverter {

	public static Document convert(File docFile, Converter converter) {
		return convert(docFile.toPath(), converter);
	}

	public static Document convert(Path docPath, Converter converter) {
		Document doc = new Document(docPath.getFileName().toString());

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
