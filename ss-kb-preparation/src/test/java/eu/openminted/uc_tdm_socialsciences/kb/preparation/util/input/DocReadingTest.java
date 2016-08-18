package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.convert.Converter;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.convert.PDFConverter;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

public class DocReadingTest {

	private static String docLocation;

	@BeforeClass
	public static void setUp() throws Exception {
		docLocation = "src/test/resources/pdf";
	}

	@Test
	public void testDocReader() {
		DocReader reader = new DocReader(Paths.get(docLocation));

		reader.readDocuments(DBManager.getInstance(true).createTables());
	}

	@Test
	public void testReadSingleFile() {
		DocReader reader = new DocReader(Paths.get(docLocation));
		List<Path> files = reader.getToProcess();
		Assert.assertTrue(!files.isEmpty());
		File docFile = files.get(0).toFile();
		String doc = PDFConverter.convert(docFile, Converter.TIKA);
		Assert.assertFalse(null == doc || doc.isEmpty());
	}

	@Test
	public void testReadSinglePath() {
		DocReader reader = new DocReader(Paths.get(docLocation));
		List<Path> files = reader.getToProcess();
		Assert.assertTrue(!files.isEmpty());
		Path docPath = files.get(0);
		String doc = PDFConverter.convert(docPath, Converter.TIKA);
		Assert.assertFalse(null == doc || doc.isEmpty());
	}

	@Test
	public void testReadMultipleFiles() {
		DocReader reader = new DocReader(Paths.get(docLocation));
		List<Path> files = reader.getToProcess();

		Map<String, String> docs = PDFConverter.convert(files, Converter.TIKA);

		Assert.assertFalse(docs.isEmpty());

		for (Entry<String, String> entry : docs.entrySet()) {
			Assert.assertFalse(null == entry.getKey());
			Assert.assertFalse(null == entry.getValue());
			System.out.println(entry.getValue().substring(0, 200));
		}
	}
}