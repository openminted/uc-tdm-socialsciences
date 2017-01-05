package eu.openminted.uc.socialsciences.ner.eval;

import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.Map;

import static eu.openminted.uc.socialsciences.ner.eval.AgreementMeasure.calculateAgreement;
import static org.junit.Assert.*;

public class AgreementMeasureTest {
    @Test
    public void calculateAgreementTest() throws Exception {
        String typesystemFile = AgreementMeasure.class.getClassLoader().getResource("typesystem.xml").getFile();
        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(typesystemFile,
                "src/test/resources/evaluation/gold/**/*.xmi", false);
        Map<String, JCas> predictionJcasMap = AgreementMeasure.getJcases(typesystemFile,
                "src/test/resources/evaluation/prediction/**/*.xmi", false);

        calculateAgreement(goldJcasMap, predictionJcasMap);
    }

    @Test
    public void getJcases() throws Exception {
        String typesystemFile = AgreementMeasure.class.getClassLoader().getResource("typesystem.xml").getFile();
        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(typesystemFile,
                "src/test/resources/evaluation/gold/**/*.xmi", false);
        assertEquals(2, goldJcasMap.size());
    }
}