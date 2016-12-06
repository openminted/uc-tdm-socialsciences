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
package helper.util;

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

	public MyIobEncoder(CAS aCas, Type aType, Feature aValueFeature, Feature aModifierFeature, boolean useSubTypes) {
		// fill map for whole JCas in order to efficiently encode IOB
		iobBeginMap = new Int2ObjectOpenHashMap<>();
		iobInsideMap = new Int2ObjectOpenHashMap<>();

		Map<AnnotationFS, Collection<AnnotationFS>> idx = CasUtil.indexCovered(aCas, aType,
				CasUtil.getType(aCas, Token.class));

		String lastValue = null;
		for (AnnotationFS chunk : CasUtil.select(aCas, aType)) {
			String value = chunk.getStringValue(aValueFeature);
			String modifier = chunk.getStringValue(aModifierFeature);

			logger.info("Current value: " + value);
			logger.info("Current modifier: " + modifier);

			for (AnnotationFS token : idx.get(chunk)) {
				logger.info("Annotation: " + token.getCoveredText());
				if (token.getBegin() == chunk.getBegin() &&
						lastValue != null && lastValue.equals(value)) {
					iobBeginMap.put(token.getBegin(), value);
				} else {
					iobInsideMap.put(token.getBegin(), value);
				}
			}

			lastValue = value;
			System.out.println();
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
