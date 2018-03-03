package eu.openminted.uc.socialsciences.variabledetection.features;

import static eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationModelTrainer.DATASET_DIR;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

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
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceComparator;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceNormComparator;
import org.dkpro.similarity.algorithms.lexical.string.LongestCommonSubstringComparator;
import org.dkpro.similarity.algorithms.lexical.uima.string.GreedyStringTilingMeasureResource;
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
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import eu.openminted.uc.socialsciences.variabledetection.disambiguation.VariableDisambiguationConstants;
import weka.core.DenseInstance;
import weka.core.Instance;

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

            AnalysisEngineDescription preprocessing = preprocessors(config.filterStopwords());

            // Similarity Scorer
            AnalysisEngineDescription scorer = createEngineDescription(
                    SimilarityScorer.class,
                    SimilarityScorer.PARAM_NAME_VIEW_1, CombinationReader.VIEW_1,
                    SimilarityScorer.PARAM_NAME_VIEW_2, CombinationReader.VIEW_2,
                    SimilarityScorer.PARAM_SEGMENT_FEATURE_PATH, config.getSegmentFeaturePath(),
                    SimilarityScorer.PARAM_TEXT_SIMILARITY_RESOURCE, config.getResource());

//            // Output Writer
//          File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
//          + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/"
//          + VariableDisambiguationConstants.Dataset.TEMP + "/" + config.getTargetPath()
//          + "/" + config.getMeasureName() + ".txt");
//            AnalysisEngineDescription writer = createEngineDescription(SimilarityScoreWriter.class,
//                    SimilarityScoreWriter.PARAM_OUTPUT_FILE, outputFile.getAbsolutePath(),
//                    SimilarityScoreWriter.PARAM_OUTPUT_SCORES_ONLY, true);

            AnalysisEngine engine = createEngine(createEngineDescription(preprocessing, scorer));

            engineMap.put(config, engine);
        }
    }

    private static JCas featureJCas;
    
    public synchronized Instance generateFeatures(String text1, String text2)
        throws Exception
    {
        Instance instance = new DenseInstance(featureConfigList.size() + 1);
        // The last column is the gold value - we set this to 0 during classifciation.
        instance.setValue(instance.numAttributes() - 1, 0.0);
        
        // We assume that we run single-threaded and re-use a single JCas all the time to avoid
        // the significant initialization overhead for creating a new JCas.
        if (featureJCas == null) {
            featureJCas = JCasFactory.createJCas();
        }
        
        int i = 0;
        for (FeatureConfig config : featureConfigList) {
            featureJCas.reset();
            
            AnalysisEngine engine = engineMap.get(config);
            JCas view1 = featureJCas.createView(CombinationReader.VIEW_1);
            JCas view2 = featureJCas.createView(CombinationReader.VIEW_2);

            view1.setDocumentText(text1);
            view1.setDocumentLanguage("en");
            DocumentMetaData metadata = DocumentMetaData.create(view1);
            metadata.setDocumentId("1");

            view2.setDocumentText(text2);
            view2.setDocumentLanguage("en");
            metadata = DocumentMetaData.create(view2);
            metadata.setDocumentId("2");

            engine.process(featureJCas);

            TextSimilarityScore score = JCasUtil.selectSingle(featureJCas,
                    ExperimentalTextSimilarityScore.class);
            
            instance.setValue(i, score.getScore());
            
//            File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
//                    + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/"
//                    + VariableDisambiguationConstants.Dataset.TEMP + "/" + config.getTargetPath()
//                    + "/" + config.getMeasureName() + ".txt");
//
//            try (FileWriter writer = new FileWriter(outputFile)) {
//                writer.write(Double.toString(score.getScore()));
//            }
            
            i++;
        }
        
        return instance;
    }

    
    public synchronized void generateFeaturesAsFiles(String text1, String text2) throws Exception
    {
        if (featureJCas == null) {
            featureJCas = JCasFactory.createJCas();
        }
        
        for (FeatureConfig config : featureConfigList) {
            File featureDirectory = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
                    + VariableDisambiguationConstants.Mode.TEMP.toString().toLowerCase() + "/"
                    + VariableDisambiguationConstants.Dataset.TEMP + "/" + config.getTargetPath()
                    + "/");
            featureDirectory.mkdirs();
            
            featureJCas.reset();
            
            AnalysisEngine engine = engineMap.get(config);
            JCas view1 = featureJCas.createView(CombinationReader.VIEW_1);
            JCas view2 = featureJCas.createView(CombinationReader.VIEW_2);

            view1.setDocumentText(text1);
            view1.setDocumentLanguage("en");
            DocumentMetaData metadata = DocumentMetaData.create(view1);
            metadata.setDocumentId("1");

            view2.setDocumentText(text2);
            view2.setDocumentLanguage("en");
            metadata = DocumentMetaData.create(view2);
            metadata.setDocumentId("2");

            engine.process(featureJCas);

            TextSimilarityScore score = JCasUtil.selectSingle(featureJCas,
                    ExperimentalTextSimilarityScore.class);
            File outputFile = new File(featureDirectory, config.getMeasureName() + ".txt");

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(Double.toString(score.getScore()));
            }
        }
    }

    private static AnalysisEngineDescription preprocessors(boolean aFilterStopwords)
        throws ResourceInitializationException
    {
        final AggregateBuilder builder = new AggregateBuilder();
        
        // Tokenization
        AnalysisEngineDescription seg = createEngineDescription(
                BreakIteratorSegmenter.class);
        builder.add(seg, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_1);
        builder.add(seg, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_2);

        // POS Tagging
        AnalysisEngineDescription pos = createEngineDescription(
                OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_LANGUAGE, "en");
        builder.add(pos, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_1);
        builder.add(pos, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_2);

        // Lemmatization
        AnalysisEngineDescription lem = createEngineDescription(StanfordLemmatizer.class);
        builder.add(lem, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_1);
        builder.add(lem, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_2);

        // Stopword Filter (if applicable)
        if (aFilterStopwords) {
//        AnalysisEngineDescription stopw = createEngineDescription(
//                StopwordFilter.class,
//                StopwordFilter.PARAM_STOPWORD_LIST,
//                "classpath:/stopwords/stopwords_english_punctuation.txt",
//                StopwordFilter.PARAM_ANNOTATION_TYPE_NAME, Lemma.class.getName(),
//                StopwordFilter.PARAM_STRING_REPRESENTATION_METHOD_NAME, "getValue");
            AnalysisEngineDescription stopw = createEngineDescription(
                    StopWordRemover.class,
                    StopWordRemover. PARAM_MODEL_LOCATION,
                    "classpath:/stopwords/stopwords_english_punctuation.txt");
            builder.add(stopw, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_1);
            builder.add(stopw, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_2);
        }
        
        return builder.createAggregateDescription();
    }
    
    public static void generateFeatures(VariableDisambiguationConstants.Dataset dataset,
            VariableDisambiguationConstants.Mode mode)
        throws Exception
    {
        List<FeatureConfig> configs = getFeatureConfigs();

        // Run the pipeline
        for (FeatureConfig config : configs) {
            System.out.println(config.getMeasureName());

            File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
                    + mode.toString().toLowerCase() + "/" + dataset.toString() + "/"
                    + config.getTargetPath() + "/" + config.getMeasureName() + ".txt");

            if (outputFile.exists()) {
                System.out.println(" - skipped, feature already generated");
            }
            else {
                CollectionReaderDescription reader = createReaderDescription(
                        SemEvalCorpusReader.class,
                        SemEvalCorpusReader.PARAM_INPUT_FILE,
                        DATASET_DIR + "/" + mode.toString().toLowerCase() + "/STS.input."
                                + dataset.toString() + ".txt",
                        SemEvalCorpusReader.PARAM_COMBINATION_STRATEGY,
                        CombinationStrategy.SAME_ROW_ONLY.toString(),
                        SemEvalCorpusReader.PARAM_LANGUAGE, "en");

                AnalysisEngineDescription preprocessing = preprocessors(config.filterStopwords());

                // Similarity Scorer
                AnalysisEngineDescription scorer = createEngineDescription(
                        SimilarityScorer.class,
                        SimilarityScorer.PARAM_NAME_VIEW_1, CombinationReader.VIEW_1,
                        SimilarityScorer.PARAM_NAME_VIEW_2, CombinationReader.VIEW_2,
                        SimilarityScorer.PARAM_SEGMENT_FEATURE_PATH, config.getSegmentFeaturePath(),
                        SimilarityScorer.PARAM_TEXT_SIMILARITY_RESOURCE, config.getResource());

                // Output Writer
                AnalysisEngineDescription writer = createEngineDescription(
                        SimilarityScoreWriter.class,
                        SimilarityScoreWriter.PARAM_OUTPUT_FILE, outputFile.getAbsolutePath(),
                        SimilarityScoreWriter.PARAM_OUTPUT_SCORES_ONLY, true);

                SimplePipeline.runPipeline(reader, preprocessing, scorer, writer);

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
        // int[] ngrams_n = new int[] { 2, 3, 4 };
        // for (int n : ngrams_n) {
        // CharacterNGramIdfValuesGenerator.computeIdfScores(mode, dataset, n);
        // }
        //
        // WordIdfValuesGenerator.computeIdfScores(mode, dataset);

        /*
         * TODO: YOUR CUSTOM MEASURE GOES HERE The example code snippet instantiates
         * MyTextSimilarityMeasure using its wrapper component MyTextSimilarityResource, and passes
         * it a configuration parameter N. The measure here is intended to operate on lists of
         * lemmas without any stopword filtering.
         */
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // MyTextSimilarityResource.class,
        // MyTextSimilarityResource.PARAM_N, "3"),
        // Lemma.class.getName(),
        // false,
        // "custom",
        // "MyTextSimilarityMeasure_3"
        // ));

        // String features
        configs.add(new FeatureConfig(
                createExternalResourceDescription(
                        GreedyStringTilingMeasureResource.class,
                        GreedyStringTilingMeasureResource.PARAM_MIN_MATCH_LENGTH, "3"),
                null, // not relevant in "text" and "jcas" modes
                false, 
                "string", 
                "GreedyStringTiling_3"));

        configs.add(new FeatureConfig(
                createExternalResourceDescription(
                        SimpleTextSimilarityResource.class,
                        SimpleTextSimilarityResource.PARAM_MODE, "text",
                        SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE,
                        LongestCommonSubsequenceComparator.class.getName()),
                null, // not relevant in "text" and "jcas" modes
                false, 
                "string", 
                "LongestCommonSubsequenceComparator"));

        configs.add(new FeatureConfig(
                createExternalResourceDescription(
                        SimpleTextSimilarityResource.class,
                        SimpleTextSimilarityResource.PARAM_MODE, "text",
                        SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE,
                        LongestCommonSubsequenceNormComparator.class.getName()),
                null, // not relevant in "text" and "jcas" modes
                false, 
                "string",
                "LongestCommonSubsequenceNormComparator"));

        configs.add(new FeatureConfig(
                createExternalResourceDescription(
                        SimpleTextSimilarityResource.class,
                        SimpleTextSimilarityResource.PARAM_MODE, "text",
                        SimpleTextSimilarityResource.PARAM_TEXT_SIMILARITY_MEASURE,
                        LongestCommonSubstringComparator.class.getName()),
                null, // not relevant in "text" and "jcas" modes
                false, 
                "string", 
                "LongestCommonSubstringComparator"));

        // ngrams_n = new int[] { 2, 3, 4 };
        // for (int n : ngrams_n)
        // {
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // CharacterNGramResource.class,
        // CharacterNGramResource.PARAM_N, new Integer(n).toString(),
        // CharacterNGramResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/character-ngrams-idf/" +
        // mode.toString().toLowerCase() + "/" + n + "/" + dataset.toString() + ".txt"),
        // Document.class.getName(),
        // false,
        // "n-grams",
        // "CharacterNGramMeasure_" + n
        // ));
        // }

        // ngrams_n = new int[] { 1, 2 };
        // for (int n : ngrams_n)
        // {
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // WordNGramContainmentResource.class,
        // WordNGramContainmentResource.PARAM_N, new Integer(n).toString()),
        // Token.class.getName(),
        // true,
        // "n-grams",
        // "WordNGramContainmentMeasure_" + n + "_stopword-filtered"
        // ));
        // }

        // ngrams_n = new int[] { 1, 3, 4 };
        // for (int n : ngrams_n)
        // {
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // WordNGramJaccardResource.class,
        // WordNGramJaccardResource.PARAM_N, new Integer(n).toString()),
        // Token.class.getName(),
        // false,
        // "n-grams",
        // "WordNGramJaccardMeasure_" + n
        // ));
        // }

        // ngrams_n = new int[] { 2, 4 };
        // for (int n : ngrams_n)
        // {
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // WordNGramJaccardResource.class,
        // WordNGramJaccardResource.PARAM_N, new Integer(n).toString()),
        // Token.class.getName(),
        // true,
        // "n-grams",
        // "WordNGramJaccardMeasure_" + n + "_stopword-filtered"
        // ));
        // }

        /*
         * TODO: If you plan to use the following measures, make sure that you have the necessary
         * resources installed. Details on obtaining and installing them can be found here:
         * https://dkpro.github.io/dkpro-similarity/settinguptheresources/
         */
        // Needs [Prerequisites]
        // // Resnik word similarity measure, aggregated according to Mihalcea et al. (2006)
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // MCS06AggregateResource.class,
        // MCS06AggregateResource.PARAM_TERM_SIMILARITY_RESOURCE, createExternalResourceDescription(
        // ResnikRelatednessResource.class,
        // ResnikRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
        // ResnikRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
        // ),
        // MCS06AggregateResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/word-idf/" +
        // mode.toString().toLowerCase() + "/" + dataset.toString() + ".txt"),
        // Lemma.class.getName() + "/value",
        // false,
        // "word-sim",
        // "MCS06_Resnik_WordNet"
        // ));
        //
        // // Lexical Substitution System wrapper for
        // // Resnik word similarity measure, aggregated according to Mihalcea et al. (2006)
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // TWSISubstituteWrapperResource.class,
        // TWSISubstituteWrapperResource.PARAM_TEXT_SIMILARITY_RESOURCE,
        // createExternalResourceDescription(
        // MCS06AggregateResource.class,
        // MCS06AggregateResource.PARAM_TERM_SIMILARITY_RESOURCE, createExternalResourceDescription(
        // ResnikRelatednessResource.class,
        // ResnikRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
        // ResnikRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
        // ),
        // MCS06AggregateResource.PARAM_IDF_VALUES_FILE, UTILS_DIR + "/word-idf/" +
        // mode.toString().toLowerCase() + "/" + dataset.toString() + ".txt")),
        // "word-sim",
        // "TWSI_MCS06_Resnik_WordNet"
        // ));
        //
        // // Explicit Semantic Analysis
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // VectorIndexSourceRelatednessResource.class,
        // VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
        // DkproContext.getContext().getWorkspace().getAbsolutePath()
        // + "/ESA/VectorIndexes/wordnet"),
        // Lemma.class.getName() + "/value",
        // false,
        // "esa",
        // "ESA_WordNet"
        // ));
        ////
        // configs.add(new FeatureConfig(
        // createExternalResourceDescription(
        // VectorIndexSourceRelatednessResource.class,
        // VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
        // DkproContext.getContext().getWorkspace().getAbsolutePath()
        // + "/ESA/VectorIndexes/wiktionary_en"),
        // Lemma.class.getName() + "/value",
        // false,
        // "esa",
        // "ESA_Wiktionary"
        // )
        // );
        return configs;
    }

    public static void combineFeatureSets(VariableDisambiguationConstants.Mode mode,
            VariableDisambiguationConstants.Dataset target,
            VariableDisambiguationConstants.Dataset... sources)
        throws IOException
    {
        String outputFolderName = target.toString();

        System.out.println("Combining feature sets");

        // Check if target directory exists. If so, delete it.
        File targetDir = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
                + mode.toString().toLowerCase() + "/" + target.toString());
        if (targetDir.exists()) {
            System.out.println(" - cleaned target directory");
            FileUtils.deleteDirectory(targetDir);
        }

        String featurePathOfFirstSet = VariableDisambiguationConstants.FEATURES_DIR + "/"
                + mode.toString().toLowerCase() + "/" + sources[0].toString();

        Collection<File> features = FileUtils.listFiles(new File(featurePathOfFirstSet),
                new String[] { "txt" }, true);

        for (File feature : features) {
            if (!feature.isDirectory()) {
                // Check that feature exists for all
                boolean shared = true;

                for (int i = 1; i < sources.length; i++) {
                    if (!new File(feature.getAbsolutePath().replace(sources[0].toString(),
                            sources[i].toString())).exists()) {
                        shared = false;
                    }
                }

                if (shared) {
                    System.out.println(" - processing " + feature.getName());

                    String concat = FileUtils.readFileToString(feature);

                    for (int i = 1; i < sources.length; i++) {
                        File nextFile = new File(feature.getAbsolutePath()
                                .replaceAll(sources[0].toString(), sources[i].toString()));

                        concat += FileUtils.readFileToString(nextFile);
                    }

                    File outputFile = new File(feature.getAbsolutePath()
                            .replace(sources[0].toString(), outputFolderName));

                    FileUtils.writeStringToFile(outputFile, concat);
                }
            }
        }

        System.out.println(" - done");
    }
}
