package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class PdfxXmlCreatorTest {

    private String inputPath;

    @Before
    public void setUp() throws URISyntaxException {
        inputPath = "src/test/resources";
    }

    @Test
    public void test() throws IOException {
        PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();
        pdfxXmlCreator.setOverwriteOutput(true);

        pdfxXmlCreator.process(inputPath, "");
        //todo check if all of the xml files have been created
    }
}
