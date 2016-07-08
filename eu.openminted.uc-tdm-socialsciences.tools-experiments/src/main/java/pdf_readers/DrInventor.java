package pdf_readers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jdom.Element;

import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;

public class DrInventor
{
    private static final String DATASET_DIRECTORY_PATH = "/home/local/UKP/kiaeeha/workspace/Datasets/openminted/uc-ss/pdf/";
    private static final String OUTPUT_DIRECTORY = "target/inventor/";

    public static void main(String args[])
        throws IOException, DRIexception
    {
        //Set this to the correct path pointing to DRI properties file
        Factory.setDRIPropertyFilePath("/home/local/UKP/kiaeeha/git/uc-tdm-socialsciences/eu.openminted.uc-tdm-socialsciences.tools-experiments/src/main/resources/DRIconfig.properties");

        //Test documents can be obtained from the following URL
        //  https://drive.google.com/file/d/0Bx1HpGFsGYhnbFh2SjAzU3p3X3c/view?usp=sharing
        final String[] fileNames = new String[] {
                "2819", "16597", "17527", "18479", "27939",
                "27940", "28005", "28189", "28681", "28750", "28835", "28862", "29294",
                "31259", "31451", "31457", "44921" };

        for (String entry : fileNames) {
            String input = DATASET_DIRECTORY_PATH + entry + ".pdf";
            String output = OUTPUT_DIRECTORY + entry + ".xml";

            convertPdfToXml(input, output);
        }
    }

    private static void convertPdfToXml(final String input, final String output)
        throws FileNotFoundException, UnsupportedEncodingException, IOException, DRIexception
    {
        //or Document doc_PDFpaperURL = Factory.getPDFloader().parsePDF(new URL("http://www2007.org/workshops/paper_45.pdf"));
        System.out.println("parsing [" + input + "]");
        Document doc_PDFpaperFILE = Factory.getPDFloader().parsePDF(input);

        OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(output),
                "UTF-8");
        outputStream.write(doc_PDFpaperFILE.getXMLString());
        outputStream.close();

        //clean up the document cache from memory
        doc_PDFpaperFILE.cleanUp();
    }
}
