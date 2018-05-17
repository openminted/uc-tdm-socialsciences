package eu.openminted.uc.socialsciences.io.docker;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import eu.openminted.uc.socialsciences.io.pdf.cermine.CerminePdfReader;

public class PdfXmiPipeline
{
    public static void main(String args[]) throws Exception
    {
        assertArguments(args);
        
        String inputDirectory = args[0];
        String outputDirectory = args[1];

        File inputDirectoryFile = new File(inputDirectory);
        File outputDirectoryFile = new File(outputDirectory);

        if (!outputDirectoryFile.exists()) {
            outputDirectoryFile.mkdirs();
        }

        assertDirectory(inputDirectoryFile);
        assertDirectory(outputDirectoryFile);

        createAndRunPipeline(inputDirectory, outputDirectory);
    }

    private static void createAndRunPipeline(String inputFolder, String outputFolder)
        throws ResourceInitializationException, UIMAException, IOException
    {
        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, inputFolder,
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf",
                CerminePdfReader.PARAM_NORMALIZE_TEXT, true);

        AnalysisEngine engine = createEngine(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, outputFolder,
                XmiWriter.PARAM_OVERWRITE, true);

        SimplePipeline.runPipeline(reader, engine);
    }

    private static void assertDirectory(File folder)
    {
        if (!folder.isDirectory()) {
            System.err.println("[" + folder + "] is not a directory!");
            System.exit(1);
        }
    }

    private static void assertArguments(String[] args)
    {
        if (args == null || args.length < 2) {
            System.err.println("Two arguments should be provided!");
            System.err.println("args[0]: input directory");
            System.err.println("args[1]: output directory");
            System.exit(1);
        }
    }
}
