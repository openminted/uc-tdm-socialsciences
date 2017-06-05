package eu.openminted.uc.socialsciences.io.pdf.cermine;


import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.jdom.Element;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class CerminePdfReader
    extends ResourceCollectionReaderBase
{

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;

        try
        {
            JCas jcas = aCAS.getJCas();

            is = res.getInputStream();

            // Create handler
            ContentExtractor extractor = new ContentExtractor();
            extractor.setPDF(is);
            Element result = extractor.getContentAsNLM();
            Handler handler = new Handler();
            handler.process(result, jcas);

            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
        }
        catch (CASException e)
        {
            throw new CollectionException(e);
        } catch (AnalysisException e)
        {
            //FIXME
            e.printStackTrace();
        } finally
        {
            closeQuietly(is);
        }
    }

    private class Handler
    {
        StringBuilder sb;
        String title;

        public void process(Element root, JCas jcas)
        {
            sb = new StringBuilder();
            parse(root, jcas);
            jcas.setDocumentText(sb.toString());
            DocumentMetaData.get(jcas).setDocumentTitle(title);
            DocumentMetaData.get(jcas).setDocumentId(title);
        }

        private void parse(Element root, JCas jcas)
        {
            for (Object node : root.getChildren())
            {
                Element element = (Element) node;
                if (element.getName().equals("front"))
                {
                    parseHeader(element, jcas);
                }
                else if (element.getName().equals("body"))
                {
                    parseBody(element, jcas);
                }
                else if (element.getName().equals("back"))
                {
                    //TODO
                }
            }
        }

        private void parseBody(Element root, JCas jcas)
        {
            for (Object node : root.getChildren())
            {
                Element element = (Element) node;
                switch (element.getName())
                {
                    case "sec":
                        parseBody(element, jcas);
                        break;
                    case "p":
                        sb.append(" ").append(element.getValue().trim());
                        break;
                    case "title":
                        sb.append(" ").append(element.getValue().trim());
                        break;
                }
            }
        }

        private void parseHeader(Element root, JCas jcas)
        {
            for (Object node : root.getChildren())
            {
                Element element = (Element) node;
                if (element.getName().equals("article-meta"))
                {
                    for (Object metaNode : element.getChildren())
                    {
                        Element metaElement = (Element) metaNode;
                        if (metaElement.getName().equals("title-group"))
                        {
                            for (Object titleNode : metaElement.getChildren())
                            {
                                Element titleGroupElement = (Element) titleNode;
                                if (titleGroupElement.getName().equals("article-title"))
                                {
                                    title = titleGroupElement.getValue();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
