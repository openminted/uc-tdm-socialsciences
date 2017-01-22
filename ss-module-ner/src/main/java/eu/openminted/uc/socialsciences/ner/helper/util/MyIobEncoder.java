/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.openminted.uc.socialsciences.ner.helper.util;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.LogManager;
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
 * Converts a chunk annotations into IOB2-style.
 * When doing the encoding, it is assumed that there is one custom type with two
 * features. Example: Type 'Named Entity' with the features 'value' and
 * 'modifier'. The 'modifier' feature is expected to be used to create sub
 * types. For example, you may have an annotation with value "LOC" and modifier
 * "city", and another annotation with value "LOC" and modifier "country".
 * The encoder can produce either coarse-grained or fine-grained annotations, so
 * in the example either "B-LOC" for both annotations, or "B-LOCcity" and
 * "B-LOCcountry" which can then be treated as different classes in training a
 * model with these annotations.
 */
public class MyIobEncoder {

	private static final Logger logger = LogManager.getLogger(MyIobEncoder.class);

	private Int2ObjectMap<String> iobBeginMap;
	private Int2ObjectMap<String> iobInsideMap;

	/**
	 * Constructor.
	 *
	 * @param aCas
	 *            The CAS.
	 * @param aType
	 *            The Type of annotation that should be encoded in IOB.
	 * @param aValueFeature
	 *            The Feature for the type that carries the annotation value.
	 * @param aModifierFeature
	 *            The Feature for the type that carries some additional info.
	 * @param useSubTypes
	 *            If true, {aModifierFeatue} will be included in the output,
	 *            otherwise it will be ignored.
	 */
	public MyIobEncoder(CAS aCas, Type aType, Feature aValueFeature, Feature aModifierFeature, boolean useSubTypes) {
		// fill map for whole JCas in order to efficiently encode IOB
		iobBeginMap = new Int2ObjectOpenHashMap<>();
		iobInsideMap = new Int2ObjectOpenHashMap<>();

		Map<AnnotationFS, Collection<AnnotationFS>> coveringNeIdx = CasUtil.indexCovering(aCas, aType, aType);
		Map<AnnotationFS, Collection<AnnotationFS>> tokenIdx = CasUtil.indexCovered(aCas, aType,
				CasUtil.getType(aCas, Token.class));

		nextChunk: for (AnnotationFS chunk : CasUtil.select(aCas, aType)) {
			String value = chunk.getStringValue(aValueFeature);
			String modifier = chunk.getStringValue(aModifierFeature);
			logger.debug(String.format("Annotation: '%s' (%d:%d)", chunk.getCoveredText(), chunk.getBegin(),
					chunk.getEnd()));
			logger.debug("Value: " + chunk.getStringValue(aValueFeature));
			logger.debug("Modifier: " + chunk.getStringValue(aModifierFeature));

			String label = useSubTypes && modifier != null ? value + modifier : value;

			if (coveringNeIdx.containsKey(chunk)) {
				// there are covering annotations, so don't include this one
				continue nextChunk;
			}

			for (AnnotationFS token : tokenIdx.get(chunk)) {
				if (token.getBegin() == chunk.getBegin()) {
					if (iobInsideMap.containsKey(token.getBegin())) {
						continue nextChunk;
					}
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
