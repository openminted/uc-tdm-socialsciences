package eu.openminted.uc_tdm_socialsciences.io.pdfx;

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
 * Reader for PDFX XML format
 *
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph"})
public class PdfxXmlReader
        extends XmlTextReader
{
    /**
     * either of these contains title of the document
     */
    private static final String TAG_ARTICLE_TITLE = "TITLE";

    public static final String ATTR_CLASS = "class";
    public static final String ATTR_TYPE = "type";
    /**
     * a sentence in main body
     */
    public static final String TAG_REGION = "region";
    public static final String ATTR_REGION_CLASS_VALUE_UNKNOWN = "unknown";
    public static final String ATTR_REGION_CLASS_VALUE_TEXTCHUNK = "DoCO:TextChunk";

    /**
     * a sentence
     */
    public static final String TAG_S = "s";

    /**
     * a marker is used for indicating breaks (e.g. paragraph end, page break, etc.)
     */
    public static final String TAG_MARKER = "marker";
    /**
     * 'block' value for 'type' attribute in 'marker' tag indicates end of paragraph
     */
    public static final String ATTR_MARKER_TYPE_VALUE_BLOCK = "block";

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
        private boolean captureText = false;
        private boolean isInsideSentence = false;

        private String documentId = null;
        private int paragraphBegin = -1;
        private int sentenceBegin = -1;

        //todo: add a '.' mark at the end of paragraph if it doesn't have one?
        //todo: retain footer and header in the jcas with proper annotation
        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                                 Attributes aAttributes)
                throws SAXException
        {
            if (TAG_ARTICLE_TITLE.equals(aName)) {
                captureText = true;
            }else if (TAG_REGION.equals(aName)){
                isInsideSentence = false;
                if(ATTR_REGION_CLASS_VALUE_UNKNOWN.equals(aAttributes.getValue(ATTR_CLASS)) ||
                        ATTR_REGION_CLASS_VALUE_TEXTCHUNK.equals(aAttributes.getValue(ATTR_CLASS))) {
                    paragraphBegin = getBuffer().length();
                    captureText = true;
                    isInsideSentence = true;
                }
            }else if(TAG_S.equals(aName)){
                sentenceBegin = getBuffer().length();
            }else if(TAG_MARKER.equals(aName)){
                if(ATTR_MARKER_TYPE_VALUE_BLOCK.equals(aAttributes.getValue(ATTR_TYPE))){
                    //paragraph end indicator
                    makeParagraph();
                }
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
                getBuffer().setLength(0);
                captureText = false;
            }else if (TAG_REGION.equals(aName)){
                if(isInsideSentence) {
                    makeParagraph();
                    captureText = false;
                }
            }else if(TAG_S.equals(aName)){
                new Sentence(getJCas(), sentenceBegin, getBuffer().length()).addToIndexes();
                sentenceBegin = -1;
            }
        }

        private void makeParagraph() {
            if(isParamAppendNewLineAfterParagraph){
                int emptySentenceStart = getBuffer().length();
                getBuffer().append(System.lineSeparator());
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