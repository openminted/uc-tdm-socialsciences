package main;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.textcat.LanguageIdentifier;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.TokenCaseTransformer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import util.convert.Converter;
import util.convert.PDFConverter;

public class Application {

	private static final String STOPWORDS_FILE = "[*]classpath:/stopwords/english.txt";
	private static String path = "R:\\DATA-SETS\\OpenMinTeD\\Variable Extraction\\Corpus\\Articles ISSP\\Articles ISSP Religion Religiosity\\08RWGE.pdf";

	public static void main(String[] args) {

		/*
		 * Code für Variable Detection, wenn die DB schon steht
		 *
		 * TODO: get ngrams for variables from associated studies (get them from
		 * db)
		 */

		String doc = getTestDocument(new File(path));

		tokenBasedNGrams(doc, 2);

	}

	/*
	 * TODO: get character n-grams
	 */
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
			 * remove stop words TODO model??
			 */
			final AnalysisEngineDescription hyphenRemover = AnalysisEngineFactory
					.createEngineDescription(HyphenationRemover.class, HyphenationRemover.PARAM_MODEL_LOCATION, "?");

			/*
			 * remove stop words
			 */
			final AnalysisEngineDescription stopwordRemover = AnalysisEngineFactory.createEngineDescription(
					StopWordRemover.class, StopWordRemover.PARAM_MODEL_LOCATION, STOPWORDS_FILE);

			/*
			 * normalize case
			 */
			final AnalysisEngineDescription tokenCaseNormalizer = AnalysisEngineFactory
					.createEngineDescription(TokenCaseTransformer.class, TokenCaseTransformer.PARAM_CASE, "NORMALCASE");

			/*
			 * stem
			 */
			final AnalysisEngineDescription stemmer = AnalysisEngineFactory
					.createEngineDescription(SnowballStemmer.class, SnowballStemmer.PARAM_LOWER_CASE, true);

			SimplePipeline.runPipeline(jCas, langIdent,
					tokenizer/* , tokenCaseNormalizer , hyphenRemover */, stopwordRemover, stemmer);

			Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
			for (Sentence sentence : sentences) {
				System.out.println(sentence.getCoveredText() + "\n\n");
			}

			/*
			 * Afterwards, jCas contains the sentences and tokens and we can
			 * build the n-grams from theses: NGramIterable‘s factory method
			 * create takes an iterable of tokens and a maximum number for the n
			 * in our n-grams:
			 */
			final Collection<Token> tokens = JCasUtil.select(jCas, Token.class);

			final NGramIterable<Token> ngrams = NGramIterable.create(tokens, max);

			// for(NGram n : ngrams) {
			//
			// }

			final Iterator<NGram> ngramIterator = ngrams.iterator();

			Set<String> result = new HashSet<>();
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
					String ngramText = ngram.getCoveredText();
					// System.out.print(ngramText);

					result.add(ngramText);
					if (ngramIterator.hasNext()) {
						// System.out.print(", ");
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
