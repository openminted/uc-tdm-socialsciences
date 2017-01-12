package eu.openminted.uc.socialsciences.ner.eval;

import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static eu.openminted.uc.socialsciences.ner.eval.AgreementMeasure.calculateAgreement;
import static org.junit.Assert.*;

public class AgreementMeasureTest {
    private static final String GOLD_DATA_PATTERN = "src/test/resources/evaluation/gold/**/*.xmi";
    private static final String PREDICTION_DATA_PATTERN = "src/test/resources/evaluation/prediction/**/*.xmi";

    @Before
    public void setup()
    {
    }

    @Test
    public void calculateAgreementTest() throws Exception {

        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(GOLD_DATA_PATTERN, false);
        Map<String, JCas> predictionJcasMap = AgreementMeasure.getJcases(PREDICTION_DATA_PATTERN, false);

        for (String key : goldJcasMap.keySet())
        {
            if (predictionJcasMap.containsKey(key))
            {
                calculateAgreement(goldJcasMap.get(key), predictionJcasMap.get(key), key);
            }
        }
    }

    @Test
    public void calculateAgreementTestWithIgnoreDocumentId() throws Exception {

        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(GOLD_DATA_PATTERN, true);
        Map<String, JCas> predictionJcasMap = AgreementMeasure.getJcases(PREDICTION_DATA_PATTERN, true);

        for (String key : goldJcasMap.keySet())
        {
            if (predictionJcasMap.containsKey(key))
            {
                calculateAgreement(goldJcasMap.get(key), predictionJcasMap.get(key), key);
            }
        }
    }

    @Test
    public void getJcases() throws Exception {
        Map<String, JCas> goldJcasMap = AgreementMeasure.getJcases(GOLD_DATA_PATTERN, false);
        assertEquals(2, goldJcasMap.size());
    }
}