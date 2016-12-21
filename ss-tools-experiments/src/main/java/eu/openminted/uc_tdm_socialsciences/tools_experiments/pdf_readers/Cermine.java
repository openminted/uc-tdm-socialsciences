/*
 * This file is part of OpenMinTeD project.
 * Copyright (c) 2016
 * 	GESIS – Leibniz-Institute for the Social Sciences
 * 	Ubiquitous Knowledge Processing (UKP) Lab at Technische Universität Darmstadt
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openminted.uc_tdm_socialsciences.tools_experiments.pdf_readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pl.edu.icm.cermine.PdfNLMContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import util.Property;

public class Cermine {
	private static final String thiz = "cermine";

	public static void main(String args[]) throws AnalysisException, IOException {
		// Test documents can be obtained from the following URL
		// https://drive.google.com/file/d/0Bx1HpGFsGYhnbFh2SjAzU3p3X3c/view?usp=sharing
		final String[] fileNames = new String[] { "2819", "16597", "17527", "18479", "27939", "27940", "28005", "28189",
				"28681", "28750", "28835", "28862", "29294", "31259", "31451", "31457", "44921" };

		File outDir = new File(Property.load(Property.PROPERTY_OUT_BASE) + thiz + "/");
		//noinspection ResultOfMethodCallIgnored
		outDir.mkdir();

		for (String entry : fileNames) {
			String input = Property.load(Property.PROPERTY_DOC_FOLDER) + entry + ".pdf";
			String output = outDir + "/" + entry + ".xml";

			convertPdfToXml(input, output);
		}
	}

	private static void convertPdfToXml(final String input, final String output)
			throws AnalysisException, IOException {
		PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
		InputStream inputStream = new FileInputStream(input);
		Element result = extractor.extractContent(inputStream);
		OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(result, outputStream);
	}

	@SuppressWarnings("unused")
	private static void printChildrenContents(Element result) {
		for (Object child : result.getChildren()) {
			Element childE = (Element) child;
			if (childE.getChildren().size() == 0) {
				System.out.println(childE.getValue());
			} else {
				printChildrenContents(childE);
			}
		}
	}
}
