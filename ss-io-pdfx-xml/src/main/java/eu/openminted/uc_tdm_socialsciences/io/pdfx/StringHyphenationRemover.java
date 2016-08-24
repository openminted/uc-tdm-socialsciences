package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.regex.Pattern;

import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.JCasIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public class StringHyphenationRemover {
    public static final String WORD_DICTIONARY_PATH_DEFAULT = PdfxXmlToXmiConverter.class.getClassLoader().
            getResource("german-words-dictionary.txt").getFile();
    private String wordDictionaryPath = WORD_DICTIONARY_PATH_DEFAULT;

    //fixme
    public static final String DOCUMENT_LANGUAGE_ENGLISH = "de";
    private String documentLanguage = DOCUMENT_LANGUAGE_ENGLISH;

    protected AnalysisEngineDescription hyphenationRemoverEngine;
    protected AnalysisEngine engine;

    public StringHyphenationRemover()
            throws ResourceInitializationException {
        // Set up engine
        hyphenationRemoverEngine = createEngineDescription(
                HyphenationRemover.class,
                HyphenationRemover.PARAM_MODEL_LOCATION, WORD_DICTIONARY_PATH_DEFAULT,
                HyphenationRemover.PARAM_MODEL_ENCODING, "utf8");
        engine = createEngine(hyphenationRemoverEngine);
    }

    public String process(String text) throws AnalysisEngineProcessException {
        JCas jcas = null;
        JCasIterator iterator = null;

        try {
            jcas = engine.newJCas();
            jcas.setDocumentLanguage(DOCUMENT_LANGUAGE_ENGLISH);
            jcas.setDocumentText(text);
            iterator = engine.processAndOutputNewCASes(jcas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (iterator != null && iterator.hasNext())
            return iterator.next().getDocumentText();
        else
            return "";
    }

    public String getDocumentLanguage() {
        return documentLanguage;
    }

    public void setDocumentLanguage(String documentLanguage) {
        this.documentLanguage = documentLanguage;
    }

    public String getWordDictionaryPath() {
        return wordDictionaryPath;
    }

    public void setWordDictionaryPath(String wordDictionaryPath) {
        this.wordDictionaryPath = wordDictionaryPath;
    }
}
