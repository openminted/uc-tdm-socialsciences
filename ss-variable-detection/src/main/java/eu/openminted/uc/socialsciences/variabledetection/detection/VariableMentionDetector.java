/**
 * Copyright 2007-2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.openminted.uc.socialsciences.variabledetection.detection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos.POSUtils;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.uc.socialsciences.annotation.VariableMention;

/**
 * Variable detection component
 */
@ResourceMetaData(name="Variable mention tagger")
public class VariableMentionDetector
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false, defaultValue = "../models/variable-detection/")
    private String modelLocation;

    private AnalysisEngine taggerEngine = null;

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        taggerEngine = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, new File(modelLocation),
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Sentence.class.getName());
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {   
        taggerEngine.process(aJCas);

        annotateSentence(aJCas);
    }

    private void annotateSentence(JCas aJCas)
    {
        List<TextClassificationOutcome> outcomes = getPredictions(aJCas);

        TextClassificationTarget target = JCasUtil.select(aJCas, TextClassificationTarget.class).iterator().next();
        target.removeFromIndexes();
        for (TextClassificationOutcome outcome : outcomes) {
            VariableMention variableMention = new VariableMention(aJCas, target.getBegin(), target.getEnd());
            variableMention.setCorrect(outcome.getOutcome());
            variableMention.addToIndexes();
            outcome.removeFromIndexes();
        }
    }

    private List<TextClassificationOutcome> getPredictions(JCas aJCas)
    {
        return new ArrayList<TextClassificationOutcome>(JCasUtil.select(aJCas,
                TextClassificationOutcome.class));
    }
}
