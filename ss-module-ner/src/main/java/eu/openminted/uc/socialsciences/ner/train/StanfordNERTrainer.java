package eu.openminted.uc.socialsciences.ner.train;

import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;

public class StanfordNERTrainer {
	private static final Logger logger = Logger.getLogger(StanfordNERTrainer.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			printHelp();
		}

		String prop = args[0];
		logger.info("Reading properties file from [" + prop + "]");
		String trainFile = args[1];
		logger.info("Reading training file from [" + trainFile + "]");

		String serializeFile;
		if (args.length >= 3) {
			serializeFile = args[2];
		} else {
			logger.info("No path for saving the trained model was specified.");
			serializeFile = "omtd-ner-model.ser.gz";
		}
		logger.info("Will write the trained model to [" + serializeFile + "]");

		/*
		 * options: IOB1, IOB2, IOE1, IOE2, SBIEO, IO, BIO, BILOU, noprefix
		 */
		String classification = "IOB2";
		/*
		 * if false, representation will be mapped back to IOB1 on output
		 */
		boolean retainClassification = true;

		StanfordNERTrainer trainModel = new StanfordNERTrainer();
		trainModel.trainCrf(serializeFile, prop, trainFile, classification, retainClassification);
		logger.info("Model training finished.");
	}

	private static void printHelp() {
		System.out.printf("Please run the program using the following arguments:%n" +
				"[arg1] path to training properties file%n" +
				"[arg2] path to input file%n" +
				"[arg3] [optional] path to output model%n");
		System.exit(1);
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
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(serializeFile);
	}
}