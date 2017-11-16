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
@ResourceInput(type = "corpus", dataFormat = @DataFormat(fileExtension = ".xmi"), encoding = "UTF-8", keyword = "xmi")
@ResourceOutput(type = "corpus", dataFormat = @DataFormat(fileExtension = ".xmi"), encoding = "UTF-8", keyword = "xmi")
public class XmiNERPipeline
{
    private static final Logger logger = LogManager.getLogger(XmiNERPipeline.class);

    public static void NERInferenceForPDFs(String[] args)
    {
        String inputDir = args[0];
        String outDir = args[1];

        try {
            File outDirF = new File(outDir);
            if (!outDirF.exists()) {
                outDirF.mkdirs();
            }

            logger.info("Setting parameters for NER");
            Pipeline pipelineNER = new Pipeline();
            pipelineNER.setInput(inputDir + "/*.xmi");
            pipelineNER.setOutput(outDir);
            pipelineNER.setUseStanfordModels(false);
            logger.info("Running NER");
            pipelineNER.run();
            logger.info("NER finished");
        }
        catch (Exception e) {
            logger.info("ERROR", e);
        }

    }
}
