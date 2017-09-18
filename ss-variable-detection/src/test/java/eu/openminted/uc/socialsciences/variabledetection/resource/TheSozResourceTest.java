package eu.openminted.uc.socialsciences.variabledetection.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TheSozResourceTest
{
    private KnowledgeBaseResource thesoz;

    @Before
    public void setup()
    {
        thesoz = new TheSozResource(
                "src/test/resources/thesoz-sample.xml");
    }

    @Test
    public void testContainsConceptLabel()
    {
        Assert.assertTrue(thesoz.containsConceptLabel("abduction"));
        // Check an altLabel
        Assert.assertTrue(thesoz.containsConceptLabel("university drop-out"));
        Assert.assertFalse(thesoz.containsConceptLabel("this should not really be there :)"));
    }

    @Test
    public void testContainsConceptLabelLanguage()
    {
        Assert.assertTrue(thesoz.containsConceptLabel("abduction", "en"));
    }
}
