package util.input;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import util.convert.Converter;
import util.convert.PDFConverter;
import util.output.DBManager;

public class DocReadingTest {

	private static String docLocation;
	private static String shortPaperName;
	private static String longPaperName;

	@BeforeClass
	public static void setUp() throws Exception {
		docLocation = "/pdf/";
		shortPaperName = "2819.pdf";
		longPaperName = "29294.pdf";
	}

	@Test
	public void testDocReader() {
		DocReader reader = new DocReader(getFile(docLocation).toPath());

		reader.readDocuments(DBManager.getInstance(true).createTables());
	}

	@Test
	public void testReadSingleFile() {
		File docFile = getFile(docLocation + shortPaperName);
		String doc = PDFConverter.convert(docFile, Converter.TIKA);
		Assert.assertFalse(null == doc || doc.isEmpty());
	}

	@Test
	public void testReadSinglePath() {
		Path docPath = getFile(docLocation + shortPaperName).toPath();
		String doc = PDFConverter.convert(docPath, Converter.TIKA);
		Assert.assertFalse(null == doc || doc.isEmpty());
	}

	@Test
	public void testReadMultipleFiles() {
		List<Path> docPaths = new ArrayList<Path>();
		addPathsToList(docPaths, shortPaperName, longPaperName);

		Map<String, String> docs = PDFConverter.convert(docPaths, Converter.TIKA);

		Assert.assertFalse(docs.isEmpty());

		for (Entry<String, String> entry : docs.entrySet()) {
			Assert.assertFalse(null == entry.getKey());
			Assert.assertFalse(null == entry.getValue());
			System.out.println(entry.getValue().substring(0, 200));
		}

	}

	private void addPathsToList(List<Path> docPaths, String... paperNames) {
		for (String paper : paperNames) {
			docPaths.add(new File(getClass().getResource(docLocation + paper).getFile()).toPath());
		}

	}

	private File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

}
