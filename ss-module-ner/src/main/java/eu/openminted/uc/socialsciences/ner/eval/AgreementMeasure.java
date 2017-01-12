package eu.openminted.uc.socialsciences.ner.eval;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.eval.measure.FMeasure;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import eu.openminted.uc.socialsciences.ner.helper.util.MyIobEncoder;
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
import webanno.custom.NamedEntity;

import java.util.*;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * @implNote When argument ignoreDocumentId is set to false (default value) for each Gold-document there should be a
 * Prediction-document in the prediction set with identical documentId (cf. documentId attribute in xmi file). If
 * this requirement is not satisfied, #AgreementMeasure.{@link #calculateAgreement(Map, Map)} method will not work
 * properly.
 */
public class AgreementMeasure {
    private static final Logger logger = Logger.getLogger(AgreementMeasure.class);

    /**
     * Number of raters which includes (1) gold-standard and (2) system predictions
     */
    private static final int RATER_COUNT = 2;

    public static void main(String[] args)
            throws ResourceInitializationException
    {
        if (args.length < 2)
        {
            printUsage();
            System.exit(1);
        }

        String goldDocumentPathPattern = args[0];
        String predictionDocumentPathPattern = args[1];
        boolean ignoreDocumentId = true;
        if (args.length == 3)
            ignoreDocumentId = Boolean.parseBoolean(args[2]);

        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(
                goldDocumentPathPattern, ignoreDocumentId);
        logger.info("Found [" + goldJcasMap.size() + "] documents in gold document path.");
        Map<String, JCas> predictionJcasMap = AgreementMeasure.getJcases(
                predictionDocumentPathPattern, ignoreDocumentId);
        logger.info("Found [" + predictionJcasMap.size() + "] documents in prediction document path.");

        calculateAgreement(goldJcasMap, predictionJcasMap);

        for (String key : goldJcasMap.keySet())
        {
            if (!predictionJcasMap.containsKey(key))
            {
                logger.error("Couldn't find document [" + key + "] in prediction set.");
            } else
            {
                System.out.printf("Calculating precision/recall info for doc [%s]%n", key);
                calculatePrecision(goldJcasMap.get(key), predictionJcasMap.get(key));
            }
        }
    }

    private static void printUsage() {
        System.out.printf("Please run the program with the following arguments: %n" +
                "\t[arg1] input pattern for gold data%n" +
                "\t[arg2] input pattern for prediction data%n" +
                "\t[arg3] [optional] ignoreDocumentId flag. If set to true for each Gold-document " +
                "there should be a Prediction-document in the prediction set with identical documentId " +
                "(cf. documentId attribute in xmi file). If this requirement is not satisfied, program will not work properly.");
    }

    public static void calculateAgreement(Map<String, JCas> goldJcasMap, Map<String, JCas> predictionJcasMap)
    {
        Map<String, UnitizingAnnotationStudy> unitizingStudyMap = new HashMap<>();
        final int raterOne = 0;
        final int raterTwo = 1;

        Map<String, Set<String>> goldCategories = new HashMap<>();
        Map<String, Set<String>> predictionCategories = new HashMap<>();

        for (String key:goldJcasMap.keySet())
        {
            JCas jcas = goldJcasMap.get(key);

            UnitizingAnnotationStudy unitizingStudy = new UnitizingAnnotationStudy(RATER_COUNT, jcas.getDocumentText().length());
            unitizingStudyMap.put(key, unitizingStudy);

            Set<String> currentGoldCategories = new HashSet<>();
            goldCategories.put(key, currentGoldCategories);

            for (NamedEntity namedEntity : JCasUtil.select(jcas, NamedEntity.class))
            {
                String category;
                if (namedEntity.getModifier() != null)
                    category = namedEntity.getValue() + namedEntity.getModifier();
                else
                    category = namedEntity.getValue();

                currentGoldCategories.add(category);
                int begin = namedEntity.getBegin();
                int length = namedEntity.getEnd() - begin;
                unitizingStudy.addUnit(begin, length, raterOne, category);
            }
        }

        for (String key:predictionJcasMap.keySet())
        {
            JCas jcas = predictionJcasMap.get(key);
            if(!unitizingStudyMap.containsKey(key))
            {
                logger.error("Gold set does not contain id [" + key + "] which was found in prediction set. " +
                        "Program will skip this document.");
                continue;
            }
            UnitizingAnnotationStudy study = unitizingStudyMap.get(key);
            Set<String> currentPredictionCategories = new HashSet<>();
            predictionCategories.put(key, currentPredictionCategories);

            for (de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity namedEntity :
                    JCasUtil.select(jcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class))
            {
                String category = namedEntity.getValue();
                currentPredictionCategories.add(category);

                int begin = namedEntity.getBegin();
                int length = namedEntity.getEnd() - begin;
                study.addUnit(begin, length, raterTwo, category);
            }
        }

        System.out.println("************************");
        for (String key:goldCategories.keySet())
        {
            System.out.printf("gold categories in document %s %n", key);

            for (String set:goldCategories.get(key))
                System.out.printf("\t%s %n", set);
            System.out.println("************");
        }
        System.out.println("************************");
        for (String key:predictionCategories.keySet())
        {
            System.out.printf("prediction categories in document %s %n", key);
            for (String set:predictionCategories.get(key))
                System.out.printf("\t%s %n", set);
            System.out.println("************");
        }
        System.out.println("************************");

        int docId = 0;
        for (String key : unitizingStudyMap.keySet())
        {
            UnitizingAnnotationStudy study = unitizingStudyMap.get(key);
//            CodingAnnotationStudy codingStudy = codingStudyMap.get(key);

            ++docId;
            System.out.printf("%nAgreement scores on file %d [%s] %n", docId, key);
            KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);
//            PercentageAgreement pa = new PercentageAgreement(codingStudy);

            for(String category : goldCategories.get(key))
                System.out.printf("\t\tAlpha for category %s: %f %n", category, alpha.calculateCategoryAgreement(category));

            System.out.printf("\tOverall Alpha: %f %n", alpha.calculateAgreement());
//            System.out.printf("Overall Percentage agreement: %f %n", pa.calculateAgreement());
        }
    }

    public static void calculatePrecision(JCas goldJcas, JCas predictionJcas)
    {
        List<MyAnnotation> goldAnnotations = new ArrayList<>();
        List<MyAnnotation> predictedAnnotations = new ArrayList<>();

        Type neType = JCasUtil.getType(goldJcas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        Feature neModifier = neType.getFeatureByBaseName("modifier");

        MyIobEncoder myIobEncoder = new MyIobEncoder(goldJcas.getCas(), neType, neValue, neModifier, true);
        for (Token token : JCasUtil.select(goldJcas, Token.class)) {
            MyAnnotation annotation = new MyAnnotation(myIobEncoder.encode(token), token.getBegin(), token.getEnd());
            goldAnnotations.add(annotation);
        }

        Type dkproNamedEntityType = JCasUtil.getType(predictionJcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class);
        Feature dkproNamedEntityValue = dkproNamedEntityType.getFeatureByBaseName("value");
        IobEncoder iobEncoder = new IobEncoder(predictionJcas.getCas(), dkproNamedEntityType, dkproNamedEntityValue);
        for (Token token : JCasUtil.select(predictionJcas, Token.class)) {
            MyAnnotation annotation = new MyAnnotation(iobEncoder.encode(token), token.getBegin(), token.getEnd());
            predictedAnnotations.add(annotation);
        }

        FMeasure fMeasure = new FMeasure();
        int hitCount = fMeasure.process(goldAnnotations, predictedAnnotations);

        System.out.printf("%nFMeasure scores%n");
        System.out.printf("\tOverall precision: %f %n", fMeasure.getPrecision());
        System.out.printf("\tOverall recall: %f %n", fMeasure.getRecall());
        System.out.printf("\tOverall F-Measure: %f %n", fMeasure.getFMeasure());
    }

    public static Map<String, JCas> getJcases(String documentPathPattern, boolean ignoreDocumentId)
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, documentPathPattern);

        Map<String, JCas> result = new HashMap<>();
        int count = 0;
        for (JCas jcas : SimplePipeline.iteratePipeline(reader))
        {
            ++count;
            String id;
            if (ignoreDocumentId)
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
}
