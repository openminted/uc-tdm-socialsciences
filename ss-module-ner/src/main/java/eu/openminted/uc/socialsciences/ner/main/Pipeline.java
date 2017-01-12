package eu.openminted.uc.socialsciences.ner.main;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class Pipeline
{

    private static final Logger logger = Logger.getLogger(Pipeline.class);

    private static void printUsage() {
		System.out.printf("Please run the program with the following arguments: %n" +
				"\t[arg1] input pattern for input data to be labeled %n" +
				"\t[arg2] path for output %n");
		System.out.printf("\t[arg3] [optional] if set to true, standard Stanford models will be used instead of the " +
                "custom models trained on social sciences data. Default: false.%n");
	}

	public static void main(String[] args) {
        boolean useStanfordModels = false;
		if (args.length < 2)
		{
			printUsage();
			System.exit(1);
		}
        if (args.length >= 3)
        {
            useStanfordModels = Boolean.parseBoolean(args[3]);
        }

		String inputPattern = args[0];
		String outputPath = args[1];

		final String modelVariant = "ss_model.crf";
		//fixme currently model files should be located on the classpath i.e.
		//		 	target/classes
		//		 so that the pipeline works.
		try {
			CollectionReaderDescription reader;
			reader = createReaderDescription(XmiReader.class,
					XmiReader.PARAM_SOURCE_LOCATION, inputPattern);

			AnalysisEngineDescription ner = useStanfordModels ?
                    createEngineDescription(StanfordNamedEntityRecognizer.class)
                    :
                    createEngineDescription(StanfordNamedEntityRecognizer.class,
							StanfordNamedEntityRecognizer.PARAM_VARIANT,
							modelVariant)
                    ;

			AnalysisEngineDescription xmiWriter = createEngineDescription(
					XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputPath,
					XmiWriter.PARAM_OVERWRITE, true,
					XmiWriter.PARAM_STRIP_EXTENSION, true);
			runPipeline(reader, ner, xmiWriter);
		} catch (UIMAException | IOException e) {
			logger.error("An error has occurred.", e);
			throw new IllegalStateException(e);
		}
	}
}