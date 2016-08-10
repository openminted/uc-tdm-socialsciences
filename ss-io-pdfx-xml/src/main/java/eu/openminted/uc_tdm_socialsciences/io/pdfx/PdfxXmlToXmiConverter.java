package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import org.apache.uima.UIMAException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PdfxXmlToXmiConverter {
    //todo: extract these hardcoded input/output paths
	public static final String INPUT_RESOURCE_DIR = "ss-io-pdfx-xml/src/test/resources/";
	public static final String OUTPUT_RESOURCE_DIR = "target/";

    public static final String[] RESOURCE_NAMES = {"2819", "27940"};

    public static final String WORD_DICTIONARY_PATH = "ss-io-pdfx-xml/src/main/resources/german-words-dictionary.txt";

    public static void main(String[] args) throws UIMAException, IOException {
        for(String name:RESOURCE_NAMES){
            String inputResource = INPUT_RESOURCE_DIR + name + "-pdfx.xml";
            String outputResource = OUTPUT_RESOURCE_DIR + name + "-pdfx.cas.xmi";
            String outputResourceCasDump = OUTPUT_RESOURCE_DIR + name + "-pdfx.cas.dump";
            convert(inputResource, outputResource);
            createCasDump(inputResource, outputResourceCasDump);
        }
    }

    public static void convert(String inputResource, String outputResource) throws UIMAException, IOException {
        runPipeline(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
                createEngineDescription(HyphenationRemover.class,
                        HyphenationRemover.PARAM_MODEL_LOCATION, WORD_DICTIONARY_PATH,
                        HyphenationRemover.PARAM_MODEL_ENCODING, "utf8"),
                createEngineDescription(BreakIteratorSegmenter.class,
                        BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, outputResource,
                        XmiWriter.PARAM_OVERWRITE, true)
        );
    }

    public static void createCasDump(String inputResource, String outputResource) throws UIMAException, IOException {
        runPipeline(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_SOURCE_LOCATION, inputResource),
                createEngineDescription(HyphenationRemover.class,
                        HyphenationRemover.PARAM_MODEL_LOCATION, WORD_DICTIONARY_PATH,
                        HyphenationRemover.PARAM_MODEL_ENCODING, "utf8"),
                createEngineDescription(BreakIteratorSegmenter.class,
                        BreakIteratorSegmenter.PARAM_STRICT_ZONING, true),
                createEngineDescription(CasDumpWriter.class,
                        CasDumpWriter.PARAM_TARGET_LOCATION, outputResource)
        );
    }
}
