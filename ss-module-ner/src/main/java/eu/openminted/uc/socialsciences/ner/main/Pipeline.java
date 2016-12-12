package eu.openminted.uc.socialsciences.ner.main;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;

public class Pipeline {

	public static void main(String[] args) throws Exception {
		String inputPattern = "./**/*.bin";
		String modelLocation = "omtd-ner-model.ser.gz";
		/*
		 * question: param language? do we make use of it? do we assume separate
		 * models for EN and DE?
		 */
		String language = "en";

		CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
				XmiReader.PARAM_SOURCE_LOCATION, inputPattern,
				XmiReader.PARAM_LANGUAGE, language);

		AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class,
				StanfordNamedEntityRecognizer.PARAM_LANGUAGE, language,
				StanfordNamedEntityRecognizer.PARAM_MODEL_LOCATION, modelLocation);

		AnalysisEngineDescription xmiWriter = createEngineDescription(
				XmiWriter.class,
				XmiWriter.PARAM_TARGET_LOCATION, ".",
				XmiWriter.PARAM_TYPE_SYSTEM_FILE, "typesystem.xml");

		/*
		 * test pipeline - XMI input, NER, XMI output (can be viewed with UIMA
		 * CAS editor)
		 */
		runPipeline(reader, ner, xmiWriter);
	}
}