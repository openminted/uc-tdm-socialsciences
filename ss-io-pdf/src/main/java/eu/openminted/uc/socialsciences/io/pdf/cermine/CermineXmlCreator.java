package eu.openminted.uc.socialsciences.io.pdf.cermine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import eu.openminted.uc.socialsciences.io.xml.AbstractXmlCreator;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 *
 */
public class CermineXmlCreator
        extends AbstractXmlCreator
{
    private static final Logger LOG = Logger.getLogger(CermineXmlCreator.class);

    @Override
    protected Path singleFileProcess(Path pdfFile, Path outFile)
    {
        Path resultPath = null;
        ContentExtractor extractor;
        try {
            extractor = new ContentExtractor();
            InputStream inputStream = new FileInputStream(pdfFile.toFile());
            extractor.setPDF(inputStream);
            Element result = extractor.getContentAsNLM();
            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outFile.toFile()), "UTF-8");
            XMLOutputter outputter = new XMLOutputter();
            outputter.output(result, outputStream);
            resultPath = outFile;
        } catch (AnalysisException e) {
            LOG.error("AnalysisException was thrown for file [" + pdfFile + "]", e);
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException was thrown for file [" + pdfFile + "]", e);
        } catch (IOException e) {
            LOG.error("IOException was thrown for file [" + pdfFile + "]", e);
        }
        return resultPath;
    }
}
