package eu.openminted.uc.socialsciences.io.cas;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.testing.validation.CasValidator;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class CasValidatorComponent
        extends JCasConsumer_ImplBase
{
    private static Logger logger = Logger.getLogger(CasValidatorComponent.class);
    private static CasValidator validator = CasValidator.createWithAllChecks();

    public static final String PARAM_STRICT_CHECK = "strictCheck";
    @ConfigurationParameter(name = PARAM_STRICT_CHECK, mandatory = false, defaultValue = "false", description =
            "if set to true component will throw an exception when cas contains errors.")
    protected boolean strictCheck;

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        logger.info("CasValidation process started.");
        List<Message> messages = validator.analyze(aJCas);
        logger.info("CasValidation process finished.");
        if (messages.isEmpty())
            logger.info("CasValidation was successful!");
        else
        {
            logger.warn("CasValidator found " + messages.size() + " issues.");
            for (Message message : messages)
            {
                switch (message.level)
                {
                    case INFO:
                        logger.info(message.message);
                        break;
                    case ERROR:
                        logger.error(message.message);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected value of message.level [" + message.level + "] encountered!");
                }
            }

            if (strictCheck)
                throw new IllegalArgumentException(messages.size() + " errors were found in the jcas. You should fix them " +
                        "to be able to run the pipeline.");
        }
    }
}
