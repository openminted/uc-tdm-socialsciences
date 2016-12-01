package train;

import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;

public class StanfordNERTrainer {

	public static void main(String[] args) {
		String serializeFile = "src/main/resources/omtd-ner-model.ser.gz";
		String prop = "src/main/resources/trainingProperties.txt";
		/*
		 * TODO need to find out how to train on corpus rather than single file
		 */
		/*
		 * TODO: need to convert and use converted files instead
		 */
		String trainFile = "src/test/resources/test.tsv";

		StanfordNERTrainer trainModel = new StanfordNERTrainer();
		trainModel.trainCrf(serializeFile, prop, trainFile);

	}

	private void trainCrf(String serializeFile, String prop, String trainFile) {
		Properties props = StringUtils.propFileToProperties(prop);
		props.setProperty("serializeTo", serializeFile);
		props.setProperty("trainFile", trainFile);
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(serializeFile);
	}

}
