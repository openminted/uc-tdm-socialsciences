package eu.openminted.uc.socialsciences.variabledetection.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class XmlCorpusToXmiConverter
{
    public static void main(String args[]) throws Exception
    {
        CollectionReader reader = createReader(XmlCorpusReader.class,
                XmlCorpusReader.PARAM_SOURCE_LOCATION, args[0],
                XmlCorpusReader.PARAM_PATTERNS, "[+]**/*.xml");

        AnalysisEngine writer = createEngine(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, args[1]);

        SimplePipeline.runPipeline(reader, writer);
    }
}
