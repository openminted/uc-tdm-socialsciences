package eu.openminted.uc.socialsciences.variabledetection.io;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
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
            assertSentence(expectedSentences.get(0), select(jcas, Sentence.class));
            expectedSentences.remove(0);
        }
    }
}
