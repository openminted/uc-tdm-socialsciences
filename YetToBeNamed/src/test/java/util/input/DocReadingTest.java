package util.input;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public void testDocReader() {
		DocReader reader = new DocReader();
		reader.setRootDir(getFile(docLocation).toPath());

		Map<String, Document> readDocuments = reader.readDocuments();
		for (String docID : readDocuments.keySet()) {
			Document doc = readDocuments.get(docID);
			System.out.println(String.format("Document %s:\t%s", docID, doc.getText().trim().substring(0, 40)));
		}
	}

	@Test
	public void testReadSingleFile() {
		File docFile = getFile(docLocation + shortPaperName);
		Document doc = PDFConverter.convert(docFile, Converter.TIKA);
		Assert.assertFalse(doc.isEmpty());
		System.out.println(doc.getText());
	}

	@Test
	public void testReadSinglePath() {
		Path docPath = getFile(docLocation + shortPaperName).toPath();
		Document doc = PDFConverter.convert(docPath, Converter.TIKA);
		Assert.assertFalse(doc.isEmpty());
		System.out.println(doc.getText());
	}

	@Test
	public void testReadMultipleFiles() {
		List<Path> docPaths = new ArrayList<Path>();
		addPathsToList(docPaths, shortPaperName, longPaperName);

		List<Document> docs = PDFConverter.convert(docPaths, Converter.TIKA);

		Assert.assertFalse(docs.isEmpty());

		for (Document document : docs) {
			Assert.assertFalse(document.isEmpty());
			System.out.println(document.getText().substring(0, 200));
		}

	}

	private void addPathsToList(List<Path> docPaths, String... paperNames) {
		for (String paper : paperNames) {
			docPaths.add(new File(getClass().getResource(docLocation + paper).getFile()).toPath());
		}

	}

	File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

}
