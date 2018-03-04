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

import static org.apache.uima.fit.util.JCasUtil.exists;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.io.File;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import eu.openminted.uc.socialsciences.annotation.GoldVariableMention;
import eu.openminted.uc.socialsciences.annotation.VariableMention;

/**
 * Variable detection component
 */
@ResourceMetaData(name = "Variable mention tagger")
public class VariableMentionDetector
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION)
    private File modelLocation;
    
    private AnalysisEngine taggerEngine = null;

    @Override
    public void initialize(final UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        taggerEngine = AnalysisEngineFactory.createEngine(
                TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelLocation,
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Sentence.class);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        getLogger().info("Detecting variables in [" + aJCas.getDocumentText() + "]");
        
        taggerEngine.process(aJCas);

        // Remove the classification target that was created by the TcAnnotator
        TextClassificationTarget target = selectSingle(aJCas, TextClassificationTarget.class);
        target.removeFromIndexes();

        // Convert all TextClassificationOutcomes to VariableMentions (actually, there will only
        // be a single one)
        for (TextClassificationOutcome outcome : select(aJCas, TextClassificationOutcome.class)) {
            VariableMention variableMention = new VariableMention(aJCas, target.getBegin(),
                    target.getEnd());
            variableMention.setCorrect(outcome.getOutcome());
            variableMention.addToIndexes();
            
            getLogger().info("Detected variable mention: " + variableMention.getCorrect());
            
            outcome.removeFromIndexes();
        }
        
        getLogger().info("Gold variable mention    : "
                + (exists(aJCas, GoldVariableMention.class) ? "Yes" : "No"));
    }
    
    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        if (taggerEngine != null) {
            taggerEngine.collectionProcessComplete();
        }
    }
    
    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException
    {
        super.batchProcessComplete();
        
        if (taggerEngine != null) {
            taggerEngine.batchProcessComplete();
        }
    }
    
    @Override
    public void destroy()
    {
        super.destroy();
        
        if (taggerEngine != null) {
            taggerEngine.destroy();
        }
    }
}
