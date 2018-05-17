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
import java.io.Serializable;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.similarity.algorithms.api.JCasTextSimilarityMeasureBase;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.dkpro.similarity.ml.filters.LogFilter;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;

/**
 *
 * Copied from dkpro-similarity project https://github.com/dkpro/dkpro-similarity
 * 
 * Original class org.dkpro.similarity.algorithms.ml.LinearRegressionSimilarityMeasure
 *
 * 
 * Runs a linear regression classifier on the provided test data on a model that is trained on the
 * given training data. Mind that the {@link #getSimilarity(JCas,JCas) getSimilarity} method
 * classifies the input texts by their ID, not their textual contents. The
 * 
 * <pre>
 * DocumentID
 * </pre>
 * 
 * of the
 * 
 * <pre>
 * DocumentMetaData
 * </pre>
 * 
 * is expected to denote the corresponding input line in the test data.
 */
public class LinearRegressionSimilarityMeasure
    extends JCasTextSimilarityMeasureBase
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final Classifier CLASSIFIER = new LinearRegression();

    private Classifier filteredClassifier;
    private boolean useLogFilter;

    public LinearRegressionSimilarityMeasure(File trainArff, boolean aUseLogFilter) throws Exception
    {
        // Get all instances
        Instances train = getTrainInstances(trainArff);
        useLogFilter = aUseLogFilter;

        // Apply log filter
        if (useLogFilter) {
            Filter logFilter = new LogFilter();
            logFilter.setInputFormat(train);
            train = Filter.useFilter(train, logFilter);
        }

        Classifier clsCopy;
        try {
            // Copy the classifier
            clsCopy = AbstractClassifier.makeCopy(CLASSIFIER);

            // Build the classifier
            filteredClassifier = clsCopy;
            filteredClassifier.buildClassifier(train);

            System.out.println(filteredClassifier.toString());
        }
        catch (Exception e) {
            throw new SimilarityException(e);
        }
    }

    private Instances getTrainInstances(File trainArff) throws SimilarityException
    {
        // Read with Weka
        Instances data;
        try {
            data = DataSource.read(trainArff.getAbsolutePath());
        }
        catch (Exception e) {
            throw new SimilarityException(e);
        }

        // Set the index of the class attribute
        data.setClassIndex(data.numAttributes() - 1);

        return data;
    }

    public Instance getInstance(File arff) throws Exception
    {
        Instances instances = getTrainInstances(arff);
        if (useLogFilter) {
            Filter logFilter = new LogFilter();
            logFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, logFilter);
        }
        return instances.get(0);
    }
    
    public boolean isUseLogFilter()
    {
        return useLogFilter;
    }

    public double getSimilarity(Instance instance) throws Exception
    {
        return filteredClassifier.classifyInstance(instance);
    }

    @Override
    public double getSimilarity(JCas jcas1, JCas jcas2, Annotation coveringAnnotation1,
            Annotation coveringAnnotation2)
        throws SimilarityException
    {
        throw new UnsupportedOperationException();
    }
}