package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PdfxXmlCreatorTest {

    private final static Logger logger = Logger.getLogger(PdfxXmlCreatorTest.class);
    private final String inputPath = "src/test/resources";
    private PdfxXmlCreator pdfxXmlCreator;

    @Before
    public void setUp() {
        pdfxXmlCreator = new PdfxXmlCreator();
    }

    //this is covered by testOverwriteOutputFalse
    @Ignore
    public void test() {
        pdfxXmlCreator.setOverwriteOutput(true);
        pdfxXmlCreator.process(inputPath, inputPath);
    }

    @Test
    public void testOverwriteOutputFalse() {
        test();

        pdfxXmlCreator.setOverwriteOutput(false);
        List<Path> outputFiles = pdfxXmlCreator.process(inputPath, inputPath);
        assert !pdfxXmlCreator.isOverwriteOutput() && (outputFiles == null || outputFiles.size() == 0);
    }

    @Test
    public void testInvalidInputDirectory() {
        assert pdfxXmlCreator.process("a-really-invalid-input-directory", inputPath).size() == 0;
    }

    @Test
    public void testCreateOutputDirectory() {
        String outputDirectory = "src/test/java/testCreateOutputDirectory";
        String inputDirectory = "src/test/java";
        assert pdfxXmlCreator.process(inputDirectory, outputDirectory).size() == 0;
        assert (new File(outputDirectory)).isDirectory();
        try {
            FileUtils.deleteDirectory(new File(outputDirectory));
        } catch (IOException e) {
            logger.error("Could not delete the output directory [" + outputDirectory + "]", e);
        }
    }

	@Test
	public void testSingleFile() {
		String inputFile = "src/test/resources/14_Paper.pdf";
        pdfxXmlCreator.setOverwriteOutput(true);
		List<Path> process = pdfxXmlCreator.process(inputFile, null);
		Assert.assertTrue(process.size() == 1);
	}
}
