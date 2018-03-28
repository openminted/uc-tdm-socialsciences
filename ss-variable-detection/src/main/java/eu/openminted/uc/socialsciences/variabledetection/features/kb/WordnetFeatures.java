package eu.openminted.uc.socialsciences.variabledetection.features.kb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
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
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.SemanticRelation;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.ResourceFactory;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

/**
 * Extracts features using wordnet i.e. entity id, synonyms, hypernyms
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos" })
public class WordnetFeatures
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{
    public static final String PARAM_RESOURCE_NAME = "LsrResourceName";
    @ConfigurationParameter(name = PARAM_RESOURCE_NAME, mandatory = true)
    protected String lsrResourceName;

    public static final String PARAM_RESOURCE_LANGUAGE = "LSRResourceLanguage";
    @ConfigurationParameter(name = PARAM_RESOURCE_LANGUAGE, mandatory = true)
    protected String lsrResourceLanguage;

    protected LexicalSemanticResource lsr;

    public static final String PARAM_SYNONYM_FEATURE = "synonymFeature";
    @ConfigurationParameter(name = PARAM_SYNONYM_FEATURE, defaultValue = "true", mandatory = true)
    private boolean synonymFeatures;

    public static final String PARAM_HYPERNYM_FEATURE = "hypernymFeature";
    @ConfigurationParameter(name = PARAM_HYPERNYM_FEATURE, defaultValue = "false", mandatory = false)
    private boolean hypernymFeatures;

    public static final String PARAM_DERIVATION_FEATURE = "derivationFeature";
    @ConfigurationParameter(name = PARAM_DERIVATION_FEATURE, defaultValue = "false", mandatory = false)
    private boolean derivativeFeatures;

    public static final String WORDNET_FIELD = "wordnet";

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            lsr = ResourceFactory.getInstance().get(lsrResourceName, lsrResourceLanguage);
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
        List<Token> tokens = JCasUtil.selectCovered(view, Token.class, target);
        for (Token token : tokens) {
            String lexeme = token.getCoveredText().toLowerCase();
            PoS pos = null;
            switch (token.getPos().getCoarseValue()) {
            case "ADJ":
                pos = PoS.adj;
                break;
            case "ADV":
                pos = PoS.adv;
                break;
            case "N":
                pos = PoS.n;
                break;
            case "V":
                pos = PoS.v;
                break;
            }
            if (pos == null)
                continue;

            try {
                Entity entity = lsr.getMostFrequentEntity(lexeme, pos);
                if (entity == null) {
                    continue;
                }
                featureVector.inc(entity.getId());

                // Synonyms
                if (synonymFeatures) {
                    if (entity.getSense(lexeme) != null) {
                        Set<String> synonyms = lsr.getRelatedLexemes(lexeme, pos,
                                entity.getSense(lexeme),
                                LexicalSemanticResource.LexicalRelation.synonymy);
                        for (String synonym : synonyms) {
                            Set<Entity> synonymEntities = lsr.getEntity(synonym, pos);
                            for (Entity nEntity : synonymEntities) {
                                featureVector.inc(nEntity.getId());
                            }
                        }
                    }
                }

                // Hypernyms
                if (hypernymFeatures) {
                    Set<Entity> hypernyms = lsr.getRelatedEntities(entity,
                            SemanticRelation.hypernymy);
                    for (Entity pEntity : hypernyms) {
                        featureVector.inc(pEntity.getId());
                    }
                }
            }
            catch (LexicalSemanticResourceException e) {
                throw new IllegalStateException("Method not supported by LSR!", e);
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
        return Arrays.asList(
                new MetaCollectorConfiguration(WordnetMetaCollector.class, parameterSettings)
                        .addStorageMapping(WordnetMetaCollector.PARAM_TARGET_LOCATION,
                                WordnetFeatures.PARAM_SOURCE_LOCATION,
                                WordnetMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return WORDNET_FIELD + featureExtractorName;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "wordnet-";
    }
}
