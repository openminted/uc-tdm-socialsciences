package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import org.apache.cxf.common.i18n.Exception;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.xml.sax.Attributes;

@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
                "webanno.custom.Reference"
        })
public class PdfxXmlReaderWithHyphenationRemover
        extends PdfxXmlReader
{
    @Override
    protected Handler newSaxHandler()
    {
        return new PdfxXmlHandlerWithHyphenationRemover();
    }

    public class PdfxXmlHandlerWithHyphenationRemover
            extends PdfxXmlHandler
    {
        private final Logger logger = Logger.getLogger(PdfxXmlHandlerWithHyphenationRemover.class);

        StringBuilder tempBuffer = null;
        int unprocessedBeginOffset = -1;
        StringHyphenationRemover hyphenationRemover;

        public PdfxXmlHandlerWithHyphenationRemover()
        {
            try{
                hyphenationRemover = new StringHyphenationRemover();
            }catch(org.apache.uima.resource.ResourceInitializationException x){
                //todo // FIXME: 23.08.16
                logger.error("StringHyphenationRemover initialization failed. Handler will remove hyphenations.");
                hyphenationRemover = null;
            }
        }

        private boolean isHyphenationRemoverInitialized() {
            return hyphenationRemover != null;
        }

        @Override
        protected void startElementS()
        {
            super.startElementS();
            unprocessedBeginOffset = getBuffer().length();
        }

        @Override
        protected void endElementS()
        {
            if (isHyphenationRemoverInitialized()){
                String unprocessedString = getBuffer().substring(unprocessedBeginOffset);
                try{
                    String fixedString = hyphenationRemover.process(unprocessedString);
                    String preprocessedSubstring = getBuffer().substring(0, unprocessedBeginOffset);
                    getBuffer().setLength(0);
                    getBuffer().append(preprocessedSubstring).append(fixedString);
                } catch (AnalysisEngineProcessException e) {
                    logger.error("An exception occurred while removing hyphenations from the string, " +
                            "document text will be left untouched. String: " + unprocessedString);
                }
            }
            super.endElementS();
        }

        @Override
        protected void startElementRef(Attributes aAttributes)
        {
            if (isHyphenationRemoverInitialized()){
                String unprocessedString = getBuffer().substring(unprocessedBeginOffset);
                try{
                    String fixedString = hyphenationRemover.process(unprocessedString);
                    String preprocessedSubstring = getBuffer().substring(0, unprocessedBeginOffset);
                    getBuffer().setLength(0);
                    getBuffer().append(preprocessedSubstring).append(fixedString);
                } catch (AnalysisEngineProcessException e) {
                    logger.error("An exception occurred while removing hyphenations from the string, " +
                            "document text will be left untouched. String: " + unprocessedString);
                }
            }
            super.startElementRef(aAttributes);
        }

        @Override
        protected void endElementRef()
        {
            super.endElementRef();
            unprocessedBeginOffset = getBuffer().length();
        }
    }
}