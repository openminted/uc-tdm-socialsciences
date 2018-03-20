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
package eu.openminted.uc.socialsciences.variabledetection.uima.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import eu.openminted.uc.socialsciences.variabledetection.type.GoldVariableMention;

/**
 * Collection reader for Variable mention XML file containing sentences from several documents
 */
public class XmlCorpusAllDocsReader
    extends JCasResourceCollectionReader_ImplBase
{
    public static final String PARAM_INCLUDE_TARGET_AND_OUTCOME = "includeTargetAndOutcome";
    @ConfigurationParameter(name = PARAM_INCLUDE_TARGET_AND_OUTCOME, defaultValue = "false")
    private boolean includeTargetAndOutcome;

    public static final String PARAM_INCLUDE_GOLD = "includeGold";
    @ConfigurationParameter(name = PARAM_INCLUDE_GOLD, defaultValue = "false")
    private boolean includeGold;

    private Deque<DataRecord> dataQueue = new LinkedList<>();
    private Resource res;
    private int count = 0;
    
    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        if (dataQueue.isEmpty()) {
            res = nextFile();
            try (InputStream is = res.getInputStream()) {
                fillDataQueue(is);
            }
            count = 0;
        }

        initCas(aJCas, res, Integer.toString(count));
        
        try {
            DataRecord data = dataQueue.pop();
            
            aJCas.setDocumentText(data.text);

            if (includeTargetAndOutcome) {
                // Add the gold label
                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
                outcome.setOutcome(data.variablePresent ? "Yes" : "No");
                outcome.setWeight(1.0);
                outcome.addToIndexes();

                // Mark the entire document as "this is what we want to classify"
                new TextClassificationTarget(aJCas, 0, aJCas.getDocumentText().length())
                        .addToIndexes();
            }
            
            if (includeGold && data.variablePresent) {
                for (String varId : getMatchingVariableIds(data.originalLabel)) {
                    GoldVariableMention gold = new GoldVariableMention(aJCas, 0,
                            aJCas.getDocumentText().length());
                    gold.setVariableId(varId);
                    gold.addToIndexes();
                }
            }
            
            ++count;
        }
        catch (CASRuntimeException e) {
            throw new CollectionException(e);
        }
    }

    private List<String> getMatchingVariableIds(String aOriginalLabel)
    {
        List<String> variableIDs = new ArrayList<>();
        String labels = StringUtils.substring(aOriginalLabel, 1, -1);
        String[] individualLabels = labels.split(",");
        for (String l : individualLabels) {
            String[] pair = l.split("-");
            if ("Yes".equals(pair[1])) {
                variableIDs.add(pair[0]);
            }
        }
        
        return variableIDs;
    }

    private void fillDataQueue(InputStream aInputStream) throws IOException
    {
        Document document;
        try {
            DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
            document = xmlDocumentBuilder.parse(new InputSource(aInputStream));
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList docNodes = (NodeList) xpath.compile("/docs//doc").evaluate(document,
                    XPathConstants.NODESET);

            // Read all the sentences from all the documents and create target/outcome pairs
            // for them.
            Node docNode = docNodes.item(0);
            while (docNode != null) {
                // Check if the language configuration matches the data
                NamedNodeMap docAttributes = docNode.getAttributes();
                if (docAttributes != null && docAttributes.getNamedItem("lang") != null
                        && !Objects.equals(getLanguage(),
                                docAttributes.getNamedItem("lang").getTextContent())) {
                    getLogger().warn("Component language [" + getLanguage()
                            + "] does not match sentence language ["
                            + docAttributes.getNamedItem("lang").getTextContent() + "].");
                }
                
                NodeList sentenceNodes = (NodeList) xpath.compile(".//s").evaluate(docNode,
                        XPathConstants.NODESET);
                Node sentenceNode = sentenceNodes.item(0);
                while (sentenceNode != null) {
                    if (!sentenceNode.getTextContent().trim().isEmpty()) {
                        NamedNodeMap sentenceAttributes = sentenceNode.getAttributes();
                        String correct = sentenceAttributes.getNamedItem("correct")
                                .getTextContent().trim();
                        
                        if (!correct.equals("NoSkip")) {
                            // Here we are only interested in whether there is a variable or
                            // not. So if there is at least one gold match, then we consider
                            // the sentence to contain a variable mention. Thus e.g.
                            // correct="[290-Yes,295-No,251-No]" gets interpreted as "Yes".
                            boolean variablePresent = !correct.equals("No");
                            
                            DataRecord data = new DataRecord();
                            data.text = normalizeWhitespaces(sentenceNode.getTextContent());
                            data.variablePresent = variablePresent;
                            data.originalLabel = correct;
                            dataQueue.addLast(data);
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
    
    private String normalizeWhitespaces(String input)
    {
        return input.replaceAll("\\s+", " ").trim();
    }

    @Override
    public boolean hasNext()
            throws IOException, CollectionException
    {
//        if (count >= 20) {
//            return false;
//        }
        
        getLogger().info("Processed: " + count + " / " + (dataQueue.size() + count));
        return super.hasNext() || !dataQueue.isEmpty();
    }
    
    private static class DataRecord
    {
        String text;
        boolean variablePresent;
        String originalLabel;
    }
}
