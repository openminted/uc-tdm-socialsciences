package main;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.NGramIterable;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.textcat.LanguageIdentifier;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import util.convert.Converter;
import util.convert.PDFConverter;

public class Application {

	private static final String STOPWORDS_FILE = "[*]classpath:/stopwords/english.txt";
	private static String path = "R:\\DATA-SETS\\OpenMinTeD\\Variable Extraction\\Corpus\\Articles ISSP\\Articles ISSP Religion Religiosity\\J5KCOR.pdf";

	public static void main(String[] args) {

		/*
		 * Code für Variable Detection, wenn die DB schon steht
		 */

		String doc = getTestDocument(new File(path));

		tokenBasedNGrams(doc, 3);

	}

	private static void tokenBasedNGrams(String doc, int max) {
		JCas jCas;
		try {

			jCas = JCasFactory.createJCas();
			jCas.setDocumentText(doc);
			// jCas.setDocumentLanguage("en");

			final AnalysisEngineDescription langIdent = AnalysisEngineFactory
					.createEngineDescription(LanguageIdentifier.class);

			/*
			 * use the BreakIteratorSegmenter component for tokenization:
			 */
			final AnalysisEngineDescription tokenizer = AnalysisEngineFactory
					.createEngineDescription(BreakIteratorSegmenter.class);

			/*
			 * remove stop words
			 */
			final AnalysisEngineDescription stopwordRemover = AnalysisEngineFactory.createEngineDescription(
					StopWordRemover.class, StopWordRemover.PARAM_MODEL_LOCATION, STOPWORDS_FILE);

			SimplePipeline.runPipeline(jCas, langIdent, tokenizer, stopwordRemover);

			Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);

			/*
			 * Afterwards, jCas contains the sentences and tokens and we can
			 * build the n-grams from theses: NGramIterable‘s factory method
			 * create takes an iterable of tokens and a maximum number for the n
			 * in our n-grams:
			 */
			final Collection<Token> tokens = JCasUtil.select(jCas, Token.class);

			final NGramIterable<Token> ngrams = NGramIterable.create(tokens, max);
			final Iterator<NGram> ngramIterator = ngrams.iterator();

			/*
			 * As with every iterator, we can now use the iterator methods
			 * hasNext and next in order to retrieve the n-grams.
			 */
			while (ngramIterator.hasNext()) {
				final NGram ngram = ngramIterator.next();

				/*
				 * Unfortunately, the iterator will return all n-grams up to a
				 * length of n, i.e., all unigrams/tokens and bigrams. but we
				 * only want the bigrams! We can use a little trick to identify
				 * the bigrams: A bigram always covers exactly two tokens and so
				 * we can use JCasUtil.selectCovered to check how may tokens an
				 * n-gram actually subsumes:
				 *
				 */
				if (JCasUtil.selectCovered(Token.class, ngram).size() == max) {
					System.out.print(ngram.getCoveredText());

					if (ngramIterator.hasNext()) {
						System.out.print(", ");
					}
				}

			}
		} catch (UIMAException e) {
			e.printStackTrace();
		}

	}

	private static String getTestDocument(File doc) {
		String content = PDFConverter.convert(doc, Converter.TIKA);
		return content;
	}

}
