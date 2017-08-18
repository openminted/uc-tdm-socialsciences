package eu.openminted.uc.socialsciences.variabledetection.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class CsvDatasetConverter
{
    private static final String COLUMN_QUESTION_ENGLISH = "question english";
    private static final String COLUMN_LANGUAGE = "language";
    private static final String COLUMN_REFERENCE_TEXT = "reference-text";

    public static void main(String[] args) throws Exception
    {
        new CsvDatasetConverter().convert(args[0], args[1], args[2]);
    }

    public void convert(String originalDataset, String flatDataset, String scoreFile) throws IOException
    {
        CsvParserSettings settings = new CsvParserSettings();

        settings.setHeaderExtractionEnabled(true);
        // settings.setLineSeparatorDetectionEnabled(true);
        settings.getFormat().setLineSeparator("\r\n");
        settings.setSkipEmptyLines(true);
        settings.getFormat().setDelimiter(';');
        settings.trimValues(true);

        settings.selectFields(COLUMN_QUESTION_ENGLISH, COLUMN_REFERENCE_TEXT, COLUMN_LANGUAGE);

        CsvParser parser = new CsvParser(settings);

        File initialFile = new File(originalDataset);
        parser.beginParsing(initialFile);

        FileWriter fileWriter = new FileWriter(flatDataset);
        FileWriter scoreFileWriter = new FileWriter(scoreFile);

        Record record;
        while ((record = parser.parseNextRecord()) != null) {
            String language = record.getString(COLUMN_LANGUAGE);
            // For now just read English data
            // Later parameterize this
            if (language == null || !language.equals("E"))
                continue;

            String questionEnglish = record.getString(COLUMN_QUESTION_ENGLISH);
            String referenceText = record.getString(COLUMN_REFERENCE_TEXT);

            if (questionEnglish == null || questionEnglish.trim().isEmpty())
                continue;

            if (!questionEnglish.trim().endsWith("."))
                questionEnglish += ".";
            if (!referenceText.trim().endsWith("."))
                referenceText += ".";
            fileWriter.write(questionEnglish + "\t" + referenceText + System.lineSeparator());
            scoreFileWriter.write("1.0" + System.lineSeparator());
        }

        fileWriter.flush();
        fileWriter.close();
        
        scoreFileWriter.flush();
        scoreFileWriter.close();
    }
}