package eu.openminted.uc.socialsciences.variabledetection.io;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.asCopyableString;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.JCasIterator;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class CsvReaderTest
{
    @Test
    public void csvReaderTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                CsvReader.class, 
                CsvReader.PARAM_LANGUAGE, "en",
                CsvReader.PARAM_SOURCE_LOCATION, "src/test/resources/csv/", 
                CsvReader.PARAM_PATTERNS, "allbus.test.en.csv"
        );
        
        List<String[]> expectedSentences = new ArrayList<>();
        expectedSentences.add(new String[] {"What citizenship do you have? If you have several "
                + "citizenships, please name all of them."});
        expectedSentences.add(new String[] {"Have you had German citizenship since birth?"});
        expectedSentences.add(new String[] {"Were you born within the current borders of Germany?"});        
        
        JCasIterator jcasIterator = new JCasIterable(reader).iterator();
        while (jcasIterator.hasNext())
        {
            JCas jcas = jcasIterator.next();
            int index = find(expectedSentences, select(jcas, Sentence.class));
            assertTrue(index >= 0);            
            expectedSentences.remove(index);
        }
    }

    private int find(List<String[]> expectedSentences, Collection<Sentence> aActual)
    {   
        int index = 0;
        boolean found = false;
        for (String[] sentences : expectedSentences)
        {
            List<String> expected = asList(sentences);
            List<String> actual = toText(aActual);

            System.out.printf("%-20s - Expected: %s%n", "Sentences", asCopyableString(expected, false));
            System.out.printf("%-20s - Actual  : %s%n", "Sentences", asCopyableString(actual, false));

            if (asCopyableString(expected, true).equals(asCopyableString(actual, true)))
            {
                found = true;
                break;
            }
            ++index;
        }        
        
        if (found)
            return index;
        else
            return -1;
    }
}
