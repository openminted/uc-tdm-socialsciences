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
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import helper.util.MyIobEncoder;
import webanno.custom.NamedEntity;

/**
 * Writer for Custom tsv format to be used as input for Stanford CoreNLP NER
 * training.
 *
 * @author neumanmy
 */
@MimeTypeCapability({ MimeTypes.TEXT_X_CONLL_2003 })
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				"webanno.custom.NamedEntity" })
public class MyStanfordTsvWriter extends JCasFileWriter_ImplBase {

	private static final Logger logger = Logger.getLogger(MyStanfordTsvWriter.class);

	public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
	@ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".tsv")
	private String filenameSuffix;

	public static final String PARAM_USE_SUBTYPES = "false";
	@ConfigurationParameter(name = PARAM_USE_SUBTYPES, mandatory = true, defaultValue = "false")
	private boolean useSubTypes;

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
		logger.info("Starting processing JCas. " + aJCas.getViewName());
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
		Type neType = JCasUtil.getType(aJCas, NamedEntity.class); // TODO
																	 // use
																	 // CustomNamedEntity
																	 // somehow

		// only "value" in test run, in real annotation it's "entityType"
		Feature neValue = neType.getFeatureByBaseName("value");
		Feature neModifier = neType.getFeatureByBaseName("modifier");

		for (Sentence sentence : select(aJCas, Sentence.class)) {
			HashMap<Token, Row> ctokens = new LinkedHashMap<>();

			// Tokens
			List<Token> tokens = selectCovered(Token.class, sentence);

			MyIobEncoder encoder = new MyIobEncoder(aJCas.getCas(), neType, neValue, neModifier, useSubTypes);

			for (Token token : tokens) {
				Row row = new Row();
				row.token = token;
				row.ne_val = encoder.encode(token);
				ctokens.put(row.token, row);
			}

			/*
			 * Write sentence in tsv format
			 * One token per line, tag in 2nd column. Sentences separated by
			 * empty line.
			 */
			for (Row row : ctokens.values()) {
				String label = row.ne_val;

				// write tab separated data
				aOut.printf("%s\t%s\n", row.token.getCoveredText(), label);
			}

			aOut.println();
		}
	}

	private static final class Row {
		Token token;
		String ne_val;
	}
}
