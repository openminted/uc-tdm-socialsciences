package eu.openminted.uc_tdm_socialsciences.io.pdfx;

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

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Reader for PDFX XML format
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

    public static final String PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH = "false";
    public static final String NEWLINE_SEPARATOR = "\r\n";
    public static final String UNKNOWN_VALUE = "N/A";

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
        private boolean captureText = false;
        private boolean isInsideParagraph = false;
        private boolean paragraphHasSentence = false;

        private String documentId = "UNKNOWN";
        private int paragraphBegin = -1;
        private int sentenceBegin = -1;

        private String referenceType = "";
        private String referenceRId = "";
        private int referenceStart = -1;

        //todo: create specific annotations for footer and header?
        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                                 Attributes aAttributes)
                throws SAXException
        {
            //todo optimize order of following if statements
            if (TAG_ARTICLE_TITLE.equals(aName)) {
                startElementArticleTitle();
            }else if (TAG_ABSTRACT.equals(aName)) {
                startElementAbstract();
            }else if (TAG_REGION.equals(aName)) {
                startElementRegion(aAttributes);
            }else if(TAG_S.equals(aName)) {
                startElementS();
            }else if(TAG_H1.equals(aName) || TAG_H2.equals(aName) || TAG_H3.equals(aName) || TAG_H4.equals(aName)) {
                startElementHeading();
            }else if(TAG_MARKER.equals(aName)){
                startElementMarker(aAttributes);
            }else if(TAG_REF.equals(aName)){
                startElementRef(aAttributes);
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException
        {
            if (TAG_ARTICLE_TITLE.equals(aName)) {
                endElementArticleTitle();
            }else if (TAG_ABSTRACT.equals(aName)){
                endElementAbstract();
            }else if (TAG_REGION.equals(aName)){
                endElementRegion();
            }else if(TAG_S.equals(aName)){
                endElementS();
            }else if(TAG_H1.equals(aName) || TAG_H2.equals(aName) || TAG_H3.equals(aName) || TAG_H4.equals(aName)) {
                //todo create a specific annotation for heading?
                endElementHeading();
            }else if(TAG_REF.equals(aName)){
                endElementRef();
            }
        }

        protected void startElementHeading() {
            captureText = true;
            startElementS();
        }

        protected void endElementHeading() {
            endElementS();
            captureText = false;
        }

        protected void startElementRef(Attributes aAttributes) {
            String referenceType = aAttributes.getValue(ATTR_REF_REF_TYPE);
            String referenceId = aAttributes.getValue(ATTR_REF_REF_ID);
            //todo also include id <xref rid=referenceId id="...">
            this.referenceType = (referenceType == null ? UNKNOWN_VALUE : referenceType);
            this.referenceRId = (referenceId == null? UNKNOWN_VALUE : referenceId);
            referenceStart = getBuffer().length();
        }

        protected void startElementMarker(Attributes aAttributes) {
            if(ATTR_MARKER_TYPE_VALUE_BLOCK.equals(aAttributes.getValue(ATTR_TYPE))){
                //paragraph end indicator
                makeParagraph();
            }
        }

        protected void startElementS() {
            //sentence begin
            sentenceBegin = getBuffer().length();
            paragraphHasSentence = true;
        }

        protected void startElementRegion(Attributes aAttributes) {
            isInsideParagraph = false;
            if(ATTR_REGION_CLASS_VALUE_UNKNOWN.equals(aAttributes.getValue(ATTR_CLASS)) ||
                    ATTR_REGION_CLASS_VALUE_TEXTCHUNK.equals(aAttributes.getValue(ATTR_CLASS))) {
                //paragraph begin
                beginParagraph();
            }
        }

        protected void startElementAbstract() {
            //paragraph begin
            beginParagraph();
        }

        protected void startElementArticleTitle() {
            captureText = true;
            beginParagraph();
        }

        protected void endElementRef() {
            if(isNotBlank(getBuffer().substring(referenceStart, getBuffer().length()))){
                Reference reference = new Reference(getJCas(), referenceStart, getBuffer().length());
                reference.setRefId(referenceRId);
                reference.setRefType(referenceType);
                reference.addToIndexes();
            }
        }

        protected void endElementS() {
            //end of sentence
            //fixme should only check isInsideParagraph
            if(sentenceBegin >= 0 && getBuffer().length() > sentenceBegin) {
                new Sentence(getJCas(), sentenceBegin, getBuffer().length()).addToIndexes();
                sentenceBegin = -1;
            }
        }

        protected void endElementRegion() {
            if(isInsideParagraph) {
                //end of paragraph
                if (!paragraphHasSentence){
                    //force create a sentence annotation if no sentence tag was seen inside this <Region>
                    sentenceBegin = paragraphBegin;
                    endElementS();
                }
                makeParagraph();
                captureText = false;
                isInsideParagraph = false;
            }
        }

        protected void endElementAbstract() {
            //end of paragraph
            makeParagraph();
            captureText = false;
        }

        protected void endElementArticleTitle() {
            String documentTitle = getBuffer().toString().trim();
            documentId = documentTitle;

            DocumentMetaData.get(getJCas()).setDocumentTitle(documentTitle);
            DocumentMetaData.get(getJCas()).setDocumentId(documentId);

            makeParagraph();
            captureText = false;
        }

        private void beginParagraph() {
            paragraphBegin = getBuffer().length();
            captureText = true;
            isInsideParagraph = true;
            paragraphHasSentence = false;
        }

        private void makeParagraph() {
            if(isParamAppendNewLineAfterParagraph){
                int emptySentenceStart = getBuffer().length();
				getBuffer().append(NEWLINE_SEPARATOR);
                new Sentence(getJCas(), emptySentenceStart, getBuffer().length()).addToIndexes();
            }
            new Paragraph(getJCas(), paragraphBegin, getBuffer().length()).addToIndexes();
            paragraphBegin = getBuffer().length();
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