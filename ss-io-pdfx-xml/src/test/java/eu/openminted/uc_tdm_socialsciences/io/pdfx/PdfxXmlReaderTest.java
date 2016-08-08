package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.Rule;
import org.junit.Test;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class PdfxXmlReaderTest {
    public static final String TEST_RESOURCES_PATH = "src/test/resources/";
    public static final String TEST_RESOURCE_ARTICLE1 = "2819-pdfx.xml";
    public static final String TEST_RESOURCE_ARTICLE1_PATH = TEST_RESOURCES_PATH + TEST_RESOURCE_ARTICLE1;
    public static final String TEST_RESOURCE_ARTICLE1_DUMP = "2819-pdfx.xml.dump";
    public static final String TEST_RESOURCE_ARTICLE1_DUMP_PATH = TEST_RESOURCES_PATH + TEST_RESOURCE_ARTICLE1_DUMP;
    public static final String TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP = "2819-pdfx-appended.xml.dump";
    public static final String TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP_PATH = TEST_RESOURCES_PATH +
            TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP;

    public PdfxXmlReaderTest(){

    }

    @Test
    public void testRead() throws Exception
    {
        testOneWay(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en"),
                TEST_RESOURCE_ARTICLE1_DUMP,
                TEST_RESOURCE_ARTICLE1);

        //After applying a change to the reader that changes its underlying jcas, this part should be uncommented
        // and run once to create a new dump test resource file to be used in one-way test
/*
        runPipeline(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_SOURCE_LOCATION, TEST_RESOURCE_ARTICLE1_PATH),
                createEngineDescription(CasDumpWriter.class,
                        CasDumpWriter.PARAM_TARGET_LOCATION, TEST_RESOURCE_ARTICLE1_DUMP_PATH,
                        CasDumpWriter.PARAM_SORT, true)
        );
*/
    }

    @Test
    public void testReadWithAppendNewLine() throws Exception
    {
        testOneWay(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, true),
                TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP,
                TEST_RESOURCE_ARTICLE1);

        //After applying a change to the reader that changes its underlying jcas, this part should be uncommented
        // and run once to create a new dump test resource file to be used in one-way test
/*
        runPipeline(
                createReaderDescription(PdfxXmlReader.class,
                        PdfxXmlReader.PARAM_LANGUAGE, "en",
                        PdfxXmlReader.PARAM_SOURCE_LOCATION, TEST_RESOURCE_ARTICLE1_PATH,
                        PdfxXmlReader.PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, true),
                createEngineDescription(CasDumpWriter.class,
                        CasDumpWriter.PARAM_TARGET_LOCATION, TEST_RESOURCE_ARTICLE1_APPENDED_XML_DUMP_PATH,
                        CasDumpWriter.PARAM_SORT, true)
        );
*/
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}