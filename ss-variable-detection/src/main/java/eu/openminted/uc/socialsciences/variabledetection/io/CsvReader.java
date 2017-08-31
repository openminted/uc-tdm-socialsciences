package eu.openminted.uc.socialsciences.variabledetection.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class CsvReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    private static final String COLUMN_QUESTION_ENGLISH = "question english";
    private static final String COLUMN_LANGUAGE = "language";
    private static final String COLUMN_REFERENCE_TEXT = "reference-text";
    
    private CsvParser parser;
    private boolean goToNextFile = true;
    private Record nextRecord = null;
    private InputStreamReader reader;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        CsvParserSettings settings = new CsvParserSettings();

        settings.setHeaderExtractionEnabled(true);
        // settings.setLineSeparatorDetectionEnabled(true);
        settings.getFormat().setLineSeparator("\r\n");
        settings.setSkipEmptyLines(true);
        settings.getFormat().setDelimiter(';');
        settings.trimValues(true);

        settings.selectFields(COLUMN_QUESTION_ENGLISH, COLUMN_REFERENCE_TEXT, COLUMN_LANGUAGE);
        parser = new CsvParser(settings);
    }
    
    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        if (goToNextFile)
        {
            goToNextFile = false;
            Resource res = nextFile();
            initCas(aJCas, res);
            
            reader = new InputStreamReader(
                        CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()),
                        encoding);
            parser.beginParsing(reader);            
        }
        
        readRecord(aJCas, parser);
        
        //Close the reader after reading is finished
        if (goToNextFile)
            closeQuietly(reader);
    }
    
    private void readRecord(JCas aJCas, CsvParser aParser)
    {
        JCasBuilder doc = new JCasBuilder(aJCas);
        
        String questionEnglish;
        Record record = nextRecord;
        //This is the first record of the file
        if (record == null) {
            record = aParser.parseNextRecord();
            while (true) {
                String language = record.getString(COLUMN_LANGUAGE);
                // TODO add language filter

                questionEnglish = record.getString(COLUMN_QUESTION_ENGLISH);

                if (questionEnglish == null || questionEnglish.trim().isEmpty())
                    record = aParser.parseNextRecord();
                else
                    break;
            }
        }
        else {
            questionEnglish = record.getString(COLUMN_QUESTION_ENGLISH);
        }
        
        Sentence sentence = doc.add(questionEnglish, Sentence.class);
        doc.close();
        
        nextRecord = aParser.parseNextRecord();
        while (nextRecord != null) {
            questionEnglish = nextRecord.getString(COLUMN_QUESTION_ENGLISH);

            if (questionEnglish == null || questionEnglish.trim().isEmpty())
                nextRecord = aParser.parseNextRecord();
            else
                break;
        }
        if (nextRecord == null)
            goToNextFile = true;
    }
    
    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return super.hasNext() || nextRecord != null;
    }
}
