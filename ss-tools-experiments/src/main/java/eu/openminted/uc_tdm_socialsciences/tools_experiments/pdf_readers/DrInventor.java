/*
    Copyright 2016
        GESIS – Leibniz-Institute for the Social Sciences
        Ubiquitous Knowledge Processing (UKP) Lab at Technische Universität Darmstadt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package eu.openminted.uc_tdm_socialsciences.tools_experiments.pdf_readers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import util.Property;

public class DrInventor
{
	private static final String thiz = "drInventor";

    public static void main(String args[])
        throws IOException, DRIexception
    {
        //Set this to the correct path pointing to DRI properties file
        Factory.setDRIPropertyFilePath(Property.load(Property.PROPERTY_DRINVENTOR_PROPERTIES));

        //Test documents can be obtained from the following URL
        //  https://drive.google.com/file/d/0Bx1HpGFsGYhnbFh2SjAzU3p3X3c/view?usp=sharing
        final String[] fileNames = new String[] {
                "2819", "16597", "17527", "18479", "27939",
                "27940", "28005", "28189", "28681", "28750", "28835", "28862", "29294",
                "31259", "31451", "31457", "44921" };

		File outDir = new File(Property.load(Property.PROPERTY_OUT_BASE) + thiz + "/");
        //noinspection ResultOfMethodCallIgnored
        outDir.mkdirs();

        for (String entry : fileNames) {
			String input = Property.load(Property.PROPERTY_DOC_FOLDER) + entry + ".pdf";
			String output = outDir + "/" + entry + ".xml";

            convertPdfToXml(input, output);
        }
    }

    private static void convertPdfToXml(final String input, final String output)
        throws IOException, DRIexception
    {
        //or Document doc_PDFpaperURL = Factory.getPDFloader().parsePDF(new URL("http://www2007.org/workshops/paper_45.pdf"));
        System.out.println("parsing [" + input + "]");
        Document doc_PDFpaperFILE = Factory.getPDFloader().parsePDF(input);

        OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(output),
                "UTF-8");
        outputStream.write(doc_PDFpaperFILE.getXMLString());
        outputStream.close();

        //clean up the document cache from memory
        doc_PDFpaperFILE.cleanUp();
    }
}
