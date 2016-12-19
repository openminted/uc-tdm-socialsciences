package eu.openminted.uc.socialsciences.ner.helper.util;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Converts a chunk annotations into IOB2-style
 */
public class MyIobEncoder {

	private static final Logger logger = Logger.getLogger(MyIobEncoder.class);

	private Int2ObjectMap<String> iobBeginMap;
	private Int2ObjectMap<String> iobInsideMap;

	public MyIobEncoder(CAS aCas, Type aType, Feature aValueFeature, Feature aModifierFeature) {
		this(aCas, aType, aValueFeature, aModifierFeature, false);
	}

	// TODO: does not correctly handle overlapping annotations, where
	// begin[anno1]<begin[anno2] and end[anno1]<end[anno2] and
	// end[anno1]>begin[anno2]
	public MyIobEncoder(CAS aCas, Type aType, Feature aValueFeature, Feature aModifierFeature, boolean useSubTypes) {
		// fill map for whole JCas in order to efficiently encode IOB
		iobBeginMap = new Int2ObjectOpenHashMap<>();
		iobInsideMap = new Int2ObjectOpenHashMap<>();

		Map<AnnotationFS, Collection<AnnotationFS>> nestedAnnoIdx = CasUtil.indexCovering(aCas, aType, aType);
		Map<AnnotationFS, Collection<AnnotationFS>> tokenIdx = CasUtil.indexCovered(aCas, aType,
				CasUtil.getType(aCas, Token.class));

		for (AnnotationFS chunk : CasUtil.select(aCas, aType)) {
			String value = chunk.getStringValue(aValueFeature);
			String modifier = chunk.getStringValue(aModifierFeature);
			logger.debug(String.format("Annotation: '%s' (%d:%d)", chunk.getCoveredText(), chunk.getBegin(),
					chunk.getEnd()));
			logger.debug("Value: " + chunk.getStringValue(aValueFeature));
			logger.debug("Modifier: " + chunk.getStringValue(aModifierFeature));

			if (null == value) {
				continue;
			}

			String label = useSubTypes && modifier != null ? value + modifier : value;

			if (!nestedAnnoIdx.get(chunk).isEmpty()) {
				continue;
			}

			for (AnnotationFS token : tokenIdx.get(chunk)) {
				if (token.getBegin() == chunk.getBegin()) {
					iobBeginMap.put(token.getBegin(), label);
				} else {
					iobInsideMap.put(token.getBegin(), label);
				}
			}
		}
	}

	/**
	 * Returns the IOB tag for a given token.
	 *
	 * @param token
	 *            a token.
	 * @return the IOB tag.
	 */
	public String encode(Token token) {
		if (iobBeginMap.containsKey(token.getBegin())) {
			return "B-" + iobBeginMap.get(token.getBegin());
		}

		if (iobInsideMap.containsKey(token.getBegin())) {
			return "I-" + iobInsideMap.get(token.getBegin());
		}

		return "O";
	}
}
