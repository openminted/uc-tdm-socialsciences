package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class PdfxXmlCreatorTest {

	private String inputPath;

	@Before
	public void setUp() throws URISyntaxException {
		String docFolder = "documents";
		URL docFolderURL = getClass().getClassLoader().getResource(docFolder);
		// inputPath = new File(docFolderURL.toURI()).getAbsolutePath();
		inputPath = "src/test/resources/documents";
	}

	@Test
	public void test() {
		Path inputDir = Paths.get(inputPath);
		try {
			PdfxXmlCreator.process(inputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
