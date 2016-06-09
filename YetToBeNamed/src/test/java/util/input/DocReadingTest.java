package util.input;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import datamodel.Document;
import util.convert.Converter;
import util.convert.PDFConverter;

public class DocReadingTest {

	private static String docLocation;
	private static String shortPaperName;
	private static String longPaperName;

	@Before
	public void setUp() throws Exception {
		docLocation = "/pdf/";
		shortPaperName = "2819.pdf";
		longPaperName = "29294.pdf";
	}

	@Test
	public void testReadSingleFile() {
		File docFile = getFile(docLocation + shortPaperName);
		Document doc = PDFConverter.convert(docFile, Converter.JPOD);
		Assert.assertFalse(doc.isEmpty());
	}

	@Test
	public void testReadSinglePath() {
		Path docPath = getFile(shortPaperName).toPath();
		Document doc = PDFConverter.convert(docPath, Converter.JPOD);
		Assert.assertFalse(doc.isEmpty());
	}

	@Test
	public void testReadMultipleFiles() {
		List<Path> docPaths = new ArrayList<Path>();

		List<Document> docs = PDFConverter.convert(docPaths, Converter.JPOD);

		Assert.assertFalse(docs.isEmpty());

		for (Document document : docs) {
			Assert.assertFalse(document.isEmpty());
		}

	}

	File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

}
