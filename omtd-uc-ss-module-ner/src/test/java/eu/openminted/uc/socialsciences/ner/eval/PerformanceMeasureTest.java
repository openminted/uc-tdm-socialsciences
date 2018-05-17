package eu.openminted.uc.socialsciences.ner.eval;

import static eu.openminted.uc.socialsciences.ner.eval.PerformanceMeasure.calculateAgreement;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

public class PerformanceMeasureTest {
    private static final String GOLD_DATA_PATTERN = "src/test/resources/evaluation/gold/**/*.xmi";
    private static final String PREDICTION_DATA_PATTERN = "src/test/resources/evaluation/prediction/**/*.xmi";

    @Before
    public void setup()
    {
    }

    @Test
    public void calculateAgreementTest() throws Exception {

        Map<String, JCas> goldJcasMap = PerformanceMeasure.getJcases(GOLD_DATA_PATTERN, false);
        Map<String, JCas> predictionJcasMap = PerformanceMeasure.getJcases(PREDICTION_DATA_PATTERN, false);

        for (String key : goldJcasMap.keySet())
        {
            if (predictionJcasMap.containsKey(key))
            {
                calculateAgreement(goldJcasMap.get(key), predictionJcasMap.get(key), key, true);
            }
        }
    }

    @Test
    public void calculatePrecisionTest() throws Exception {

        Map<String, JCas> goldJcasMap = PerformanceMeasure.getJcases(GOLD_DATA_PATTERN, false);
        Map<String, JCas> predictionJcasMap = PerformanceMeasure.getJcases(PREDICTION_DATA_PATTERN, false);
        PerformanceMeasure measure = new PerformanceMeasure();

        for (String key : goldJcasMap.keySet())
        {
            if (predictionJcasMap.containsKey(key))
            {
                measure.calculatePrecision(goldJcasMap.get(key), predictionJcasMap.get(key), true);
            }
        }
    }

    @Test
    public void calculateAgreementTestWithIgnoreDocumentId() throws Exception {

        Map<String, JCas> goldJcasMap = PerformanceMeasure.getJcases(GOLD_DATA_PATTERN, true);
        Map<String, JCas> predictionJcasMap = PerformanceMeasure.getJcases(PREDICTION_DATA_PATTERN, true);

        for (String key : goldJcasMap.keySet())
        {
            if (predictionJcasMap.containsKey(key))
            {
                calculateAgreement(goldJcasMap.get(key), predictionJcasMap.get(key), key, true);
            }
        }
    }

    @Test
    public void getJcases() throws Exception {
        Map<String, JCas> goldJcasMap = PerformanceMeasure.getJcases(GOLD_DATA_PATTERN, false);
        assertEquals(2, goldJcasMap.size());
    }
}