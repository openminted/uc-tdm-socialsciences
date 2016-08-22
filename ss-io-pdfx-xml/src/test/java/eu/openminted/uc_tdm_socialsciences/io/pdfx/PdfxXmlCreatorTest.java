package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PdfxXmlCreatorTest {

    private String inputPath;
    PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();

    @Before
    public void setUp() throws URISyntaxException {
        inputPath = "src/test/resources";
    }

    //this is covered by testOverwriteOutputFalse
    @Ignore
    public void test() throws IOException {
        pdfxXmlCreator.setOverwriteOutput(true);
        pdfxXmlCreator.process(inputPath, inputPath);
        //todo check if all of the xml files have been created
    }

    @Test
    public void testOverwriteOutputFalse() throws IOException {
        test();

        pdfxXmlCreator.setOverwriteOutput(false);
        List<Path> outputFiles = pdfxXmlCreator.process(inputPath, inputPath);
        assert pdfxXmlCreator.isOverwriteOutput() && (outputFiles == null || outputFiles.size() == 0);
    }

    @Test
    public void testInvalidInputDirectory() throws IOException {
        assert pdfxXmlCreator.process("a-really-invalid-input-directory", inputPath) == null;
    }

    @Test
    public void testCreateOutputDirectory() throws IOException {
        String outputDirectory = "src/test/java/testCreateOutputDirectory";
        String inputDirectory = "src/test/java";
        assert pdfxXmlCreator.process(inputDirectory, outputDirectory).size() == 0;
        FileUtils.deleteDirectory(new File(outputDirectory));
    }
}
