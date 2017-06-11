package eu.openminted.uc.socialsciences.io.pdfx;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xml.XmlTextReader;
import webanno.custom.Reference;

/**
 * Reader for PDFX XML format.
 * <br>
 * <b>note:</b> This reader is expected to ignore figures and tables (also their captions) inside the document.
 * <br>
 * schema available at : http://pdfx.cs.man.ac.uk/static/article-schema.xsd
 * schema mapping to JATS/NLM DTD available at : http://pdfx.cs.man.ac.uk/serve/pdfx-to-nlm3_v1.2.xsl
 * <br>
 * web-based tool available at : http://pdfx.cs.man.ac.uk/
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
                "webanno.custom.Reference"
        })
public class PdfxXmlReader
        extends XmlTextReader
{
    private static final Logger logger = Logger.getLogger(PdfxXmlReader.class);
    /**
     * either of these contains title of the document
     */
    public static final String TAG_ARTICLE_TITLE = "article-title";

    public static final String ATTR_CLASS = "class";
    public static final String ATTR_TYPE = "type";

    /**
     * abstract section
     */
    public static final String TAG_ABSTRACT = "abstract";


    /**
     * a section in main body
     */
    public static final String TAG_REGION = "region";
    public static final String ATTR_REGION_CLASS_VALUE_UNKNOWN = "unknown";
    public static final String ATTR_REGION_CLASS_VALUE_TEXTCHUNK = "DoCO:TextChunk";

    /**
     * a sentence
     */
    public static final String TAG_S = "s";

    /**
     * a reference citation inside a sentence scope
     */
    public static final String TAG_REF = "xref";
    public static final String ATTR_REF_REF_TYPE = "ref-type";
    public static final String ATTR_REF_REF_ID = "rid";

    /**
     * a marker is used for indicating breaks (e.g. paragraph end, page break, etc.)
     */
    public static final String TAG_MARKER = "marker";
    /**
     * 'block' value for 'type' attribute in 'marker' tag indicates end of paragraph
     */
    public static final String ATTR_MARKER_TYPE_VALUE_BLOCK = "block";

    /**
     * Various levels of headings
     */
    public static final String TAG_H1 = "h1";
    public static final String TAG_H2 = "h2";
    public static final String TAG_H3 = "h3";
    public static final String TAG_H4 = "h4";

    public static final String NEWLINE_SEPARATOR = "\r\n";
    public static final String UNKNOWN_VALUE = "N/A";

    /***
     * @deprecated Setting this parameter to true might cause problems in components/programs that do not expect
     * zero-length sentences
     */
    public static final String PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH = "false";
    @Deprecated
    @ConfigurationParameter(name = PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH, mandatory = false, defaultValue = "false")
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
        return new PdfxXmlHandler();
    }

    public class PdfxXmlHandler
            extends TextExtractor
    {
        public static final String DEFAULT_DOCUMENT_ID = "UNKNOWN";
        private boolean captureText = false;
        private boolean isInsideParagraph = false;
        private boolean paragraphHasSentence = false;

        private String documentId = DEFAULT_DOCUMENT_ID;
        private String lastElementSeen = "";
        private int paragraphBegin = -1;
        private int sentenceBegin = -1;
        private int sentenceEnd = -1;

        private String referenceType = "";
        private String referenceRId = "";
        private int referenceStart = -1;

        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                                 Attributes aAttributes)
                throws SAXException
        {
            if (TAG_REGION.equals(aName)) {
                startElementRegion(aAttributes);
            } else if (TAG_S.equals(aName)) {
                startElementS();
            } else if (TAG_MARKER.equals(aName)){
                //this is an empty xml element i.e. <marker/>
                startElementMarker(aAttributes);
            } else if (TAG_REF.equals(aName)){
                startElementRef(aAttributes);
            } else if (TAG_H1.equals(aName) || TAG_H2.equals(aName) || TAG_H3.equals(aName) || TAG_H4.equals(aName)) {
                startElementHeading();
            } else if (TAG_ARTICLE_TITLE.equals(aName)) {
                startElementArticleTitle();
            } else if (TAG_ABSTRACT.equals(aName)) {
                startElementAbstract();
            }
            lastElementSeen = aName;
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException
        {
            if (TAG_REGION.equals(aName)){
                endElementRegion();
            } else if (TAG_S.equals(aName)){
                endElementS();
            } else if (TAG_REF.equals(aName)){
                endElementRef();
            } else if (TAG_H1.equals(aName) || TAG_H2.equals(aName) || TAG_H3.equals(aName) || TAG_H4.equals(aName)) {
                endElementHeading();
            } else if (TAG_ARTICLE_TITLE.equals(aName)) {
                endElementArticleTitle();
            }else if (TAG_ABSTRACT.equals(aName)){
                endElementAbstract();
            }
            lastElementSeen = aName;
        }

        protected void startElementHeading() {
            beginParagraph();
            startElementS();
        }

        protected void endElementHeading() {
            endElementS();
            endParagraph();
        }

        protected void startElementRef(Attributes aAttributes) {
            String referenceType = aAttributes.getValue(ATTR_REF_REF_TYPE);
            String referenceId = aAttributes.getValue(ATTR_REF_REF_ID);
            //todo also include id <xref rid=referenceId id="...">
            this.referenceType = (referenceType == null ? UNKNOWN_VALUE : referenceType);
            this.referenceRId = (referenceId == null ? UNKNOWN_VALUE : referenceId);
            referenceStart = getBuffer().length();
        }

        protected void endElementRef() {
            if(isNotZeroLengthSpan(referenceStart, getBuffer().length())){
                Reference reference = new Reference(getJCas(), referenceStart, getBuffer().length());
                reference.setRefId(referenceRId);
                reference.setRefType(referenceType);
                reference.addToIndexes();
                referenceStart = -1;
            }
        }

        protected void startElementMarker(Attributes aAttributes) {
            //a </s><marker type="block">... indicates a section end
            if(ATTR_MARKER_TYPE_VALUE_BLOCK.equals(aAttributes.getValue(ATTR_TYPE)) &&
                    lastElementSeen.equals(TAG_S) &&
                    !isNotZeroLengthSpan(sentenceEnd, getBuffer().length()) &&
                    isInsideParagraph){
                endParagraph();
                beginParagraph();
            }
        }

        protected void startElementS() {
            //sentence begin
            sentenceBegin = getBuffer().length();
            if (!paragraphHasSentence &&
                    sentenceBegin > paragraphBegin &&
                    getBuffer().substring(paragraphBegin, sentenceBegin).trim().equals("")) {
                //force removing extra whitespace before a paragraph begin
                getBuffer().delete(paragraphBegin, sentenceBegin);
                paragraphBegin = getBuffer().length();
                sentenceBegin = getBuffer().length();
            }

            paragraphHasSentence = true;
        }

        protected void endElementS() {
            //end of sentence
            if (isInsideParagraph && isNotZeroLengthSpan(sentenceBegin, getBuffer().length())) {
                sentenceEnd = getBuffer().length();
                new Sentence(getJCas(), sentenceBegin, sentenceEnd).addToIndexes();
                paragraphHasSentence = true;
            }
        }

        protected void startElementRegion(Attributes aAttributes) {
            isInsideParagraph = false;
            if(ATTR_REGION_CLASS_VALUE_UNKNOWN.equals(aAttributes.getValue(ATTR_CLASS)) ||
                    ATTR_REGION_CLASS_VALUE_TEXTCHUNK.equals(aAttributes.getValue(ATTR_CLASS))) {
                //paragraph begin
                beginParagraph();
            }
        }

        protected void endElementRegion() {
            if(isInsideParagraph) {
                //end of paragraph
                endParagraph();
            }
        }

        protected void startElementAbstract() {
            //paragraph begin
            beginParagraph();
        }

        protected void endElementAbstract() {
            //end of paragraph
            endParagraph();
        }

        protected void startElementArticleTitle() {
            beginParagraph();
        }

        protected void endElementArticleTitle() {
            if (!documentId.equals(DEFAULT_DOCUMENT_ID))
                logger.warn("More than one article title was seen in the article. Previous value seen [" + documentId + "]");

            String documentTitle = getBuffer().substring(paragraphBegin).trim();
            documentId = documentTitle;

            DocumentMetaData.get(getJCas()).setDocumentTitle(documentId);
            DocumentMetaData.get(getJCas()).setDocumentId(documentId);

            endParagraph();
        }

        protected boolean isNotZeroLengthSpan(int startIndex, int endIndex) {
            return (startIndex >= 0 && endIndex > startIndex);
        }

        private void beginParagraph() {
            paragraphBegin = getBuffer().length();
            captureText = true;
            isInsideParagraph = true;
            paragraphHasSentence = false;
        }

        private void endParagraph() {
            int paragraphEnd = getBuffer().length();
            if (isNotZeroLengthSpan(paragraphBegin, paragraphEnd)) {
                if(isParamAppendNewLineAfterParagraph){
                    int emptySentenceStart = paragraphEnd;
                    getBuffer().append(NEWLINE_SEPARATOR);
                    new Sentence(getJCas(), emptySentenceStart, paragraphEnd).addToIndexes();
                }
                if (!paragraphHasSentence) {
                    //force create a sentence annotation if no sentence tag was seen inside this <Region>
                    sentenceBegin = paragraphBegin;
                    endElementS();
                }
                if (sentenceEnd < paragraphEnd) {
                    //force create a sentence if last piece of text was not inside a sentence <s> tag
                    sentenceBegin = sentenceEnd;
                    endElementS();
                }
                new Paragraph(getJCas(), paragraphBegin, paragraphEnd).addToIndexes();
            }
            captureText = false;
            isInsideParagraph = false;
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