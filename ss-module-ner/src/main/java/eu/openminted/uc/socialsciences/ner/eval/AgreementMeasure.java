package eu.openminted.uc.socialsciences.ner.eval;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import eu.openminted.uc.socialsciences.ner.main.Pipeline;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import webanno.custom.NamedEntity;

import java.util.*;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class AgreementMeasure {
    /**
     * Number of raters which includes (1) gold-standard and (2) system predictions
     */
    private static final int RATER_COUNT = 2;

    public static void main(String[] args)
            throws ResourceInitializationException
    {
        //fixme
        String typesystemFile = "ss-module-ner/src/main/resources/typesystem.xml";

        runTest(typesystemFile);
    }

    protected static void runTest(String typesystemFile) throws ResourceInitializationException {
        //todo test with multiple documents
        List<JCas> goldJcases = AgreementMeasure.getJcases(typesystemFile, "ss-module-ner/src/test/resources/evaluation/gold/de/**/*.xmi");
        List<JCas> predictionJcases = AgreementMeasure.getJcases(typesystemFile, "ss-module-ner/src/test/resources/evaluation/prediction/de/**/*.xmi");

        calculate(goldJcases, predictionJcases);
    }

    public static void calculate(List<JCas> goldJcases, List<JCas> predictionJcases)
    {
        List<UnitizingAnnotationStudy> studyList = new ArrayList<>();
        final int raterOne = 0;
        final int raterTwo = 1;
        Set<String> coarseCategories = new HashSet<>();
        Set<String> finegrainedCategories = new HashSet<>();
        Set<String> predictionCategories = new HashSet<>();

        for (JCas jcas:goldJcases)
        {
            //todo check if reader preserves reading order for both gold and prediction set?
            UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(RATER_COUNT, jcas.getDocumentText().length());
            studyList.add(study);

            for (NamedEntity namedEntity : JCasUtil.select(jcas, NamedEntity.class))
            {
                String category;
                if (namedEntity.getModifier() != null && !namedEntity.getModifier().isEmpty())
                    category = namedEntity.getValue() + namedEntity.getModifier();
                else
                    category = namedEntity.getValue();


                finegrainedCategories.add(category);
                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterOne, category);
            }
        }

        Iterator<UnitizingAnnotationStudy> iterator = studyList.iterator();
        for (JCas jcas:predictionJcases)
        {
            UnitizingAnnotationStudy study = iterator.next();
            for (de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity namedEntity : JCasUtil.select(jcas, de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity.class))
            {
                String category = namedEntity.getValue();
                predictionCategories.add(category);

                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterTwo, category);
            }
        }

        System.out.println("************************");
        for (String set:finegrainedCategories)
            System.out.printf("finegrained category %s %n", set);
        System.out.println("************");
        for (String set:predictionCategories)
            System.out.printf("prediction category %s %n", set);
        System.out.println("************************");

        int docId = 0;
        for (UnitizingAnnotationStudy study : studyList)
        {
            ++docId;
            System.out.printf("%nAgreement scores on file %d %n", docId);
            KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);

            for(String category : finegrainedCategories)
                System.out.printf("Alpha for category %s: %f %n", category, alpha.calculateCategoryAgreement(category));

            System.out.printf("Overall Alpha: %f %n", alpha.calculateAgreement());
        }
    }

    public static List<JCas> getJcases(String typeSystemFile, String documentPathPattern)
            throws ResourceInitializationException
    {
        TypeSystemDescription typeSystemDescription = Pipeline.mergeBuiltInAndCustomTypes(typeSystemFile);
        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                typeSystemDescription,
                XmiReader.PARAM_SOURCE_LOCATION, documentPathPattern);

        List<JCas> result = new ArrayList<>();
        for (JCas jcas : SimplePipeline.iteratePipeline(reader))
        {
            result.add(jcas);
        }

        return result;
    }
}
