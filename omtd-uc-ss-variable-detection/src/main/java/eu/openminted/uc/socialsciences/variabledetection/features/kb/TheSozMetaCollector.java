package eu.openminted.uc.socialsciences.variabledetection.features.kb;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;
import eu.openminted.uc.socialsciences.variabledetection.resource.KnowledgeBaseResource;
import eu.openminted.uc.socialsciences.variabledetection.resource.TheSozResource;
import eu.openminted.uc.socialsciences.variabledetection.similarity.kb.KnowledgeBaseFactory;

public class TheSozMetaCollector
    extends LuceneBasedMetaCollector
{
    protected KnowledgeBaseResource kbr;

    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;
    
    @ConfigurationParameter(name = TheSozFeatures.PARAM_NGRAM_MIN_N, mandatory = true)
    protected int ngramMinN;

    @ConfigurationParameter(name = TheSozFeatures.PARAM_NGRAM_MAX_N, mandatory = true)
    protected int ngramMaxN;

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
            kbr = KnowledgeBaseFactory.getInstance().get(TheSozResource.NAME);
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

        FrequencyDistribution<String> documentNgrams = getDocumentNgrams(jcas, true, false, ngramMinN, ngramMaxN,
                stopwords, Token.class);
        for (String ngram : documentNgrams.getKeys()) {
            if (kbr.containsConceptLabel(ngram, jcas.getDocumentLanguage())) {
                frequencyDistribution.inc(ngram);
            }
        }

        return frequencyDistribution;
    }

    @Override
    protected String getFieldName()
    {
        return TheSozResource.NAME + featureExtractorName;
    }

    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN,
            Set<String> stopwords, Class<? extends Annotation> annotationClass)
        throws TextClassificationException
    {
        final String ngramGlue = " ";
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
            List<String> strings = NGramUtils.valuesToText(jcas, s, annotationClass.getName());
            for (List<String> ngram : new NGramStringListIterable(strings, minN, maxN)) {
                if (lowerCaseNGrams) {
                    ngram = NGramUtils.lower(ngram);
                }

                if (NGramUtils.passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, ngramGlue);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }
}
