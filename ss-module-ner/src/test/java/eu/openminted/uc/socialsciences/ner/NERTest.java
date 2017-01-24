package eu.openminted.uc.socialsciences.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.AssumeResource;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class NERTest
{
    @Test
    public void testEnglish()
       throws Exception
    {
        JCas jcas = runTest("en", "openminted_ss_model.crf", "10 jaar Markus werkzaam bij SAP in Duitsland .");

        String[] ne = {
                "[  8, 14]Person(I-PER) (Markus)",
                "[ 28, 31]Organization(I-ORG) (SAP)",
                "[ 35, 44]Location(I-LOC) (Duitsland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    
    @Test
    public void testGerman()
       throws Exception
    {
        JCas jcas = runTest("de", "openminted_ss_model.crf", "10 jaar Markus werkzaam bij SAP in Duitsland .");

        String[] ne = {
                "[  8, 14]Person(I-PER) (Markus)",
                "[ 28, 31]Organization(I-ORG) (SAP)",
                "[ 35, 44]Location(I-LOC) (Duitsland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AssumeResource.assumeResource(StanfordNamedEntityRecognizer.class, "ner", language,
                variant);

        AnalysisEngine engine = createEngine(StanfordNamedEntityRecognizer.class,
                StanfordNamedEntityRecognizer.PARAM_VARIANT, variant,
                StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
