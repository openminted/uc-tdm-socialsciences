package eu.openminted.uc.socialsciences.io.pdfx;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

public class PdfxXmlReaderTest {
	private static final Logger logger = Logger.getLogger(PdfxXmlReaderTest.class);
	public static final String TEST_RESOURCES_PATH = "src/test/resources/";

	public static final String TEST_RESOURCE_ARTICLE1 = "14_Paper.xml";

	public static final String TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP = "14_Paper-pdfx-appended.xml.dump";

	public static final String TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP_PATH = TEST_RESOURCES_PATH
			+ TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP;

	public PdfxXmlReaderTest() {

	}

	@Test
	public void testReadArticles() throws Exception {

		List<Path> xmlFiles = getXmlFilesFromDir(Paths.get(TEST_RESOURCES_PATH));
		logger.info("found [" + xmlFiles.size() + "] files to test.");

		for (Path xml : xmlFiles) {
			String filePath = xml.toString();
			String expectedFilePath = filePath + ".dump";

/*
			runPipeline(
					createReaderDescription(PdfxXmlReader.class,
							PdfxXmlReader.PARAM_LANGUAGE, "en",
							PdfxXmlReader.PARAM_SOURCE_LOCATION, filePath),
					createEngineDescription(CasDumpWriter.class,
							CasDumpWriter.PARAM_TARGET_LOCATION, expectedFilePath,
							CasDumpWriter.PARAM_SORT, true));
*/


			String fileName = xml.getFileName().toString();
			String expectedFileName = fileName + ".dump";
			logger.info("Checking reader on file [" + fileName + "] against the dump file [" + expectedFileName + "]");

			testOneWay(createReaderDescription(PdfxXmlReader.class, PdfxXmlReader.PARAM_LANGUAGE, "en"),
					expectedFileName, fileName);
		}
	}

	private static List<Path> getXmlFilesFromDir(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter((p) -> p.toString().endsWith(".xml"))
					.forEach(toProcess::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toProcess;
	}

	@Ignore
	public void testReadArticle1WithAppendNewLine() throws Exception {
		// After applying a change to the reader that changes its underlying
		// jcas, this part should be uncommented
		// and run once to create a new dump test resource file to be used in
		// one-way test



/*
		runPipeline(createReaderDescription(PdfxXmlReader.class,
				PdfxXmlReader.PARAM_LANGUAGE, "en",
				PdfxXmlReader.PARAM_SOURCE_LOCATION, TEST_RESOURCES_PATH + TEST_RESOURCE_ARTICLE1,
				PdfxXmlReader.PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, true),
				createEngineDescription(CasDumpWriter.class,
						CasDumpWriter.PARAM_TARGET_LOCATION,
						TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP_PATH,
						CasDumpWriter.PARAM_SORT, true));
*/


		testOneWay(
				createReaderDescription(PdfxXmlReader.class, PdfxXmlReader.PARAM_LANGUAGE, "en",
						PdfxXmlReader.PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, true),
				TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP, TEST_RESOURCE_ARTICLE1);

	}
}