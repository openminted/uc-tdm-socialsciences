package eu.openminted.uc.socialsciences.variabledetection.eval;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import eu.openminted.uc.socialsciences.annotation.VariableMention;
import eu.openminted.uc.socialsciences.common.evaluation.FMeasure;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;

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

    private String inputGold;
    private String inputPrediction;
    private boolean strictId = false;
    private boolean verbose = false;
    private FMeasure fMeasure = new FMeasure();

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
        inputGold = args[0];
        inputPrediction = args[1];
//        strictId = true;
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
                System.out.printf("%nCalculating precision/recall scores for doc [%s]%n", key);
                calculatePrecision(goldJcasMap.get(key), predictionJcasMap.get(key), verbose);
            }
        }
    }

    public void calculatePrecision(JCas goldJcas, JCas predictionJcas, boolean verbose)
    {
        List<VariableAnnotation> goldAnnotations = new ArrayList<>();
        List<VariableAnnotation> predictedAnnotations = new ArrayList<>();

//        IobEncoder myIobEncoder = new IobEncoder(goldJcas.getCas(), neType, neValue); 
        for (VariableMention mention : JCasUtil.select(goldJcas, VariableMention.class)) {
            if (mention.getCorrect().equals("Yes"))
                goldAnnotations.add(new VariableAnnotation(mention.getVariableId(), mention.getBegin(), mention.getEnd()));
        }

        for (VariableMention mention : JCasUtil.select(predictionJcas, VariableMention.class)) {
            if (mention.getCorrect().equals("Yes"))
                predictedAnnotations.add(new VariableAnnotation(mention.getVariableId(), mention.getBegin(), mention.getEnd()));
        }

        
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
                XmiReader.PARAM_PATTERNS, "[+]**/*.xmi",
                XmiReader.PARAM_LENIENT, false);

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

    private static class VariableAnnotation {
        private final String id;
        private final int begin;
        private final int length;

        VariableAnnotation(String aId, int begin, int length)
        {
            this.id = aId;
            this.begin = begin;
            this.length = length;
        }

        public String getType()
        {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VariableAnnotation that = (VariableAnnotation) o;
            return begin == that.begin &&
                    length == that.length &&
                    Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, begin, length);
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
