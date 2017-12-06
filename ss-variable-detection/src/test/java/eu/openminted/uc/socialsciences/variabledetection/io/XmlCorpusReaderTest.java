package eu.openminted.uc.socialsciences.variabledetection.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;

public class XmlCorpusReaderTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
    
    @Test
    public void test() throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(XmlCorpusReader.class,
                XmlCorpusReader.PARAM_SOURCE_LOCATION, "src/test/resources/xml/data",
                XmlCorpusReader.PARAM_PATTERNS, "[+]**/*.xml");

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        assertTrue(FileUtils.contentEqualsIgnoreEOL(
                new File("src/test/resources/xml/reference/9V7EL1_1.xml.dump"),
                outputFile, 
                "UTF-8"));
    }
}
