package eu.openminted.uc.socialsciences.variabledetection.features;

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
import eu.openminted.uc.socialsciences.variabledetection.resource.KnowledgeBaseFactory;
import eu.openminted.uc.socialsciences.variabledetection.resource.KnowledgeBaseResource;
import eu.openminted.uc.socialsciences.variabledetection.resource.TheSozResource;

public class TheSozMetaCollector
    extends LuceneBasedMetaCollector
{
    public static final String PARAM_RESOURCE_NAME = "knowledgeBaseName";
    @ConfigurationParameter(name = PARAM_RESOURCE_NAME, mandatory = true)
    protected String knowledgeBaseName;

    protected KnowledgeBaseResource kbr;

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

        //TODO parameterize max ngram size
        FrequencyDistribution<String> documentNgrams = getDocumentNgrams(jcas, true, false, 1, 4,
                stopwords, Token.class);
        for (String ngram : documentNgrams.getKeys()) {
            // TODO language check
            if (kbr.containsConceptLabel(ngram)) {
                frequencyDistribution.inc(ngram);
            }
        }

        return frequencyDistribution;
    }

    @Override
    protected String getFieldName()
    {
        return "thesoz" + featureExtractorName;
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
