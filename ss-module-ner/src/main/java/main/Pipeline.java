package main;

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

		CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
				XmiReader.PARAM_SOURCE_LOCATION, "./**/*.xmi",
				XmiReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

		AnalysisEngineDescription xmiWriter = createEngineDescription(
				XmiWriter.class,
				XmiWriter.PARAM_TARGET_LOCATION, ".",
				XmiWriter.PARAM_TYPE_SYSTEM_FILE, "typesystem.xml");

		/*
		 * test pipeline - XMI input, NER, XMI output
		 */
		runPipeline(reader, ner, xmiWriter);

		/*
		 * convert binary CAS to XMI
		 */
		runPipeline(
				createReaderDescription(BinaryCasReader.class,
						BinaryCasReader.PARAM_SOURCE_LOCATION, ""),
				createEngineDescription(XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, "",
						XmiWriter.PARAM_STRIP_EXTENSION, true));
	}
}