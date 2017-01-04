package eu.openminted.uc.socialsciences.ner.main;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;

public class Pipeline {

	private static final Logger logger = Logger.getLogger(Pipeline.class);

	public static void main(String[] args) {
		// TODO should not reference test resources in main
		// --> create separate tests and in main, let user specify input source
		String inputPattern = "src/test/resources/**/*.xmi";

		String typesystemFile = Pipeline.class.getClassLoader().getResource("typesystem.xml")
				.getFile();

		boolean usePredefinedModel = false;

		//fixme currently model files should be located at
		// 	ss-module-ner/target/
		// so that the pipeline works.
		/*
		 * language should be read from document metadata
		 */
		String language = "de";

		try {
			TypeSystemDescription allTypes = mergeBuiltInAndCustomTypes(typesystemFile);
			for (TypeDescription type : allTypes.getTypes()) {
				logger.info("Type recognized: " + type.getName());
			}

			CollectionReaderDescription reader;
			reader = createReaderDescription(XmiReader.class, allTypes,
					XmiReader.PARAM_SOURCE_LOCATION, inputPattern,
					XmiReader.PARAM_LANGUAGE, language);

			AnalysisEngineDescription ner = usePredefinedModel
					? createEngineDescription(StanfordNamedEntityRecognizer.class,
							StanfordNamedEntityRecognizer.PARAM_LANGUAGE, language)
					: createEngineDescription(StanfordNamedEntityRecognizer.class,
							StanfordNamedEntityRecognizer.PARAM_LANGUAGE, language,
							StanfordNamedEntityRecognizer.PARAM_NAMED_ENTITY_MAPPING_LOCATION,
							"classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-${language}-ss_model.crf.map");

			AnalysisEngineDescription xmiWriter = createEngineDescription(
					XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, "target",
					XmiWriter.PARAM_TYPE_SYSTEM_FILE, "typesystem.xml",
					XmiWriter.PARAM_OVERWRITE, true);

			/*
			 * test pipeline - XMI input, NER, XMI output (can be viewed with
			 * UIMA
			 * CAS editor)
			 */
			runPipeline(reader, ner, xmiWriter);

			/*
			 * have to create aggregate analysis engine from several aes with
			 * flow
			 * controller?
			 */
			/*fixme
			ConfigurationParameter[] configurationParameters = null;
			Object[] configurationValues = null;
			Map<String, ExternalResourceDescription> externalResources = null;
			FlowControllerDescription flowControllerDescription = FlowControllerFactory.createFlowControllerDescription(
					FlowController.class, configurationParameters, configurationValues, externalResources);

			List<AnalysisEngineDescription> list = null;
			List<String> names = null;
			SofaMapping[] sofaMappings = null;
			TypePriorities priorities = null;

			AnalysisEngineDescription aggregateEngine = createEngineDescription(list, names, priorities, sofaMappings,
					flowControllerDescription);

			AnalysisEngine engine = AnalysisEngineFactory.createEngine(aggregateEngine);*/

		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}

	}

	private static TypeSystemDescription mergeBuiltInAndCustomTypes(String typesystemFile)
			throws ResourceInitializationException {
		TypeSystemDescription builtInTypes = TypeSystemDescriptionFactory
				.createTypeSystemDescription();
		TypeSystemDescription customTypes = TypeSystemDescriptionFactory
				.createTypeSystemDescriptionFromPath(typesystemFile);
		//fixme is it necessary to merge the types at all?
		return customTypes;//CasCreationUtils.mergeTypeSystems(Arrays.asList(builtInTypes, customTypes));
	}

}