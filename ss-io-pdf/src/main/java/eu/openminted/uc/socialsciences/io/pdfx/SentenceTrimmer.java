package eu.openminted.uc.socialsciences.io.pdfx;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * Reads a tab-separated file containing mappings from one token to another. All tokens that match
 * an entry in the first column are changed to the corresponding token in the second column.
 *
 */
public class SentenceTrimmer
        extends JCasTransformerChangeBased_ImplBase
{
    @Override
    public void process(JCas aInput, JCas aOutput)
            throws AnalysisEngineProcessException
    {
        // Processing must be done back-to-front to ensure that offsets for the next token being
        // processed remain valid. If this is done front-to-back, replacing a token with a
        // shorter or longer sequence would cause the offsets to shift.
        Collection<Sentence> sentences = select(aInput, Sentence.class);
        for(Sentence sentence:sentences)
        {
            String trimmedSentence = sentence.getCoveredText().trim();
            if (!trimmedSentence.equals(sentence.getCoveredText()))
            {
                replace(sentence.getBegin(), sentence.getEnd(), trimmedSentence);
            }
        }
    }
}