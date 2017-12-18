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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VariableFileReader
{
    public static Map<String, String> getVariables(String file) throws IOException
    {
        Map<String, String> variableMap = new HashMap<>();

        InputStream inputStream = new FileInputStream(file);
        DocumentBuilder xmlDocumentBuilder;
        DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
            document = xmlDocumentBuilder.parse(new InputSource(inputStream));
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }

        if (document != null) {
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList variableNodes = (NodeList) xpath.compile("/variables//variable").evaluate(document,
                        XPathConstants.NODESET);

                Node variableNode = variableNodes.item(0);
                while (variableNode != null) {
                    if (!variableNode.getTextContent().trim().equals("")) {
                        NamedNodeMap attributes = variableNode.getAttributes();
                        String variableId = attributes.getNamedItem("v_id").getTextContent().trim();
                        
                        StringBuilder sb = new StringBuilder();
                        
                        Node labelNode = (Node) xpath.compile("./v_label").evaluate(variableNode, XPathConstants.NODE);
                        sb.append(labelNode.getTextContent().trim()).append(" ");
                        
                        Node topicNode = (Node) xpath.compile("./v_topic").evaluate(variableNode, XPathConstants.NODE);
                        if (topicNode != null) {
                            sb.append(topicNode.getTextContent().trim()).append(" ");
                        }
                        
                        Node questionNode = (Node) xpath.compile("./v_question").evaluate(variableNode, XPathConstants.NODE);
                        sb.append(questionNode.getTextContent().trim()).append(" ");
                        
                        Node subquestionNode = (Node) xpath.compile("./v_subquestion").evaluate(variableNode, XPathConstants.NODE);
                        sb.append(subquestionNode.getTextContent().trim()).append(" ");
                        
                        NodeList answerNodes = (NodeList) xpath.compile("./v_answer").evaluate(variableNode, XPathConstants.NODESET);
                        Node answerNode = answerNodes.item(0);
                        while (answerNode != null) {
                            sb.append(answerNode.getTextContent().trim()).append(" ");
                            
                            answerNode = answerNode.getNextSibling();
                        }
                        variableMap.put(variableId, sb.toString());
                    }
                    
                    variableNode = variableNode.getNextSibling();
                }
            }
            catch (XPathExpressionException e) {
                throw new IOException(
                        "Problem with parsing the expression: " + e.getLocalizedMessage(), e);
            }
        }
        return variableMap;
    }
    
    private String normalizeWhitespaces(String input)
    {
        return input.replaceAll("\\s+", " ");
    }
}
