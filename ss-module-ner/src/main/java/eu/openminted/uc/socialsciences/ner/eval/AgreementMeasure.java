package eu.openminted.uc.socialsciences.ner.eval;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import webanno.custom.NamedEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class AgreementMeasure {
    /**
     * Number of raters which includes (1) gold-standard and (2) system predictions
     */
    private static final int RATER_COUNT = 2;

    public static void main(String[] args)
            throws ResourceInitializationException
    {
        List<JCas> goldJcases = AgreementMeasure.getJcases("");
        List<JCas> predictionJcases = AgreementMeasure.getJcases("");

        calculate(goldJcases, predictionJcases);
    }

    public static void calculate(List<JCas> goldJcases, List<JCas> predictionJcases)
    {
        int totalDocumentLength = 0;
        for (JCas jcas:goldJcases)
        {
            totalDocumentLength += jcas.getDocumentText().length();
        }

        UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(RATER_COUNT, totalDocumentLength);
        final int raterOne = 0;
        final int raterTwo = 1;
        Set<String> coarseCategories = new HashSet<>();
        Set<String> finegrainedCategories = new HashSet<>();

        for (JCas jcas:goldJcases)
        {
            for (NamedEntity namedEntity : JCasUtil.select(jcas, NamedEntity.class))
            {
                String category = namedEntity.getValue() + namedEntity.getModifier();
                finegrainedCategories.add(category);
                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterOne, category);
            }
        }

        for (JCas jcas:predictionJcases)
        {
            for (NamedEntity namedEntity : JCasUtil.select(jcas, NamedEntity.class))
            {
                String category = namedEntity.getValue() + namedEntity.getModifier();
                study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterTwo, category);
            }
        }

        KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);

        for(String category : finegrainedCategories)
                System.out.printf("Alpha for category %s: %f", category, alpha.calculateCategoryAgreement(category));

        System.out.printf("Overall Alpha: %f", alpha.calculateAgreement());
    }

    public static List<JCas> getJcases(String documentPathPattern)
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, documentPathPattern);

        List<JCas> result = new ArrayList<>();
        for (JCas jcas : SimplePipeline.iteratePipeline(reader))
        {
            result.add(jcas);
        }

        return result;
    }
}
