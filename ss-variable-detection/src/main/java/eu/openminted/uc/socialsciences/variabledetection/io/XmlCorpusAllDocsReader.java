/**
 * Copyright 2007-2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package eu.openminted.uc.socialsciences.variabledetection.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.io.SingleLabelReaderBase;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Collection reader for Variable mention XML file containing sentences from several documents
 */
public class XmlCorpusAllDocsReader
    extends SingleLabelReaderBase
{
    private List<TargetOutcomePair> targetOutcomePairList = new ArrayList<>();
    private String language = "";
    private Resource res;
    private int count = 0;
    
    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        if (targetOutcomePairList.isEmpty()) {
            res = nextFile();
            InputStream is = null;

            try {
                is = res.getInputStream();
                process(is);
            }
            finally {
                closeQuietly(is);
            }
            count = 0;
        }

        initCas(aCAS, res, Integer.toString(count));
        
        try {
            TargetOutcomePair pair = targetOutcomePairList.get(0);
            targetOutcomePairList.remove(0);
            JCas jcas = aCAS.getJCas();
            aCAS.getJCas().setDocumentText(pair.target);

            aCAS.setDocumentLanguage(language);
            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(pair.outcome);
            outcome.setWeight(getTextClassificationOutcomeWeight(jcas));
            outcome.addToIndexes();
            
            new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length()).addToIndexes();
            ++count;
        }
        catch (CASRuntimeException | CASException e) {
            throw new CollectionException(e);
        }
        
        
    }

    private void process(InputStream aInputStream) throws IOException
    {
        DocumentBuilder xmlDocumentBuilder;
        DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
            document = xmlDocumentBuilder.parse(new InputSource(aInputStream));
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }

        if (document != null) {
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList docNodes = (NodeList) xpath.compile("/docs//doc").evaluate(document,
                        XPathConstants.NODESET);
                Node docNode = docNodes.item(0);
                while (docNode != null) {
                    if (language.equals("")) {
                        NamedNodeMap docAttributes = docNode.getAttributes();
                        language = docAttributes.getNamedItem("lang").getTextContent();
                    }
                    
                    NodeList sentenceNodes = (NodeList) xpath.compile(".//s").evaluate(docNode, XPathConstants.NODESET);
                    Node sentenceNode = sentenceNodes.item(0);
                    while (sentenceNode != null) {
                        if (!sentenceNode.getTextContent().trim().isEmpty()) {
                            NamedNodeMap sentenceAttributes = sentenceNode.getAttributes();
                            String correct = sentenceAttributes.getNamedItem("correct").getTextContent();
                            if (!correct.equals("NoSkip")) {
                                if (!correct.equals("No")) {
                                    correct = "Yes";
                                }
                                TargetOutcomePair pair = new TargetOutcomePair();
                                pair.target = normalizeWhitespaces(sentenceNode.getTextContent());
                                pair.outcome = correct;
                                targetOutcomePairList.add(pair);
                            }
                        }
                        
                        sentenceNode = sentenceNode.getNextSibling();
                    }
                    
                    docNode = docNode.getNextSibling();
                }
            }
            catch (XPathExpressionException e) {
                throw new IOException(
                        "Problem with parsing the expression: " + e.getLocalizedMessage(), e);
            }
        }
    }
    
    private String normalizeWhitespaces(String input)
    {
        return input.replaceAll("\\s+", " ");
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas) throws CollectionException
    {
        return null;
    }
    
    @Override
    public boolean hasNext()
            throws IOException, CollectionException
    {
        return super.hasNext() || !targetOutcomePairList.isEmpty();
    }
    
    private class TargetOutcomePair
    {
        public String target;
        public String outcome;
    }
}
