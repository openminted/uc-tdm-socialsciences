package eu.openminted.uc_tdm_socialsciences.io.jats;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xml.XmlTextReader;

/**
 * Reader for the British National Corpus (XML version).
 *
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph"})
public class JatsXmlReader
        extends XmlTextReader
{
    /**
     * contains the (optional) abstract of a paper
     */
    private static final String TAG_ABSTRACT = "ABSTRACT";

    /**
     * either of these contains title of the document
     */
    public static final String TAG_CURRENT_TITLE = "CURRENT_TITLE";
    private static final String TAG_TITLE = "TITLE";

    /**
     * a paragraph
     */
    public static final String TAG_P = "P";

    /**
     * a sentence in abstract section
     */
    public static final String TAG_A_S = "A-S";

    /**
     * a sentence in main body
     */
    public static final String TAG_S = "S";

    /**
     * (optional) a reference citation inside a sentence scope
     */
    public static final String TAG_REF = "REF";

    /**
     * id of the reference entry
     */
    public static final String ATTR_REF_REFID = "REFID";
    /**
     * (optional) If assigned with the true value indicates that the citation is a self-citation
     */
    public static final String ATTR_REF_SELF = "SELF";
    public static final String ATTRVALUE_REF_SELF_TRUE = "YES";

    /**
     * (optional) contains reference entries
     */
    public static final String TAG_REFERENCELIST = "REFERENCELIST";

    public static final String PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH = "false";
    @ConfigurationParameter(name = PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, mandatory = false)
    protected boolean isParamAppendNewLineAfterParagraph;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException
    {
        super.initialize(aContext);
    }

    @Override
    protected void initCas(CAS aCas, Resource aResource, String aQualifier) throws RuntimeException
    {
        super.initCas(aCas, aResource, aQualifier);
    }

    @Override
    protected Handler newSaxHandler()
    {
        return new SciXmlHandler();
    }

    public class SciXmlHandler
            extends TextExtractor
    {
        private boolean titleCaptured = false;
        private boolean captureText = false;

        private String documentId = null;
        private int paragraphStart = -1;
        private int sentenceStart = -1;
        private int referenceStart = -1;
        private String referenceId = "";
        private boolean isSelfReference = false;

        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                                 Attributes aAttributes)
                throws SAXException
        {
            if (TAG_TITLE.equals(aName) || TAG_CURRENT_TITLE.equals(aName)) {
                if(!titleCaptured)
                    captureText = true;
            }
            else if (TAG_S.equals(aName) || TAG_A_S.equals(aName)){
                sentenceStart = getBuffer().length();
                captureText = true;
            }else if(TAG_REF.equals(aName)){
                referenceId = aAttributes.getValue(ATTR_REF_REFID);
                //ATTR_REF_SELF is an optional attribute
                String selfReference = aAttributes.getValue(ATTR_REF_SELF);
                isSelfReference = (selfReference != null && selfReference.equalsIgnoreCase(ATTRVALUE_REF_SELF_TRUE));

                referenceStart = getBuffer().length();
            }else if(TAG_P.equals(aName)){
                paragraphStart = getBuffer().length();
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException
        {
            if (TAG_TITLE.equals(aName) || TAG_CURRENT_TITLE.equals(aName)) {
                if(!titleCaptured) {
                    String documentTitle = getBuffer().toString().trim();
                    documentId = documentTitle;

                    DocumentMetaData.get(getJCas()).setDocumentTitle(documentTitle);
                    DocumentMetaData.get(getJCas()).setDocumentId(documentId);
                    getBuffer().setLength(0);
                    captureText = false;
                    titleCaptured = true;
                }
            }
            else if (TAG_S.equals(aName) || TAG_A_S.equals(aName)){
                new Sentence(getJCas(), sentenceStart, getBuffer().length()).addToIndexes();
                sentenceStart = -1;
                captureText = false;

            }/*else if(TAG_REF.equals(aName)){
                if(isNotBlank(getBuffer().substring(referenceStart, getBuffer().length()))){
                    *//*Reference reference = new Reference(getJCas(), referenceStart, getBuffer().length());
                    reference.setRefId(referenceId);
                    reference.setRefType( isSelfReference ? "self-Reference":"other-work" );
                    reference.addToIndexes();*//*
                }
            }*/else if(TAG_P.equals(aName)){
                if(isParamAppendNewLineAfterParagraph){
                    int emptySentenceStart = getBuffer().length();
                    getBuffer().append(System.lineSeparator());
                    new Sentence(getJCas(), emptySentenceStart, getBuffer().length()).addToIndexes();
                }
                new Paragraph(getJCas(), paragraphStart, getBuffer().length()).addToIndexes();
                paragraphStart = -1;
            }
        }

        @Override
        public void characters(char[] aCh, int aStart, int aLength)
                throws SAXException
        {
            if (captureText) {
                super.characters(aCh, aStart, aLength);
            }
        }

        @Override
        public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
                throws SAXException
        {
            if (captureText) {
                super.ignorableWhitespace(aCh, aStart, aLength);
            }
        }
    }
}