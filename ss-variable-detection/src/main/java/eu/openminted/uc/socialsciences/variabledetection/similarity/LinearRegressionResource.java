/**
 * Copyright 2012-2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package eu.openminted.uc.socialsciences.variabledetection.similarity;

import java.io.File;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import dkpro.similarity.uima.resource.JCasTextSimilarityResourceBase;

import eu.openminted.uc.socialsciences.variabledetection.similarity.LinearRegressionSimilarityMeasure;

/**
 * Copied from dkpro-similarity project https://github.com/dkpro/dkpro-similarity
 * 
 * Original class org.dkpro.similarity.uima.resource.ml.LinearRegressionResource
 */
public class LinearRegressionResource
    extends JCasTextSimilarityResourceBase
{
    public static final String PARAM_LOG_FILTER = "LogFilter";
    @ConfigurationParameter(name = PARAM_LOG_FILTER, mandatory = true, defaultValue = "true")
    private boolean logFilter;

    public static final String PARAM_TRAIN_ARFF = "TRAIN_ARFF";
    @ConfigurationParameter(name = PARAM_TRAIN_ARFF, mandatory = true)
    private File trainArff;

    @SuppressWarnings("unchecked")
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            setMode(TextSimilarityResourceMode.jcas);
            measure = new LinearRegressionSimilarityMeasure(trainArff, logFilter);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        return true;
    }
}
