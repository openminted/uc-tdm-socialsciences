package util.input;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import datamodel.Document;
import util.convert.PDFConverter;

public class DocReadingTest {

	private static final String DOCLOCATION = null;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testReadSingleFile() {
		File docFile = new File(DOCLOCATION);
		Document doc = PDFConverter.convert(docFile);
	}

	@Test
	public void testReadSinglePath() {
		Path docPath = Paths.get(DOCLOCATION);
		Document doc = PDFConverter.convert(docPath);
	}

	@Test
	public void testReadFromDir() {

	}

	@Test
	public void testReadMultipleFiles() {
		List<Path> docPaths = new ArrayList<Path>();
		List<Document> docs = PDFConverter.convert(docPaths);
	}

}
