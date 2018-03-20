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

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DataFormat;
import eu.openminted.share.annotations.api.ResourceInput;
import eu.openminted.share.annotations.api.constants.ComponentConstants;
import eu.openminted.uc.socialsciences.variabledetection.type.VariableMention;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Collection reader for Variable mention XML corpus
 */
//@ResourceMetaData(name = "Variable Mention Corpus Reader")
//@MimeTypeCapability({ MimeTypes.APPLICATION_XML })
//@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
//// OMTD-SHARE annotations
//@Component(value = ComponentConstants.ComponentTypeConstants.reader)
//@ResourceInput(type = "corpus", dataFormat = @DataFormat(dataFormat = "xml", mimeType = "application/xml"), encoding = "UTF-8", keyword = "xml")
public class XmlCorpusReader
    extends ResourceCollectionReaderBase
{
    private int sentenceBegin = 0;
    private int sentenceEnd = 0;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;

        try {
            is = res.getInputStream();
            process(is, aCAS);

            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
        }
        finally {
            closeQuietly(is);
        }
    }

    private void process(InputStream aInputStream, CAS aCAS) throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        String language;

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
                Node sampleNode = (Node) xpath.compile("//testset/sample").evaluate(document,
                        XPathConstants.NODE);
                if (sampleNode == null) {
                    sampleNode = (Node) xpath.compile("//testset/topic/sample").evaluate(document,
                            XPathConstants.NODE);
                }

                Node docNode = (Node) xpath.compile("./doc").evaluate(sampleNode, XPathConstants.NODE);
                NamedNodeMap docAttributes = docNode.getAttributes();
                language = docAttributes.getNamedItem("lang").getTextContent();
                
                Node titleNode = (Node) xpath.compile("./doc_title").evaluate(docNode,
                        XPathConstants.NODE);
                DocumentMetaData metadata = DocumentMetaData.get(aCAS.getJCas());
                metadata.setDocumentTitle(titleNode.getTextContent());
                
                NodeList sentenceNodes = (NodeList) xpath.compile(".//s").evaluate(docNode,
                        XPathConstants.NODESET);
                Node sentenceNode = sentenceNodes.item(0);
                int sentenceStart = 0;
                while (sentenceNode != null) {
                    if (!sentenceNode.getTextContent().trim().isEmpty()) {
                        NamedNodeMap attributes = sentenceNode.getAttributes();
                        stringBuilder.append(normalizeWhitespaces(sentenceNode.getTextContent().trim()));

                        Sentence sentence = new Sentence(aCAS.getJCas(), sentenceStart,
                                stringBuilder.length());
                        sentence.setId(attributes.getNamedItem("id").getTextContent());
                        sentence.addToIndexes();

                        if (attributes.getNamedItem("correct").getTextContent().equals("Yes")) {
                            sentenceBegin = sentenceStart;
                            sentenceEnd = stringBuilder.length();
                        }

                        stringBuilder.append(" ");
                        sentenceStart = stringBuilder.length();
                    }
                    sentenceNode = sentenceNode.getNextSibling();
                }

                NodeList variableNodes = (NodeList) xpath.compile(".//variable")
                        .evaluate(sampleNode, XPathConstants.NODESET);
                Node variableNode = variableNodes.item(0);
                while (variableNode != null) {
                    if (!variableNode.getTextContent().trim().isEmpty()) {
                        NamedNodeMap attributes = variableNode.getAttributes();
                        String correct = attributes.getNamedItem("correct").getTextContent();
                        String variableId = attributes.getNamedItem("v_id").getTextContent();
                        Node labelNode = (Node) xpath.compile("./v_label").evaluate(variableNode,
                                XPathConstants.NODE);
                        String label = labelNode.getTextContent().trim();
                        Node questionNode = (Node) xpath.compile("./v_question").evaluate(variableNode,
                                XPathConstants.NODE);
                        String question = normalizeWhitespaces(questionNode.getTextContent().trim());
                        Node subQuestionNode = (Node) xpath.compile("./v_subquestion")
                                .evaluate(variableNode, XPathConstants.NODE);
                        String subQuestion = "";
                        if (subQuestionNode != null) {
                            subQuestion = normalizeWhitespaces(subQuestionNode.getTextContent().trim());
                        }
                        // TODO implement answer extraction
                        String answer = "";

                        VariableMention variableMention = new VariableMention(aCAS.getJCas(),
                                sentenceBegin, sentenceEnd);
                        variableMention.setVariableId(variableId);
                        variableMention.setCorrect(correct);
                        variableMention.setLabel(label);
                        variableMention.setQuestion(question);
                        variableMention.setSubQuestion(subQuestion);
                        variableMention.setAnswers(answer);
                        variableMention.addToIndexes();
                    }
                    variableNode = variableNode.getNextSibling();
                }

                aCAS.getJCas().setDocumentText(stringBuilder.toString());
                aCAS.setDocumentLanguage(language);
            }
            catch (XPathExpressionException e) {
                throw new IOException(
                        "Problem with parsing the expression: " + e.getLocalizedMessage(), e);
            }
            catch (CASException e) {
                throw new IOException(e);
            }
        }
    }
    
    private String normalizeWhitespaces(String input)
    {
        return input.replaceAll("\\s+", " ");
    }
}
