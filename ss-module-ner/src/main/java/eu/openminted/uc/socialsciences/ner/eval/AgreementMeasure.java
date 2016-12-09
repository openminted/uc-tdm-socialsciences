package eu.openminted.uc.socialsciences.ner.eval;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import webanno.custom.NamedEntity;

import java.util.HashSet;
import java.util.Set;

public class AgreementMeasure {
    /**
     * Number of raters which includes (1) gold-standard and (2) system predictions
     */
    public static final int RATER_COUNT = 2;

    //todo measure this on a collection of documents
    public void Calculate(JCas jcas1, JCas jcas2)
    {
        UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(RATER_COUNT, jcas1.getDocumentText().length());
        final int raterOne = 0;
        final int raterTwo = 1;
        Set<String> coarseCategories = new HashSet<>();
        Set<String> finegrainedCategories = new HashSet<>();

        for (NamedEntity namedEntity : JCasUtil.select(jcas1, NamedEntity.class))
        {
            String category = namedEntity.getValue() + namedEntity.getModifier();
            finegrainedCategories.add(category);
            study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterOne, category);
        }
        for (NamedEntity namedEntity : JCasUtil.select(jcas2, NamedEntity.class))
        {
            String category = namedEntity.getValue() + namedEntity.getModifier();
            study.addUnit(namedEntity.getBegin(), namedEntity.getEnd() - namedEntity.getBegin(), raterTwo, category);
        }

        KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);

        for(String category : finegrainedCategories)
                System.out.printf("Alpha for category %s: %f", category, alpha.calculateCategoryAgreement(category));

        System.out.printf("Overall Alpha: %f", alpha.calculateAgreement());
    }
}
