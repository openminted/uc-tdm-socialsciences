package eu.openminted.uc.socialsciences.io.pdf.cermine;

import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import org.apache.log4j.Logger;
import org.junit.Test;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class CerminePdfReaderTest
{
    private static final Logger logger = Logger.getLogger(CerminePdfReader.class);

    @Test
    public void testReadArticle() throws Exception
    {
            String filePath = "src/test/resources/42466_Paper.pdf";
            String expectedFilePath = filePath + ".cermine.dump";

/*
			runPipeline(
					createReaderDescription(CerminePdfReader.class,
							CerminePdfReader.PARAM_LANGUAGE, "en",
							CerminePdfReader.PARAM_SOURCE_LOCATION, filePath),
					createEngineDescription(CasDumpWriter.class,
							CasDumpWriter.PARAM_TARGET_LOCATION, expectedFilePath,
							CasDumpWriter.PARAM_SORT, true));
*/


            String fileName = "42466_Paper.pdf";
            String expectedFileName = fileName + ".cermine.dump";
            logger.info("Checking reader on file [" + fileName + "] against the dump file [" + expectedFileName + "]");

            testOneWay(createReaderDescription(CerminePdfReader.class, CerminePdfReader.PARAM_LANGUAGE, "en"),
                    expectedFileName, fileName);
    }
}