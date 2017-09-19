package eu.openminted.uc.socialsciences.variabledetection.features;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;
import eu.openminted.uc.socialsciences.variabledetection.resource.KnowledgeBaseFactory;
import eu.openminted.uc.socialsciences.variabledetection.resource.KnowledgeBaseResource;
import eu.openminted.uc.socialsciences.variabledetection.resource.TheSozResource;

/**
 * Extracts features using TheSoz knowledge base
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class TheSozFeatures
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{
    public static final String PARAM_RESOURCE_NAME = "knowledgeBaseName";
    @ConfigurationParameter(name = PARAM_RESOURCE_NAME, mandatory = true)
    protected String knowledgeBaseName;

    protected KnowledgeBaseResource kbr;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            kbr = KnowledgeBaseFactory.getInstance().get(knowledgeBaseName);
        }
        catch (ResourceLoaderException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget target)
        throws TextClassificationException
    {
        FrequencyDistribution<String> featureVector = new FrequencyDistribution<>();
        // TODO parameterize max ngram size
        FrequencyDistribution<String> documentNgrams = TheSozMetaCollector.getDocumentNgrams(view,
                true, false, ngramMinN, ngramMaxN, stopwords, Token.class);
        for (String ngram : documentNgrams.getKeys()) {
            if (kbr.containsConceptLabel(ngram, view.getDocumentLanguage())) {
                featureVector.inc(ngram);
            }
        }

        Set<Feature> features = new HashSet<Feature>();
        for (String topNgram : topKSet.getKeys()) {
            if (featureVector.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true));
            }
        }
        return features;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays
                .asList(new MetaCollectorConfiguration(TheSozMetaCollector.class, parameterSettings)
                        .addStorageMapping(TheSozMetaCollector.PARAM_TARGET_LOCATION,
                                TheSozFeatures.PARAM_SOURCE_LOCATION,
                                TheSozMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return TheSozResource.NAME + featureExtractorName;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "TheSoz-";
    }
}
