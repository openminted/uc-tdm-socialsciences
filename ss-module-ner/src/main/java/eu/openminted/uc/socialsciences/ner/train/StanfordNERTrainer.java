package eu.openminted.uc.socialsciences.ner.train;

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;

public class StanfordNERTrainer {
	private static final Logger logger = LogManager.getLogger(StanfordNERTrainer.class);
	private static final String DEFAULT_OUTPUT_PATH = "omtd-ner-model.ser.gz";

	@Option(name="-i", usage="path to labeled input data (.tsv file)", required = true)
	private String input = null;

	@Option(name="-t", usage="path to training properties file", required = true)
	private String trainingPropertiesFile = null;

	@Option(name="-o", usage="[optional] path to save the model")
	private String output = DEFAULT_OUTPUT_PATH;

	public static void main(String[] args)
	{
		new StanfordNERTrainer().run(args);
	}

	private void run(String[] args)
	{
		new CommandLineArgumentHandler().parseInput(args, this);

		logger.info("Reading properties file from [" + trainingPropertiesFile + "]");
		logger.info("Reading training file from [" + input + "]");

		if (output.equals(DEFAULT_OUTPUT_PATH))
		{
			logger.info("No path for saving the trained model was specified. Default value will be used.");
		}
		logger.info("Will write the trained model to [" + output + "]");

		/*
		 * options: IOB1, IOB2, IOE1, IOE2, SBIEO, IO, BIO, BILOU, noprefix
		 */
		String classification = "noprefix";
		/*
		 * if false, representation will be mapped back to IOB1 on output
		 */
		boolean retainClassification = true;

		StanfordNERTrainer trainModel = new StanfordNERTrainer();
		trainModel.trainCrf(output, trainingPropertiesFile, input, classification, retainClassification);
		logger.info("Model training finished.");
	}

	private void trainCrf(String serializeFile, String prop, String trainFile, String classification,
			boolean retainClassification) {
		Properties props = StringUtils.propFileToProperties(prop);
		if (serializeFile != null) {
			props.setProperty("serializeTo", serializeFile);
		}
		if (trainFile != null) {
			props.setProperty("trainFile", trainFile);
		}
		props.setProperty("entitySubclassification", classification);
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		flags.retainEntitySubclassification = retainClassification;
		flags.readerAndWriter="edu.stanford.nlp.sequences.CoNLLDocumentReaderAndWriter";
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(serializeFile);
	}
}