package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        pdfxXmlCreator.process(inputPath, "");
        //todo check if all of the xml files have been created
    }

    @Test
    public void testOverwriteOutputFalse() throws IOException {
        test();

        pdfxXmlCreator.setOverwriteOutput(false);
        List<Path> outputFiles = pdfxXmlCreator.process(inputPath, "");
        assert pdfxXmlCreator.isOverwriteOutput() && (outputFiles == null || outputFiles.size() == 0);
    }
}
