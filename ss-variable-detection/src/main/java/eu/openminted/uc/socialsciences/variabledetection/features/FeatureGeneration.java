package eu.openminted.uc.socialsciences.variabledetection.features;

import static eu.openminted.uc.socialsciences.variabledetection.pipelines.DisambiguationOnlyTrainingPipeline.DATASET_DIR;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceComparator;
import dkpro.similarity.algorithms.lexical.string.LongestCommonSubsequenceNormComparator;
import dkpro.similarity.algorithms.lexical.string.LongestCommonSubstringComparator;
import dkpro.similarity.algorithms.lexical.uima.ngrams.CharacterNGramResource;
import dkpro.similarity.algorithms.lexical.uima.ngrams.WordNGramContainmentResource;
import dkpro.similarity.algorithms.lexical.uima.ngrams.WordNGramJaccardResource;
import dkpro.similarity.algorithms.lexical.uima.string.GreedyStringTilingMeasureResource;
import dkpro.similarity.ml.FeatureConfig;
import dkpro.similarity.ml.filters.LogFilter;
import dkpro.similarity.ml.io.SimilarityScoreWriter;
import dkpro.similarity.uima.annotator.SimilarityScorer;
import dkpro.similarity.uima.api.type.ExperimentalTextSimilarityScore;
import dkpro.similarity.uima.api.type.TextSimilarityScore;
import dkpro.similarity.uima.io.CombinationReader;
import dkpro.similarity.uima.io.CombinationReader.CombinationStrategy;
import dkpro.similarity.uima.resource.SimpleTextSimilarityResource;
import eu.openminted.uc.socialsciences.variabledetection.pipelines.VariableDisambiguationConstants;
import eu.openminted.uc.socialsciences.variabledetection.pipelines.VariableDisambiguationConstants.Dataset;
import eu.openminted.uc.socialsciences.variabledetection.pipelines.VariableDisambiguationConstants.Mode;
import eu.openminted.uc.socialsciences.variabledetection.uima.io.SemEvalCorpusReader;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Pipeline for generating the text similarity features.
 */
public class FeatureGeneration
{
    public static final String MODE_TEMP = "temp";
    private final AnalysisEngine preprocessingStopwordFiltering;
    private final AnalysisEngine preprocessing;
    private final Map<FeatureConfig, AnalysisEngine> engineMap = new HashMap<>();
    private final List<FeatureConfig> featureConfigList;

    public static final int[] CHAR_NGRAMS_N = new int[] { 2, 3, 4 };
    
    public FeatureGeneration() throws Exception
    {
        // Dataset and mode resemble the place where we stored the data *NOT* the semantics of
        // how that data is used here!
        featureConfigList = getFeatureConfigs(Dataset.ALL, Mode.TRAIN);
        
        preprocessingStopwordFiltering = createEngine(preprocessors(true));
        preprocessing = createEngine(preprocessors(false));
        
        for (FeatureConfig config : featureConfigList) {
            System.out.println(config.getMeasureName());

            // Similarity Scorer
            AnalysisEngineDescription scorer = createEngineDescription(
                    SimilarityScorer.class,
                    SimilarityScorer.PARAM_NAME_VIEW_1, CombinationReader.VIEW_1,
                    SimilarityScorer.PARAM_NAME_VIEW_2, CombinationReader.VIEW_2,
                    SimilarityScorer.PARAM_SEGMENT_FEATURE_PATH, config.getSegmentFeaturePath(),
                    SimilarityScorer.PARAM_TEXT_SIMILARITY_RESOURCE, config.getResource());

            AnalysisEngine engine = createEngine(createEngineDescription(scorer));

            engineMap.put(config, engine);
        }
    }

    private static JCas featureJCasStopwordFiltering;
    private static JCas featureJCas;
    
    private static void setupCas(JCas aJCas, String text1, String text2) throws CASException
    {
        JCas view1 = aJCas.createView(CombinationReader.VIEW_1);
        JCas view2 = aJCas.createView(CombinationReader.VIEW_2);

        view1.setDocumentText(text1);
        view1.setDocumentLanguage("en");
        DocumentMetaData metadata = DocumentMetaData.create(view1);
        metadata.setDocumentId("1");

        view2.setDocumentText(text2);
        view2.setDocumentLanguage("en");
        metadata = DocumentMetaData.create(view2);
        metadata.setDocumentId("2");
    }
    
    public synchronized void generateFeaturesAsFiles(String text1, String text2) throws Exception
    {
        if (featureJCas == null) {
            featureJCasStopwordFiltering = JCasFactory.createJCas();
            featureJCas = JCasFactory.createJCas();
        }
        else {
            featureJCasStopwordFiltering.reset();
            featureJCas.reset();
        }
        
        setupCas(featureJCasStopwordFiltering, text1, text2);
        setupCas(featureJCas, text1, text2);
        
        preprocessing.process(featureJCas);
        preprocessingStopwordFiltering.process(featureJCasStopwordFiltering);
        
        for (FeatureConfig config : featureConfigList) {
            File featureDirectory = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
                    + Mode.TEMP.toString().toLowerCase() + "/" + Dataset.TEMP + "/"
                    + config.getTargetPath() + "/");
            featureDirectory.mkdirs();
            
            AnalysisEngine engine = engineMap.get(config);
            engine.process(featureJCas);

            TextSimilarityScore score = selectSingle(featureJCas,
                    ExperimentalTextSimilarityScore.class);
            File outputFile = new File(featureDirectory, config.getMeasureName() + ".txt");

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(Double.toString(score.getScore()));
            }
            
            score.removeFromIndexes();
        }
    }

    public synchronized Instance generateFeatures(String text1, String text2, boolean useLogFilter) throws Exception
    {
        if (featureJCas == null) {
            featureJCasStopwordFiltering = JCasFactory.createJCas();
            featureJCas = JCasFactory.createJCas();
        }
        else {
            featureJCasStopwordFiltering.reset();
            featureJCas.reset();
        }
        
        setupCas(featureJCasStopwordFiltering, text1, text2);
        setupCas(featureJCas, text1, text2);
        
        preprocessing.process(featureJCas);
        preprocessingStopwordFiltering.process(featureJCasStopwordFiltering);

        ArrayList<Attribute> attributes = new ArrayList<>();
        Map<FeatureConfig, Attribute> attrMap = new IdentityHashMap<>();
        for (FeatureConfig config : featureConfigList) {
            Attribute attr = new Attribute(config.getTargetPath() + "/" + config.getMeasureName());
            attributes.add(attr);
            attrMap.put(config, attr);
        }
        
        Attribute dummyGold = new Attribute("gold");
        attributes.add(dummyGold);

        Instances instances = new Instances("temp-relation", attributes, 10);
        Instance instance = new DenseInstance(attributes.size());
        
        for (FeatureConfig config : featureConfigList) {
            AnalysisEngine engine = engineMap.get(config);
            engine.process(featureJCas);

            TextSimilarityScore score = selectSingle(featureJCas,
                    ExperimentalTextSimilarityScore.class);
            score.removeFromIndexes();
            
            // Limit to [0;5] interval
            // See: eu.openminted.uc.socialsciences.variabledetection.util.Features2Arff.toArffString(Collection<File>, InputStream)
            double s = score.getScore();
            if (s < 0.0) {
                s = 0;
            }
            if (s > 5.0) {
                s = 5.0;
            }
            
            instance.setValue(attrMap.get(config), s);
        }
        instance.setValue(dummyGold, 0.0);
        
        instances.add(instance);

        if (useLogFilter) {
            Filter logFilter = new LogFilter();
            logFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, logFilter);
        }

        return instances.get(0);
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
            AnalysisEngineDescription stopw = createEngineDescription(
                    StopWordRemover.class,
                    StopWordRemover. PARAM_MODEL_LOCATION,
                    "classpath:/stopwords/stopwords_english_punctuation.txt");
            builder.add(stopw, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_1);
            builder.add(stopw, CAS.NAME_DEFAULT_SOFA, CombinationReader.VIEW_2);
        }
        
        return builder.createAggregateDescription();
    }
    
    public static void generateFeatures(Dataset target, List<Dataset> datasets, Mode mode) throws Exception
    {
        List<FeatureConfig> configs = getFeatureConfigs(target, mode);

        // Run the pipeline
        for (FeatureConfig config : configs) {
            System.out.println(config.getMeasureName());

            File outputFile = new File(VariableDisambiguationConstants.FEATURES_DIR + "/"
                    + mode.toString().toLowerCase() + "/" + target + "/" + config.getTargetPath()
                    + "/" + config.getMeasureName() + ".txt");

            if (outputFile.exists()) {
                System.out.println(" - skipped, feature already generated");
            }
            else {
                List<String> datasetLocations = new ArrayList<>();
                for (Dataset dataset : datasets) {
                    datasetLocations.add(DATASET_DIR + "/" + mode.toString().toLowerCase()
                            + "/STS.input." + dataset.toString() + ".txt");
                }                
                
                CollectionReaderDescription reader = createReaderDescription(
                        SemEvalCorpusReader.class,
                        SemEvalCorpusReader.PARAM_INPUT_FILES, datasetLocations,
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

    private static List<FeatureConfig> getFeatureConfigs(Dataset dataset, Mode mode)
        throws Exception
    {
        // Define the features
        List<FeatureConfig> configs = new ArrayList<FeatureConfig>();

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

        for (int n : CHAR_NGRAMS_N) {
            configs.add(new FeatureConfig(
                    createExternalResourceDescription(
                            CharacterNGramResource.class,
                            CharacterNGramResource.PARAM_N, Integer.toString(n),
                            CharacterNGramResource.PARAM_IDF_VALUES_FILE,
                            "classpath:/models/character-ngrams-idf/" + mode.toString().toLowerCase()
                                    + "/" + n + "/" + dataset.toString() + ".txt"),
                    null, // not relevant in "text" and "jcas" modes
                    false, 
                    "n-grams", 
                    "CharacterNGramMeasure_" + n));
        }

        for (int n : new int[] { 1, 2 }) {
            configs.add(new FeatureConfig(
                    createExternalResourceDescription(
                            WordNGramContainmentResource.class,
                            WordNGramContainmentResource.PARAM_N, Integer.toString(n)),
                    Token.class.getName(), 
                    true, 
                    "n-grams",
                    "WordNGramContainmentMeasure_" + n + "_stopword-filtered"));
        }

        for (int n : new int[] { 1, 3, 4 }) {
            configs.add(new FeatureConfig(
                    createExternalResourceDescription(
                            WordNGramJaccardResource.class,
                            WordNGramJaccardResource.PARAM_N, Integer.toString(n)),
                    Token.class.getName(), 
                    false, 
                    "n-grams", 
                    "WordNGramJaccardMeasure_" + n));
        }

        for (int n : new int[] { 2, 4 }) {
            configs.add(new FeatureConfig(
                    createExternalResourceDescription(
                            WordNGramJaccardResource.class,
                            WordNGramJaccardResource.PARAM_N, Integer.toString(n)),
                    Token.class.getName(), 
                    true, 
                    "n-grams",
                    "WordNGramJaccardMeasure_" + n + "_stopword-filtered"));
        }

        /*
         * TODO: If you plan to use the following measures, make sure that you have the necessary
         * resources installed. Details on obtaining and installing them can be found here:
         * https://dkpro.github.io/dkpro-similarity/settinguptheresources/
         */
        // Needs [Prerequisites]
        // Resnik word similarity measure, aggregated according to Mihalcea et al. (2006)
        /*
        configs.add(new FeatureConfig(
                createExternalResourceDescription(
                        MCS06AggregateResource.class,
                        MCS06AggregateResource.PARAM_TERM_SIMILARITY_RESOURCE,
                                createExternalResourceDescription(
                                        ResnikRelatednessResource.class,
                                        ResnikRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
                                        ResnikRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"),
                        MCS06AggregateResource.PARAM_IDF_VALUES_FILE,
                        UTILS_DIR + "/word-idf/" + mode.toString().toLowerCase() + "/"
                                + dataset.toString() + ".txt"),
                Lemma.class.getName() + "/value", 
                false, 
                "word-sim", 
                "MCS06_Resnik_WordNet"));
        */
        
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
        
        configs.sort((a, b) -> Comparator.comparing(FeatureConfig::getTargetPath)
                .thenComparing(FeatureConfig::getMeasureName).compare(a, b));
        
        return configs;
    }
}
