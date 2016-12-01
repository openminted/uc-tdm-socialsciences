package helper;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Writer;

/**
 * Writer for Custom Conll format to be used as input for Stanford CoreNLP NER
 * training.
 *
 * @author neumanmy
 */
public class MyConllWriter extends Conll2002Writer {

	private static final Logger logger = Logger.getLogger(MyConllWriter.class);
	public static final TypeSystemDescription PARAM_TARGET_LOCATION = null;

	public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
	@ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".tsv")
	private String filenameSuffix;

	/**
	 * Name of configuration parameter that contains the character encoding used
	 * by the input files.
	 */
	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
					encoding));
			convert(aJCas, out);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		} finally {
			closeQuietly(out);
		}
	}

	private void convert(JCas aJCas, PrintWriter aOut) {
		Type neType = JCasUtil.getType(aJCas, NamedEntity.class); // TODO use
																	 // CustomNamedEntity
																	 // somehow
		Feature neValue = neType.getFeatureByBaseName("value"); // TODO not
																 // value
		// TODO also incorporate modifier

		for (Sentence sentence : select(aJCas, Sentence.class)) {
			HashMap<Token, Row> ctokens = new LinkedHashMap<>();

			// Tokens
			List<Token> tokens = selectCovered(Token.class, sentence);

			// Named Entities
			IobEncoder encoder = new IobEncoder(aJCas.getCas(), neType, neValue);

			for (Token token : tokens) {
				Row row = new Row();
				row.token = token;
				row.ne = encoder.encode(token);
				ctokens.put(row.token, row);
			}

			// Write sentence in CONLL 2006 format
			for (Row row : ctokens.values()) {
				String namedEntity = row.ne;

				aOut.printf("%s %s\n", row.token.getCoveredText(), namedEntity);
			}

			aOut.println();
		}
	}

	private static final class Row {
		Token token;
		String ne;
	}
}
