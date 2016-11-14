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
        String testDocument = "src/test/resources/14_Paper.xml";
        if (!(new File(testDocument)).isFile())
            assert pdfxXmlCreator.process(testDocument, null).size() != 0;

        pdfxXmlCreator.setOverwriteOutput(false);
        List<Path> outputFiles = pdfxXmlCreator.process(testDocument, null);
        assert !pdfxXmlCreator.isOverwriteOutput();
        assert outputFiles.size() == 0;
    }

    @Test
	public void testInvalidInputDirectoryAndValidOutputNull() {
		String invalidInputPath = "a-really-invalid-input-directory";
		String validOutputNull = null;
        pdfxXmlCreator.setOverwriteOutput(false);

		assert (pdfxXmlCreator.process(invalidInputPath, validOutputNull).size() +
                pdfxXmlCreator.getSkippedFileList().size()) == 0;
    }

    @Test
    public void testValidSingleFileAndValidOutput() {
        String validInputFile = "src/test/resources/14_Paper.pdf";
        String validOutputFile = "src/test/resources/";
        pdfxXmlCreator.setOverwriteOutput(false);

        assert (pdfxXmlCreator.process(validInputFile, validOutputFile).size() +
                pdfxXmlCreator.getSkippedFileList().size()) == 1;
    }

    @Test
    public void testValidInputDirectoryAndValidOutput() {
        String validInputFolder = "src/test/resources/";
        String validOutputFolder = "src/test/resources/";
        pdfxXmlCreator.setOverwriteOutput(false);

        assert (pdfxXmlCreator.process(validInputFolder, validOutputFolder).size() +
                pdfxXmlCreator.getSkippedFileList().size()) > 0;
    }

    @Test
    public void testCreateOutputDirectory() {
        String outputDirectory = "src/test/java/testCreateOutputDirectory";
        String inputDirectory = "src/test/java";
        assert pdfxXmlCreator.process(inputDirectory, outputDirectory).size() == 0;
        assert new File(outputDirectory).isDirectory();
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
