package pdf_readers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pl.edu.icm.cermine.PdfNLMContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

public class Cermine
{
    private static final String DATASET_DIRECTORY_PATH = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/pdf/";
    private static final String OUTPUT_DIRECTORY = "target/";

    public static void main(String args[])
        throws AnalysisException, IOException
    {
        final String[] fileNames = new String[] { "2819", "16597", "17527", "18479", "27939",
                "27940", "28005", "28189", "28681", "28750", "28835", "28862", "29294", "31259",
                "31451", "31457", "44921" };

        for (String entry : fileNames) {
            String input = DATASET_DIRECTORY_PATH + entry + ".pdf";
            String output = OUTPUT_DIRECTORY + entry + ".xml";

            convertPdfToXml(input, output);
        }
    }

    private static void convertPdfToXml(final String input, final String output)
        throws AnalysisException, FileNotFoundException, UnsupportedEncodingException, IOException
    {
        PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
        InputStream inputStream = new FileInputStream(input);
        Element result = extractor.extractContent(inputStream);
        OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(output),
                "UTF-8");
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
