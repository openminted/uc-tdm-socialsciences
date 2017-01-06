package eu.openminted.uc.socialsciences.ner.main;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;
import java.util.Arrays;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import org.apache.uima.util.CasCreationUtils;

public class Pipeline {

    private static final Logger logger = Logger.getLogger(Pipeline.class);

    private static void printUsage() {
		System.out.printf("Please run the program with the following arguments: %n" +
				"\t[arg1] input pattern for input data to be labeled %n" +
				"\t[arg2] path to typesystem.xml file %n" +
				"\t[arg3] path for output %n");
		System.out.printf("\t[arg4] [optional] if set to true, standard Stanford models will be used instead of the " +
                "custom models trained on social sciences data. Default: false.%n");
	}

	public static void main(String[] args) {
        boolean useStanfordModels = false;
		if (args.length < 3)
		{
			printUsage();
			System.exit(1);
		}
        if (args.length == 4)
        {
            useStanfordModels = Boolean.parseBoolean(args[3]);
        }

		String inputPattern = args[0];
		String typesystemFile = args[1];
		String outputPath = args[2];

		final String modelVariant = "ss_model.crf";
		//fixme currently model files should be located on the classpath i.e.
		//		 	target/classes
		//		 so that the pipeline works.
		try {
			TypeSystemDescription allTypes = mergeBuiltInAndCustomTypes(typesystemFile);

			CollectionReaderDescription reader;
			reader = createReaderDescription(XmiReader.class, allTypes,
					XmiReader.PARAM_SOURCE_LOCATION, inputPattern);

			AnalysisEngineDescription ner = useStanfordModels ?
                    createEngineDescription(StanfordNamedEntityRecognizer.class)
                    :
                    createEngineDescription(MyStanfordNamedEntityRecognizer.class,
                            MyStanfordNamedEntityRecognizer.PARAM_VARIANT,
							modelVariant)
                    ;

			AnalysisEngineDescription xmiWriter = createEngineDescription(
					XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputPath,
					XmiWriter.PARAM_TYPE_SYSTEM_FILE, typesystemFile,
					XmiWriter.PARAM_OVERWRITE, true,
					XmiWriter.PARAM_STRIP_EXTENSION, true);
			runPipeline(reader, ner, xmiWriter);
		} catch (UIMAException | IOException e) {
			logger.error("An error has occurred.", e);
			throw new IllegalStateException(e);
		}
	}

	public static TypeSystemDescription mergeBuiltInAndCustomTypes(String typesystemFile)
			throws ResourceInitializationException {
		TypeSystemDescription builtInTypes = TypeSystemDescriptionFactory
				.createTypeSystemDescription();
		TypeSystemDescription customTypes = TypeSystemDescriptionFactory
				.createTypeSystemDescriptionFromPath(typesystemFile);
		return CasCreationUtils.mergeTypeSystems(Arrays.asList(builtInTypes, customTypes));
	}

}