package eu.openminted.uc.socialsciences.ner.eval;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import eu.openminted.uc.socialsciences.ner.util.MyIobEncoder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.kohsuke.args4j.Option;
import webanno.custom.NamedEntity;

import java.util.*;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * Class for evaluating system performance
 *
 * <b>Note:</b> When argument strictId is set to false (default value) for each Gold-document there should be a
 * Prediction-document in the prediction set with identical documentId (cf. documentId attribute in xmi file). If
 * this requirement is not satisfied, #PerformanceMeasure.{@link #main(String[])} method will not work
 * properly.
 */
public class PerformanceMeasure {
    private static final Logger logger = LogManager.getLogger(PerformanceMeasure.class);

    /**
     * Number of raters which includes (1) gold-standard and (2) system predictions
     */
    private static final int RATER_COUNT = 2;

    @Option(name = "-iGold", usage = "input pattern for gold data", required = true)
    private String inputGold;

    @Option(name = "-iPred", usage = "input pattern for prediction data", required = true)
    private String inputPrediction;

    @Option(name = "-strictId", usage = "[optional] strictId flag. If set for each Gold-document " +
            "there should be a Prediction-document in the prediction set with identical documentId " +
            "(cf. documentId attribute in xmi file). If this requirement is not satisfied, program will not work properly.")
    private boolean strictId = false;

    @Option(name = "-v", usage = "[optional] verbose output flag. If this flag is set, output will " +
            "contain comprehensive information about tags found in gold and prediction sets.")
    private boolean verbose = false;

    public static void main(String[] args)
            throws ResourceInitializationException
    {
        new PerformanceMeasure().run(args);
    }

    public void run()
            throws ResourceInitializationException
    {
        assertFields();
        runInternal();
    }

    private void assertFields() {
        if(inputGold==null)
            throw new IllegalArgumentException("inputGold can not be null.");
        if(inputPrediction==null)
            throw new IllegalArgumentException("InputPrediction can not be null.");
    }

    private void run(String[] args)
            throws ResourceInitializationException
    {
        new CommandLineArgumentHandler().parseInput(args, this);

        runInternal();
    }

    private void runInternal() throws ResourceInitializationException {
        logger.info(String.format("Gold path: %s", inputGold));
        Map<String, JCas> goldJcasMap = PerformanceMeasure.getJcases(
                inputGold, strictId);
        logger.info(String.format("Found [%d] documents in gold document path.", goldJcasMap.size()));

        logger.info(String.format("Prediction path: %s", inputPrediction));
        Map<String, JCas> predictionJcasMap = PerformanceMeasure.getJcases(
                inputPrediction, strictId);
        logger.info(String.format("Found [%d] documents in prediction document path.", predictionJcasMap.size()));


        for (String key : goldJcasMap.keySet())
        {
            if (!predictionJcasMap.containsKey(key))
            {
                logger.error("Couldn't find document [" + key + "] in prediction set.");
            } else
            {
                System.out.printf("%nCalculating agreement scores for doc [%s]%n", key);
                calculateAgreement(goldJcasMap.get(key), predictionJcasMap.get(key), key, verbose);

                System.out.printf("%nCalculating precision/recall scores for doc [%s]%n", key);
                calculatePrecision(goldJcasMap.get(key), predictionJcasMap.get(key), verbose);
            }
        }
    }

    public static void calculateAgreement(JCas goldJcas, JCas predictionJcas, String docId, boolean verbose)
    {
        final int raterOne = 0;
        final int raterTwo = 1;

        UnitizingAnnotationStudy unitizingStudy = new UnitizingAnnotationStudy(RATER_COUNT, goldJcas.getDocumentText().length());

        Set<String> currentGoldCategories = new HashSet<>();

        for (NamedEntity namedEntity : JCasUtil.select(goldJcas, NamedEntity.class)) {
            String category;
            if (namedEntity.getValue() == null)
            {
                logger.warn(String.format("found a named entity with null value at (begin, end)=(%d, %d)! Will skip this annotation..."
                        , namedEntity.getBegin(), namedEntity.getEnd()));
                continue;
            }
            if (namedEntity.getModifier() != null)
                category = namedEntity.getValue() + namedEntity.getModifier();
            else
                category = namedEntity.getValue();

            currentGoldCategories.add(category);
            int begin = namedEntity.getBegin();
            int length = namedEntity.getEnd() - begin;
            unitizingStudy.addUnit(begin, length, raterOne, category);
        }

        Set<String> currentPredictionCategories = new HashSet<>();

        for (de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity namedEntity :
                JCasUtil.select(predictionJcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class)) {
            String category = namedEntity.getValue();
            currentPredictionCategories.add(category);

            int begin = namedEntity.getBegin();
            int length = namedEntity.getEnd() - begin;
            unitizingStudy.addUnit(begin, length, raterTwo, category);
        }

        if (verbose)
        {
            System.out.println("************************");
            System.out.printf("gold categories in document %s %n", docId);
            for (String set : currentGoldCategories)
                System.out.printf("\t%s %n", set);
            System.out.println("************");
            System.out.printf("prediction categories in document %s %n", docId);
            for (String set:currentPredictionCategories)
                System.out.printf("\t%s %n", set);
            System.out.printf("************************%n%n");
        }

        System.out.printf("Agreement scores on file [%s] %n", docId);
        KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(unitizingStudy);

        for(String category : currentGoldCategories)
            if (currentPredictionCategories.contains(category))
                System.out.printf("\t-\tAlpha for category %s: %f %n", category, alpha.calculateCategoryAgreement(category));
            else
                System.out.printf("\t-\tCategory %s doesn't exist in prediction data! %n", category);

        System.out.printf("\tOverall Alpha: %f %n", alpha.calculateAgreement());
    }

    public static void calculatePrecision(JCas goldJcas, JCas predictionJcas, boolean verbose)
    {
        List<MyAnnotation> goldAnnotations = new ArrayList<>();
        List<MyAnnotation> predictedAnnotations = new ArrayList<>();

        Type neType = JCasUtil.getType(goldJcas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        Feature neModifier = neType.getFeatureByBaseName("modifier");

        MyIobEncoder myIobEncoder = new MyIobEncoder(goldJcas.getCas(), neType, neValue, neModifier, true);
        for (Token token : JCasUtil.select(goldJcas, Token.class)) {
            MyAnnotation annotation = new MyAnnotation(myIobEncoder.encode(token), token.getBegin(), token.getEnd());
            if (!annotation.getType().equalsIgnoreCase("O"))
                goldAnnotations.add(annotation);
        }

        Type dkproNamedEntityType = JCasUtil.getType(predictionJcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class);
        Feature dkproNamedEntityValue = dkproNamedEntityType.getFeatureByBaseName("value");
        IobEncoder iobEncoder = new IobEncoder(predictionJcas.getCas(), dkproNamedEntityType, dkproNamedEntityValue);
        for (Token token : JCasUtil.select(predictionJcas, Token.class)) {
            MyAnnotation annotation = new MyAnnotation(iobEncoder.encode(token), token.getBegin(), token.getEnd());
            if (!annotation.getType().equalsIgnoreCase("O"))
                predictedAnnotations.add(annotation);
        }

        FMeasure fMeasure = new FMeasure();
        int hitCount = fMeasure.process(goldAnnotations, predictedAnnotations);
        if (verbose)
        {
            System.out.printf("Total annotations in gold file: %d%n", goldAnnotations.size());
            System.out.printf("Intersection of annotations found in gold and prediction file: %d%n", hitCount);
        }

        System.out.printf("FMeasure scores%n");
        System.out.printf("\tOverall precision: %f %n", fMeasure.getPrecision());
        System.out.printf("\tOverall recall: %f %n", fMeasure.getRecall());
        System.out.printf("\tOverall F-Measure: %f %n", fMeasure.getFMeasure());
    }

    public static Map<String, JCas> getJcases(String documentPathPattern, boolean strictId)
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, documentPathPattern,
                XmiReader.PARAM_LENIENT, true);

        Map<String, JCas> result = new HashMap<>();
        int count = 0;
        for (JCas jcas : SimplePipeline.iteratePipeline(reader))
        {
            ++count;
            String id;
            if (!strictId)
            {
                id = Integer.toString(count);
            } else
            {
                DocumentMetaData metadata = DocumentMetaData.get(jcas);
                id = metadata.getDocumentId();
            }

            JCas myjcas;
            try {
                myjcas = JCasFactory.createJCas();
            } catch (UIMAException e) {
                logger.error("An error occurred while trying to create a new JCas", e);
                throw new IllegalStateException(e);
            }

            CasCopier.copyCas(jcas.getCas(), myjcas.getCas(), true);
            result.put(id, myjcas);
        }

        return result;
    }

    private static class MyAnnotation {
        private final String type;
        private final int begin;
        private final int length;

        MyAnnotation(String type, int begin, int length)
        {
            this.type = type;
            this.begin = begin;
            this.length = length;
        }

        public String getType()
        {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyAnnotation that = (MyAnnotation) o;
            return begin == that.begin &&
                    length == that.length &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, begin, length);
        }
    }

    public void setInputGold(String inputGold)
    {
        this.inputGold = inputGold;
    }
    public String getInputGold()
    {
        return inputGold;
    }
    public void setInputPrediction(String inputPrediction)
    {
        this.inputPrediction = inputPrediction;
    }
    public String getInputPrediction()
    {
        return inputPrediction;
    }
    public void setStrictId(boolean value)
    {
        strictId = value;
    }
    public boolean isStrictId()
    {
        return strictId;
    }
    public void setVerbose(boolean value)
    {
        this.verbose = value;
    }
    public boolean isVerbose()
    {
        return verbose;
    }
}
