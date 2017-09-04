package eu.openminted.uc.socialsciences.variabledetection.features;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.ResourceFactory;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

/**
 * Extracts features using wordnet i.e. entity id, synonyms, hypernyms
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos"})
public class WordnetFeatures
    extends FeatureExtractorResource_ImplBase
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
        Set<Feature> featureList = new TreeSet<Feature>();

        FrequencyDistribution<String> featureVector = new FrequencyDistribution<>();
        List<Token> tokens = JCasUtil.selectCovered(view, Token.class, target);
        for (Token token : tokens) {
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
                Set<Entity> foundEntities = lsr.getEntity(token.getCoveredText(), pos);
                for (Entity entity : foundEntities) {
                    featureVector.inc(entity.getId());

                    if (synonymFeatures) {
                        Set<Entity> neighbors = lsr.getNeighbors(entity);
                        for (Entity nEntity : neighbors) {
                            featureVector.inc(nEntity.getId());
                        }
                    }

                    if (hypernymFeatures) {
                        Set<Entity> parents = lsr.getParents(entity);
                        for (Entity pEntity : parents) {
                            featureVector.inc(pEntity.getId());
                        }
                    }
                }
            }
            catch (LexicalSemanticResourceException e) {
                throw new IllegalStateException("Method not supported by LSR!", e);
            }
        }
        
        for (String key : featureVector.getKeys()) {
            featureList.add(new Feature(getFeaturePrefix() + key, 1));
        }

        return featureList;
    }

    private String getFeaturePrefix()
    {
        return "wordnet-";
    }
}
