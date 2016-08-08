package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import org.apache.uima.UIMAException;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class JatsXmlToXmiConverter {

    //todo: extract these hardcoded input/output paths
    public static final String INPUT_RESOURCE = "scixml-reader/src/test/resources/article1.xml";
    public static final String OUTPUT_RESOURCE_XMI = "scixml-reader/target/article1.cas.xmi";

    public static void main(String[] args) throws UIMAException, IOException {
        runPipeline(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_SOURCE_LOCATION, INPUT_RESOURCE),
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, OUTPUT_RESOURCE_XMI)
        );
    }
}
