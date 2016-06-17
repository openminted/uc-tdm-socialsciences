package pdf_readers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pl.edu.icm.cermine.PdfNLMContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

public class Cermine
{
    private static final String DATASET_PATH = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/pdf/";
    private static final String TEST_PDF_RESOURCE = "2819.pdf";
    private static final String OUTPUT_RESOURCE = "target/2819.txt";

    public static void main(String args[])
        throws AnalysisException, IOException
    {
        PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
        extractor.setExtractText(true);
        extractor.setExtractText(true);
        InputStream inputStream = new FileInputStream(DATASET_PATH + TEST_PDF_RESOURCE);
        Element result = extractor.extractContent(inputStream);

        OutputStreamWriter outputStream = new OutputStreamWriter(
                new FileOutputStream(OUTPUT_RESOURCE), "UTF-8");
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(result, outputStream);
    }

    @SuppressWarnings("unused")
    private static void printChildrenContents(Element result)
    {
        for (Object child : result.getChildren()) {
            Element childE = (Element) child;
            if (childE.getChildren().size() == 0) {
                System.out.println(childE.getValue());
            }
            else {
                printChildrenContents(childE);
            }
        }
    }
}
