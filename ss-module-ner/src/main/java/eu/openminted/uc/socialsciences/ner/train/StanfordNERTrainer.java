package eu.openminted.uc.socialsciences.ner.train;

import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;

public class StanfordNERTrainer {

	public static void main(String[] args) {
		String serializeFile = "src/main/resources/omtd-ner-model.ser.gz";
		String prop = "src/main/resources/trainingProperties.txt";
		String trainFile = "src/test/resources/stanfordTrain.tsv";

		/*
		 * options: IOB1, IOB2, IOE1, IOE2, SBIEO, IO
		 */
		String classification = "IOB2";
		/*
		 * if false, representation will be mapped back to IOB1 on output
		 */
		boolean retainClassification = true;

		StanfordNERTrainer trainModel = new StanfordNERTrainer();
		trainModel.trainCrf(serializeFile, prop, trainFile, classification, retainClassification);
	}

	private void trainCrf(String serializeFile, String prop, String trainFile, String classification,
			boolean retainClassification) {
		Properties props = StringUtils.propFileToProperties(prop);
		props.setProperty("serializeTo", serializeFile);
		props.setProperty("trainFile", trainFile);
		props.setProperty("entitySubclassification", classification);
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		flags.retainEntitySubclassification = retainClassification;
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(serializeFile);
	}
}