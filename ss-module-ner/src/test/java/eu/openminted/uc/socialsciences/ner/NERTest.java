package eu.openminted.uc.socialsciences.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
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
        String variant = "openminted_ss_model.crf";
        String language = "en";
        String testDocument1 = "was reversed during the Vietnam War in the 1960s";
        JCas jcas = runTest(language, variant, testDocument1);

        String[] neArray1 = {
                "[ 24, 35]NamedEntity(OTHevt) (Vietnam War)"};
        AssertAnnotations.assertNamedEntity(neArray1, select(jcas, NamedEntity.class));

        String testDocument2 = "Europeans are leading the way in having already embraced an additional identity.";
        jcas = runTest(language, variant, testDocument2);
        String[] neArray2 = {
                "[  0,  9]NamedEntity(PERgrp) (Europeans)"};
        AssertAnnotations.assertNamedEntity(neArray2, select(jcas, NamedEntity.class));
    }

    @Test
    public void testGerman()
       throws Exception
    {
        String testDocument1 = "verschiedene Workshops an Der Deutschen Bibliothek und der Aufbau eines " +
                "Kompetenznetzwerks zum Thema Langzeitarchivierung,";
        String variant = "openminted_ss_model.crf";
        String language = "de";
        JCas jcas = runTest(language, variant, testDocument1);

        String[] neArray1 = {
                "[ 30, 50]NamedEntity(ORGoth) (Deutschen Bibliothek)"};
        AssertAnnotations.assertNamedEntity(neArray1, select(jcas, NamedEntity.class));

        String testDocument2 = "von den Verfassern zusammen mit Prof. Wolfram Koch von der GDCh durchgeführte";
        jcas = runTest(language, variant, testDocument2);
        String[] neArray2 = {
                "[ 32, 50]NamedEntity(PERind) (Prof. Wolfram Koch)",
                "[ 59, 63]NamedEntity(ORGsci) (GDCh)"};
        AssertAnnotations.assertNamedEntity(neArray2, select(jcas, NamedEntity.class));

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
