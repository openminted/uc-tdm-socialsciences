package eu.openminted.uc.socialsciences.ner.docker;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DataFormat;
import eu.openminted.share.annotations.api.ResourceInput;
import eu.openminted.share.annotations.api.ResourceOutput;
import eu.openminted.share.annotations.api.constants.ComponentConstants;
import eu.openminted.uc.socialsciences.ner.Pipeline;

@Component(value = ComponentConstants.ComponentTypeConstants.namedEntityRecognizer)
@ResourceInput(type = "corpus", dataFormat = @DataFormat(dataFormat = "xmi"), encoding = "UTF-8", keyword = "xmi")
@ResourceOutput(type = "corpus", dataFormat = @DataFormat(dataFormat = "xmi"), encoding = "UTF-8", keyword = "xmi, ner, named entity annotation")
public class XmiNerPipeline
{
    private static final Logger logger = LogManager.getLogger(XmiNerPipeline.class);

    public static void main(String[] args)
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
        logger.info("Setting parameters for NER");
        Pipeline pipelineNER = new Pipeline();
        pipelineNER.setInput(inputDirectory + "[+]**/*.xmi");
        pipelineNER.setOutput(outputDirectory);
        pipelineNER.setUseStanfordModels(false);
        logger.info("Running NER");
        pipelineNER.run();
        logger.info("NER finished");
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
