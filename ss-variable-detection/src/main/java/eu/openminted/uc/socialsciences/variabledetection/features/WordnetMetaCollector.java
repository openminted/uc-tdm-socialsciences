package eu.openminted.uc.socialsciences.variabledetection.features;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.SemanticRelation;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.ResourceFactory;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

public class WordnetMetaCollector
    extends LuceneBasedMetaCollector
{
    public static final String PARAM_RESOURCE_NAME = "LsrResourceName";
    @ConfigurationParameter(name = PARAM_RESOURCE_NAME, mandatory = true)
    protected String lsrResourceName;

    public static final String PARAM_RESOURCE_LANGUAGE = "LSRResourceLanguage";
    @ConfigurationParameter(name = PARAM_RESOURCE_LANGUAGE, mandatory = true)
    protected String lsrResourceLanguage;

    protected LexicalSemanticResource lsr;

    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    private Set<String> stopwords;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, false);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        try {
            lsr = ResourceFactory.getInstance().get(lsrResourceName, lsrResourceLanguage);
        }
        catch (ResourceLoaderException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        FrequencyDistribution<String> frequencyDistribution = new FrequencyDistribution<>();
        Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
        for (Token token : tokens) {
            String lexeme = token.getCoveredText().toLowerCase();
            if (stopwords.contains(lexeme))
                continue;

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
                Set<Entity> foundEntities = lsr.getEntity(lexeme, pos);
                for (Entity entity : foundEntities) {
                    frequencyDistribution.inc(entity.getId());

                    // Synonyms
                    if (entity.getSense(lexeme) != null) {
                        Set<String> synonyms = lsr.getRelatedLexemes(lexeme, pos,
                                entity.getSense(lexeme),
                                LexicalSemanticResource.LexicalRelation.synonymy);
                        for (String synonym : synonyms) {
                            Set<Entity> synonymEntities = lsr.getEntity(synonym, pos);
                            for (Entity nEntity : synonymEntities) {
                                frequencyDistribution.inc(nEntity.getId());
                            }
                        }
                    }

                    // Hypernyms
                    Set<Entity> hypernyms = lsr.getRelatedEntities(entity,
                            SemanticRelation.hypernymy);
                    for (Entity pEntity : hypernyms) {
                        frequencyDistribution.inc(pEntity.getId());
                    }

                }
            }
            catch (LexicalSemanticResourceException e) {
                throw new IllegalStateException("Method not supported by LSR!", e);
            }
        }

        return frequencyDistribution;
    }

    @Override
    protected String getFieldName()
    {
        return WordnetFeatures.WORDNET_FIELD + featureExtractorName;
    }

}
