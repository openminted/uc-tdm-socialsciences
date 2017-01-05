package eu.openminted.uc.socialsciences.ner.eval;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import eu.openminted.uc.socialsciences.ner.main.Pipeline;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCopier;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import webanno.custom.NamedEntity;

import java.util.*;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * @implNote When argument ignoreDocumentId is set to false (default value) for each Gold-document there should be a
 * Prediction-document in the prediction set with identical documentId (cf. documentId attribute in xmi file). If
 * this requirement is not satisfied, #AgreementMeasure.{@link #calculateAgreement(Map, Map)} method will produce an
 * error.
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

        String typeSystemFileName = "typesystem.xml";
        String typesystemFile;
        try
        {
            typesystemFile = AgreementMeasure.class.getClassLoader().getResource(typeSystemFileName).getFile();
        }catch (NullPointerException x)
        {
            logger.error("Type system file [" + typeSystemFileName + "] could not be found on classpath!");
            throw new IllegalStateException(x);
        }

        String goldDocumentPathPattern = args[0];
        String predictionDocumentPathPattern = args[1];
        boolean ignoreDocumentId = false;
        if (args.length == 3)
            ignoreDocumentId = Boolean.parseBoolean(args[2]);

        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(typesystemFile,
                goldDocumentPathPattern, ignoreDocumentId);
        Map<String, JCas> predictionJcasMap = AgreementMeasure.getJcases(typesystemFile,
                predictionDocumentPathPattern, ignoreDocumentId);

        calculateAgreement(goldJcasMap, predictionJcasMap);
    }

    private static void printUsage() {
        System.out.printf("Please run the program with the following arguments: %n" +
                "\t[arg1] input pattern for gold data%n" +
                "\t[arg2] input pattern for prediction data%n" +
                "\t[arg3] [optional] ignoreDocumentId flag. If set to false (default value) for each Gold-document " +
                "there should be a Prediction-document in the prediction set with identical documentId " +
                "(cf. documentId attribute in xmi file). If this requirement is not satisfied, program will produce an error.");
    }

    public static void calculateAgreement(Map<String, JCas> goldJcasMap, Map<String, JCas> predictionJcasMap)
    {
        Map<String, UnitizingAnnotationStudy> studyList = new HashMap<>();
        final int raterOne = 0;
        final int raterTwo = 1;

        Map<String, Set<String>> goldCategories = new HashMap<>();
        Map<String, Set<String>> predictionCategories = new HashMap<>();

        for (String key:goldJcasMap.keySet())
        {
            JCas jcas = goldJcasMap.get(key);
            UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(RATER_COUNT, jcas.getDocumentText().length());
            studyList.put(key, study);
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
                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterOne, category);
            }
        }

        for (String key:predictionJcasMap.keySet())
        {
            JCas jcas = predictionJcasMap.get(key);
            //todo handle case when key doesn't exist because of non-matching document id
            UnitizingAnnotationStudy study = studyList.get(key);
            Set<String> currentPredictionCategories = new HashSet<>();
            predictionCategories.put(key, currentPredictionCategories);

            for (de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity namedEntity :
                    JCasUtil.select(jcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class))
            {
                String category = namedEntity.getValue();
                currentPredictionCategories.add(category);

                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterTwo, category);
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
        for (String key : studyList.keySet())
        {
            UnitizingAnnotationStudy study = studyList.get(key);


            ++docId;
            System.out.printf("%nAgreement scores on file %d [%s] %n", docId, key);
            KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);

            for(String category : goldCategories.get(key))
                System.out.printf("Alpha for category %s: %f %n", category, alpha.calculateCategoryAgreement(category));

            System.out.printf("Overall Alpha: %f %n", alpha.calculateAgreement());
        }
    }

    public static Map<String, JCas> getJcases(String typeSystemFile, String documentPathPattern, boolean ignoreDocumentId)
            throws ResourceInitializationException
    {
        TypeSystemDescription typeSystemDescription = Pipeline.mergeBuiltInAndCustomTypes(typeSystemFile);
        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                typeSystemDescription,
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
                myjcas = JCasFactory.createJCas(typeSystemDescription);
            } catch (UIMAException e) {
                logger.error("An error occurred while trying to create a new JCas from type system file ["
                        + typeSystemFile + "]", e);
                throw new IllegalStateException(e);
            }

            CasCopier.copyCas(jcas.getCas(), myjcas.getCas(), true);
            result.put(id, myjcas);
        }

        return result;
    }
}
