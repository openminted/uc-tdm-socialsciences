package eu.openminted.uc.socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PdfxXmlCreatorTest {

    private final static Logger logger = Logger.getLogger(PdfxXmlCreatorTest.class);
    private final String inputPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private PdfxXmlCreator pdfxXmlCreator;

    @Before
    public void setUp() {
        pdfxXmlCreator = new PdfxXmlCreator();
    }

    /***
     * Generate xml documents for all of the test pdf documents
     */
    @Test
    public void testExhaustive() {
        pdfxXmlCreator.setOverwriteOutput(false);
        pdfxXmlCreator.process(inputPath, inputPath);
    }

    @Test
    public void testOverwriteOutputFalse() {
        String testDocument = inputPath + "14_Paper.xml";
        if (!(new File(testDocument)).isFile()) {
            Assert.assertNotEquals(0, pdfxXmlCreator.process(testDocument, null).size());
        }

        pdfxXmlCreator.setOverwriteOutput(false);
        List<Path> outputFiles = pdfxXmlCreator.process(testDocument, null);

        Assert.assertEquals(false, pdfxXmlCreator.isOverwriteOutput());
        Assert.assertEquals(0, outputFiles.size());
    }

    @Test
	public void testInvalidInputDirectoryAndValidOutputNull() {
		String invalidInputPath = "a-really-invalid-input-directory";
        pdfxXmlCreator.setOverwriteOutput(false);

		Assert.assertEquals(0, pdfxXmlCreator.process(invalidInputPath, null).size() +
                pdfxXmlCreator.getSkippedFileList().size());
    }

    @Test
    public void testValidSingleFileAndValidOutput() {
        String validInputFile = "src/test/resources/14_Paper.pdf";
        String validOutputFile = "src/test/resources/";
        pdfxXmlCreator.setOverwriteOutput(false);

        Assert.assertEquals(1, pdfxXmlCreator.process(validInputFile, validOutputFile).size() +
                pdfxXmlCreator.getSkippedFileList().size());
    }

    @Test
    public void testValidInputDirectoryAndValidOutput() {
        String validInputFolder = "src/test/resources/";
        String validOutputFolder = "src/test/resources/";
        pdfxXmlCreator.setOverwriteOutput(false);

        Assert.assertTrue(pdfxXmlCreator.process(validInputFolder, validOutputFolder).size() +
                pdfxXmlCreator.getSkippedFileList().size() > 0);
    }

    @Test
    public void testCreateOutputDirectory() {
        String outputDirectory = "src/test/java/testCreateOutputDirectory";
        String inputDirectory = "src/test/java";
        Assert.assertEquals(0, pdfxXmlCreator.process(inputDirectory, outputDirectory).size());
        Assert.assertTrue(new File(outputDirectory).isDirectory());
        try {
            FileUtils.deleteDirectory(new File(outputDirectory));
        } catch (IOException e) {
            logger.error("Could not delete the output directory [" + outputDirectory + "]", e);
        }
    }

	@Test
	public void testSingleFile() {
		String inputFile = "src/test/resources/457680_Paper.pdf";
        pdfxXmlCreator.setOverwriteOutput(true);
		List<Path> process = pdfxXmlCreator.process(inputFile, null);
		Assert.assertTrue(process.size() == 1);
	}
}
