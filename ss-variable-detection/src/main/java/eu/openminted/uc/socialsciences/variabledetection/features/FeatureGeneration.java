package eu.openminted.uc.socialsciences.variabledetection.features;

import static eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationModelTrainer.DATASET_DIR;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceComparator;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceNormComparator;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubstringComparator;
import org.dkpro.similarity.algorithms.lexical.uima.ngrams.CharacterNGramResource;
import org.dkpro.similarity.algorithms.lexical.uima.ngrams.WordNGramContainmentResource;
import org.dkpro.similarity.algorithms.lexical.uima.ngrams.WordNGramJaccardResource;
import org.dkpro.similarity.algorithms.lexical.uima.string.GreedyStringTilingMeasureResource;
import org.dkpro.similarity.algorithms.lsr.uima.aggregate.MCS06AggregateResource;
import org.dkpro.similarity.algorithms.lsr.uima.path.ResnikRelatednessResource;
import org.dkpro.similarity.algorithms.vsm.uima.VectorIndexSourceRelatednessResource;
import org.dkpro.similarity.ml.FeatureConfig;
import org.dkpro.similarity.ml.io.SimilarityScoreWriter;
import org.dkpro.similarity.uima.annotator.SimilarityScorer;
import org.dkpro.similarity.uima.api.type.ExperimentalTextSimilarityScore;
import org.dkpro.similarity.uima.api.type.TextSimilarityScore;
import org.dkpro.similarity.uima.io.CombinationReader;
import org.dkpro.similarity.uima.io.SemEvalCorpusReader;
import org.dkpro.similarity.uima.io.CombinationReader.CombinationStrategy;
import org.dkpro.similarity.uima.resource.SimpleTextSimilarityResource;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationConstants;
import eu.openminted.uc.socialsciences.variabledetection.util.StopwordFilter;


/**
 * Pipeline for generating the text similarity features.
 */
public class FeatureGeneration
{
    public static final String MODE_TEMP = "temp";
    private final Map<FeatureConfig, AnalysisEngine> engineMap = new HashMap<>();
    private final List<FeatureConfig> featureConfigList;
    
    public FeatureGeneration() throws Exception
    {
        featureConfigList = getFeatureConfigs();
        for (FeatureConfig config : featureConfigList) {
            System.out.println(config.getMeasureName());

            File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/" + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/" + VariableDisambiguationConstants.Dataset.TEMP + "/"
                    + config.getTargetPath() + "/" + config.getMeasureName() + ".txt");
            File featureDirectory = new File(VariableDisambiguationConstants.FEATURES_DIR + "/" + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/" + VariableDisambiguationConstants.Dataset.TEMP + "/"
                    + config.getTargetPath() + "/");
            featureDirectory.mkdirs();

            // Tokenization
            AnalysisEngineDescription seg = createEngineDescription(BreakIteratorSegmenter.class);
            AggregateBuilder builder = new AggregateBuilder();
            builder.add(seg, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
            builder.add(seg, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
            AnalysisEngineDescription aggr_seg = builder.createAggregateDescription();

            // POS Tagging
            AnalysisEngineDescription pos = createEngineDescription(OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_LANGUAGE, "en");
            builder = new AggregateBuilder();
            builder.add(pos, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
            builder.add(pos, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
            AnalysisEngineDescription aggr_pos = builder.createAggregateDescription();

            // Lemmatization
            AnalysisEngineDescription lem = createEngineDescription(
                     StanfordLemmatizer.class);
            builder = new AggregateBuilder();
            builder.add(lem, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
            builder.add(lem, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
            AnalysisEngineDescription aggr_lem = builder.createAggregateDescription();

            // Stopword Filter (if applicable)
            AnalysisEngineDescription stopw = createEngineDescription(StopwordFilter.class,
                    StopwordFilter.PARAM_STOPWORD_LIST,
                    "classpath:/stopwords/stopwords_english_punctuation.txt",
                    StopwordFilter.PARAM_ANNOTATION_TYPE_NAME, Lemma.class.getName(),
                    StopwordFilter.PARAM_STRING_REPRESENTATION_METHOD_NAME, "getValue");
            builder = new AggregateBuilder();
            builder.add(stopw, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
            builder.add(stopw, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
            AnalysisEngineDescription aggr_stopw = builder.createAggregateDescription();

            // Similarity Scorer
            AnalysisEngineDescription scorer = createEngineDescription(SimilarityScorer.class,
                    SimilarityScorer.PARAM_NAME_VIEW_1, CombinationReader.VIEW_1,
                    SimilarityScorer.PARAM_NAME_VIEW_2, CombinationReader.VIEW_2,
                    SimilarityScorer.PARAM_SEGMENT_FEATURE_PATH, config.getSegmentFeaturePath(),
                    SimilarityScorer.PARAM_TEXT_SIMILARITY_RESOURCE, config.getResource());

            // Output Writer
            AnalysisEngineDescription writer = createEngineDescription(SimilarityScoreWriter.class,
                    SimilarityScoreWriter.PARAM_OUTPUT_FILE, outputFile.getAbsolutePath(),
                    SimilarityScoreWriter.PARAM_OUTPUT_SCORES_ONLY, true);

            AnalysisEngine engine;
            if (config.filterStopwords()) {
                engine = createEngine(createEngineDescription(aggr_seg, aggr_pos, aggr_lem, aggr_stopw, scorer));
            }
            else {
                engine = createEngine(createEngineDescription(aggr_seg, aggr_pos, aggr_lem, scorer));
            }
            
            engineMap.put(config, engine);
        }
    }
    
    public void generateFeatures(String text1, String text2)
        throws Exception
    {
        for (FeatureConfig config : featureConfigList) {
            AnalysisEngine engine = engineMap.get(config);
            JCas jcas = engine.newJCas();
            JCas view1 = jcas.createView(CombinationReader.VIEW_1);
            JCas view2 = jcas.createView(CombinationReader.VIEW_2);

            view1.setDocumentText(text1);
            view1.setDocumentLanguage("en");
            DocumentMetaData metadata = DocumentMetaData.create(view1);
            metadata.setDocumentId("1");
            
            view2.setDocumentText(text2);
            view2.setDocumentLanguage("en");
            metadata = DocumentMetaData.create(view2);
            metadata.setDocumentId("2");

            engine.process(jcas);
            
            TextSimilarityScore score = JCasUtil.selectSingle(jcas, ExperimentalTextSimilarityScore.class);
            File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/" + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/" + VariableDisambiguationConstants.Dataset.TEMP + "/"
                    + config.getTargetPath() + "/" + config.getMeasureName() + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(Double.toString(score.getScore()));
            writer.close();
        }
    }
    
	public static void generateFeatures(VariableDisambiguationConstants.Dataset dataset, VariableDisambiguationConstants.Mode mode)
		throws Exception
	{
		List<FeatureConfig> configs = getFeatureConfigs();
		
		// Run the pipeline		
		for (FeatureConfig config : configs)
		{			
			System.out.println(config.getMeasureName());
			
			File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/" + mode.toString().toLowerCase() + "/" + dataset.toString() + "/" + config.getTargetPath() + "/" + config.getMeasureName() + ".txt");
			
			if (outputFile.exists())
			{
				System.out.println(" - skipped, feature already generated");
			} 
			else
			{			
				CollectionReader reader = createReader(SemEvalCorpusReader.class,
						SemEvalCorpusReader.PARAM_INPUT_FILE, DATASET_DIR + "/" + mode.toString().toLowerCase() + "/STS.input." + dataset.toString() + ".txt",
						SemEvalCorpusReader.PARAM_COMBINATION_STRATEGY, CombinationStrategy.SAME_ROW_ONLY.toString(), SemEvalCorpusReader.PARAM_LANGUAGE, "en");
		
				// Tokenization
				AnalysisEngineDescription seg = createEngineDescription(
						BreakIteratorSegmenter.class);
				AggregateBuilder builder = new AggregateBuilder();
				builder.add(seg, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
				builder.add(seg, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
				AnalysisEngine aggr_seg = builder.createAggregate();
				
				// POS Tagging
				AnalysisEngineDescription pos = createEngineDescription(
						OpenNlpPosTagger.class,
						OpenNlpPosTagger.PARAM_LANGUAGE, "en");		
				builder = new AggregateBuilder();
				builder.add(pos, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
				builder.add(pos, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
				AnalysisEngine aggr_pos = builder.createAggregate();
				
				// Lemmatization
				AnalysisEngineDescription lem = createEngineDescription(
						StanfordLemmatizer.class);
				builder = new AggregateBuilder();
				builder.add(lem, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
				builder.add(lem, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
				AnalysisEngine aggr_lem = builder.createAggregate();
				
				// Stopword Filter (if applicable)
				AnalysisEngineDescription stopw = createEngineDescription(
						StopwordFilter.class,
						StopwordFilter.PARAM_STOPWORD_LIST, "classpath:/stopwords/stopwords_english_punctuation.txt",
						StopwordFilter.PARAM_ANNOTATION_TYPE_NAME, Lemma.class.getName(),
						StopwordFilter.PARAM_STRING_REPRESENTATION_METHOD_NAME, "getValue");
				builder = new AggregateBuilder();
				builder.add(stopw, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_1);
				builder.add(stopw, CombinationReader.INITIAL_VIEW, CombinationReader.VIEW_2);
				AnalysisEngine aggr_stopw = builder.createAggregate();
		
				// Similarity Scorer
				AnalysisEngine scorer = createEngine(SimilarityScorer.class,
				    SimilarityScorer.PARAM_NAME_VIEW_1, CombinationReader.VIEW_1,
				    SimilarityScorer.PARAM_NAME_VIEW_2, CombinationReader.VIEW_2,
				    SimilarityScorer.PARAM_SEGMENT_FEATURE_PATH, config.getSegmentFeaturePath(),
				    SimilarityScorer.PARAM_TEXT_SIMILARITY_RESOURCE, config.getResource()
				    );
				
				// Output Writer
				AnalysisEngine writer = createEngine(SimilarityScoreWriter.class,
					SimilarityScoreWriter.PARAM_OUTPUT_FILE, outputFile.getAbsolutePath(),
					SimilarityScoreWriter.PARAM_OUTPUT_SCORES_ONLY, true);
		
				if (config.filterStopwords()) {
                    SimplePipeline.runPipeline(reader, aggr_seg, aggr_pos, aggr_lem, aggr_stopw, scorer, writer);
                }
                else {
                    SimplePipeline.runPipeline(reader, aggr_seg, aggr_pos, aggr_lem, scorer, writer);
                }
				
				System.out.println(" - done");
			}
		}
		
		System.out.println("Successful.");
	}

    private static List<FeatureConfig> getFeatureConfigs() throws IOException
    {
        // Define the features
		List<FeatureConfig> configs = new ArrayList<FeatureConfig>();
		 
		// [Prerequisites]
//		int[] ngrams_n = new int[] { 2, 3, 4 };
//		for (int n : ngrams_n) {
//            CharacterNGramIdfValuesGenerator.computeIdfScores(mode, dataset, n);
//        }
//		
//		WordIdfValuesGenerator.computeIdfScores(mode, dataset);
		
		/* TODO: YOUR CUSTOM MEASURE GOES HERE
		 * The example code snippet instantiates MyTextSimilarityMeasure using its wrapper
		 * component MyTextSimilarityResource, and passes it a configuration parameter N.
		 * The measure here is intended to operate on lists of lemmas without any stopword
		 * filtering. 
		 */
//		configs.add(new FeatureConfig(
//				createExternalResourceDescription(
//				    	MyTextSimilarityResource.class,
//				    	MyTextSimilarityResource.PARAM_N, "3"),
//				Lemma.class.getName(),
//				false,
//				"custom",
//				"MyTextSimilarityMeasure_3"
//				));
		
		// String features
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	GreedyStringTilingMeasureResource.class,
				    	GreedyStringTilingMeasureResource.PARAM_MIN_MATCH_LENGTH, "3"),
				Document.class.getName(),
				false,
				"string",
				"GreedyStringTiling_3"
				));
		
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	SimpleTextSimilarityResource.class,
				    	SimpleTextSimilarityResource.PARAM_MODE, "text",
				    	SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE, LongestCommonSubsequenceComparator.class.getName()),
				Document.class.getName(),
				false,
				"string",
				"LongestCommonSubsequenceComparator"
				));
		
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	SimpleTextSimilarityResource.class,
				    	SimpleTextSimilarityResource.PARAM_MODE, "text",
				    	SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE, LongestCommonSubsequenceNormComparator.class.getName()),
				Document.class.getName(),
				false,
				"string",
				"LongestCommonSubsequenceNormComparator"
				));
		
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	SimpleTextSimilarityResource.class,
				    	SimpleTextSimilarityResource.PARAM_MODE, "text",
				    	SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE, LongestCommonSubstringComparator.class.getName()),
				Document.class.getName(),    	
				false,
				"string",
				"LongestCommonSubstringComparator"
				));
		
//		ngrams_n = new int[] { 2, 3, 4 };
//		for (int n : ngrams_n)
//		{
//			configs.add(new FeatureConfig(
//					createExternalResourceDescription(
//							CharacterNGramResource.class,
//							CharacterNGramResource.PARAM_N, new Integer(n).toString(),
//							CharacterNGramResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/character-ngrams-idf/" + mode.toString().toLowerCase() + "/" + n + "/" + dataset.toString() + ".txt"),
//					Document.class.getName(),
//					false,
//					"n-grams",
//					"CharacterNGramMeasure_" + n
//					));
//		}
		
//		ngrams_n = new int[] { 1, 2 };
//		for (int n : ngrams_n)
//		{
//			configs.add(new FeatureConfig(
//					createExternalResourceDescription(
//					    	WordNGramContainmentResource.class,
//					    	WordNGramContainmentResource.PARAM_N, new Integer(n).toString()),
//					Token.class.getName(),
//					true,
//					"n-grams",
//					"WordNGramContainmentMeasure_" + n + "_stopword-filtered"
//					));
//		}
		
//		ngrams_n = new int[] { 1, 3, 4 };
//		for (int n : ngrams_n)
//		{
//			configs.add(new FeatureConfig(
//					createExternalResourceDescription(
//					    	WordNGramJaccardResource.class,
//					    	WordNGramJaccardResource.PARAM_N, new Integer(n).toString()),
//					Token.class.getName(),
//					false,
//					"n-grams",
//					"WordNGramJaccardMeasure_" + n
//					));			
//		}
		
//		ngrams_n = new int[] { 2, 4 };
//		for (int n : ngrams_n)
//		{
//			configs.add(new FeatureConfig(
//					createExternalResourceDescription(
//					    	WordNGramJaccardResource.class,
//					    	WordNGramJaccardResource.PARAM_N, new Integer(n).toString()),
//					Token.class.getName(),
//					true,
//					"n-grams",
//					"WordNGramJaccardMeasure_" + n + "_stopword-filtered"
//					));			
//		}
		
		/* TODO: If you plan to use the following measures, make sure that you have the
		 * necessary resources installed. 
		 * Details on obtaining and installing them can be found here:
		 * https://dkpro.github.io/dkpro-similarity/settinguptheresources/
		 */
//		Needs [Prerequisites]
//		// Resnik word similarity measure, aggregated according to Mihalcea et al. (2006)
//		configs.add(new FeatureConfig(
//				createExternalResourceDescription(
//				    	MCS06AggregateResource.class,
//				    	MCS06AggregateResource.PARAM_TERM_SIMILARITY_RESOURCE, createExternalResourceDescription(
//				    			ResnikRelatednessResource.class,
//				    			ResnikRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
//				    			ResnikRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
//				    			),
//				    	MCS06AggregateResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/word-idf/" + mode.toString().toLowerCase() + "/" + dataset.toString() + ".txt"),
//				Lemma.class.getName() + "/value",
//				false,
//				"word-sim",
//				"MCS06_Resnik_WordNet"
//				));
//		
//		// Lexical Substitution System wrapper for 
//		// Resnik word similarity measure, aggregated according to Mihalcea et al. (2006)
//		configs.add(new FeatureConfig(
//				createExternalResourceDescription(
//						TWSISubstituteWrapperResource.class,
//						TWSISubstituteWrapperResource.PARAM_TEXT_SIMILARITY_RESOURCE, createExternalResourceDescription(
//						    	MCS06AggregateResource.class,
//						    	MCS06AggregateResource.PARAM_TERM_SIMILARITY_RESOURCE, createExternalResourceDescription(
//						    			ResnikRelatednessResource.class,
//						    			ResnikRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
//						    			ResnikRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
//						    			),
//						    	MCS06AggregateResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/word-idf/" + mode.toString().toLowerCase() + "/" + dataset.toString() + ".txt")),
//				"word-sim",
//				"TWSI_MCS06_Resnik_WordNet"
//				));
//				
//		// Explicit Semantic Analysis
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	VectorIndexSourceRelatednessResource.class,
                        VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
                        DkproContext.getContext().getWorkspace().getAbsolutePath()
                                + "/ESA/VectorIndexes/wordnet"),
				Lemma.class.getName() + "/value",
				false,
				"esa",
				"ESA_WordNet"
				));
//		
		configs.add(new FeatureConfig(
				createExternalResourceDescription(
				    	VectorIndexSourceRelatednessResource.class,
                        VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
                        DkproContext.getContext().getWorkspace().getAbsolutePath()
                                + "/ESA/VectorIndexes/wiktionary_en"),
				Lemma.class.getName() + "/value",
				false,
				"esa",
				"ESA_Wiktionary"
				));
        return configs;
    }
	
	public static void combineFeatureSets(VariableDisambiguationConstants.Mode mode, VariableDisambiguationConstants.Dataset target, VariableDisambiguationConstants.Dataset... sources)
			throws IOException
	{	
		String outputFolderName = target.toString();
		
		System.out.println("Combining feature sets");
		
		// Check if target directory exists. If so, delete it.
		File targetDir = new File(VariableDisambiguationConstants.FEATURES_DIR + "/" + mode.toString().toLowerCase() + "/" + target.toString());
		if (targetDir.exists())
		{
			System.out.println(" - cleaned target directory");
			FileUtils.deleteDirectory(targetDir);
		}
		
		String featurePathOfFirstSet = VariableDisambiguationConstants.FEATURES_DIR + "/" + mode.toString().toLowerCase() + "/" + sources[0].toString();
		
		Collection<File> features = FileUtils.listFiles(new File(featurePathOfFirstSet), new String[] { "txt" }, true);
		
		for (File feature : features)
		{
			if (!feature.isDirectory())
			{
				// Check that feature exists for all 
				boolean shared = true;
				
				for (int i = 1; i < sources.length; i++)
				{
					if (!new File(feature.getAbsolutePath().replace(sources[0].toString(), sources[i].toString())).exists()) {
                        shared = false;
                    }
				}
				
				if (shared)
				{					
					System.out.println(" - processing " + feature.getName());
					
					String concat = FileUtils.readFileToString(feature);
					
					for (int i = 1; i < sources.length; i++)
					{
						File nextFile = new File(feature.getAbsolutePath().replaceAll(sources[0].toString(), sources[i].toString()));
						
						concat += FileUtils.readFileToString(nextFile);
					}
					
					File outputFile = new File(feature.getAbsolutePath().replace(sources[0].toString(), outputFolderName));
					
					FileUtils.writeStringToFile(outputFile, concat);
				}
			}
		}
		
		System.out.println(" - done");
	}
}
