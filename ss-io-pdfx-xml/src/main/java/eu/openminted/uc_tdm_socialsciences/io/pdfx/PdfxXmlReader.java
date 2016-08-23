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

    public static final String PARAM_APPEND_NEW_LINE_AFTER_PARAGRAPH = "false";
    public static final String NEWLINE_SEPARATOR = "\r\n";
    public static final String UNKNOWN_VALUE = "N/A";
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
        private boolean captureText = false;
        private boolean isInsideSentence = false;

        private String documentId = "UNKNOWN";
        private int paragraphBegin = -1;
        private int sentenceBegin = -1;

        private String referenceType = "";
        private String referenceRId = "";
        private int referenceStart = -1;

        //todo: retain footer and header in the jcas with proper annotation
        //todo read section titles, too?
        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                                 Attributes aAttributes)
                throws SAXException
        {
            if (TAG_ARTICLE_TITLE.equals(aName)) {
                captureText = true;
                beginParagraph();
            }else if (TAG_ABSTRACT.equals(aName)){
                //paragraph begin
                beginParagraph();
            }else if (TAG_REGION.equals(aName)){
                isInsideSentence = false;
                if(ATTR_REGION_CLASS_VALUE_UNKNOWN.equals(aAttributes.getValue(ATTR_CLASS)) ||
                        ATTR_REGION_CLASS_VALUE_TEXTCHUNK.equals(aAttributes.getValue(ATTR_CLASS))) {
                    //paragraph begin
                    beginParagraph();
                }
            }else if(TAG_S.equals(aName)){
                //sentence begin
                sentenceBegin = getBuffer().length();
            }else if(TAG_MARKER.equals(aName)){
                if(ATTR_MARKER_TYPE_VALUE_BLOCK.equals(aAttributes.getValue(ATTR_TYPE))){
                    //paragraph end indicator
                    makeParagraph();
                }
            }else if(TAG_REF.equals(aName)){
                String referenceType = aAttributes.getValue(ATTR_REF_REF_TYPE);
                String referenceId = aAttributes.getValue(ATTR_REF_REF_ID);
                //todo also include id <xref rid=referenceId id="...">
                this.referenceType = (referenceType == null ? UNKNOWN_VALUE : referenceType);
                this.referenceRId = (referenceId == null? UNKNOWN_VALUE : referenceId);
                referenceStart = getBuffer().length();
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException
        {
            if (TAG_ARTICLE_TITLE.equals(aName)) {
                String documentTitle = getBuffer().toString().trim();
                documentId = documentTitle;

                DocumentMetaData.get(getJCas()).setDocumentTitle(documentTitle);
                DocumentMetaData.get(getJCas()).setDocumentId(documentId);

                makeParagraph();
                captureText = false;
            }else if (TAG_ABSTRACT.equals(aName)){
                //end of paragraph
                makeParagraph();
                captureText = false;
            }else if (TAG_REGION.equals(aName)){
                if(isInsideSentence) {
                    //end of paragraph
                    makeParagraph();
                    captureText = false;
                }
            }else if(TAG_S.equals(aName)){
                //end of sentence
                new Sentence(getJCas(), sentenceBegin, getBuffer().length()).addToIndexes();
                sentenceBegin = -1;
            }else if(TAG_REF.equals(aName)){
                if(isNotBlank(getBuffer().substring(referenceStart, getBuffer().length()))){
                    Reference reference = new Reference(getJCas(), referenceStart, getBuffer().length());
                    reference.setRefId(referenceRId);
                    reference.setRefType(referenceType);
                    reference.addToIndexes();
                }
            }
        }

        private void beginParagraph() {
            paragraphBegin = getBuffer().length();
            captureText = true;
            isInsideSentence = true;
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