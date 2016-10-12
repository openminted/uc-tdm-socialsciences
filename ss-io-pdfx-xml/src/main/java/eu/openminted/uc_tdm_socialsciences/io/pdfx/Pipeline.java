package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UIMAException;

public class Pipeline {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Please provide path to input directory containing pdf files.");
			System.exit(0);
		}

		String inputPath = args[0];

		PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();
		pdfxXmlCreator.setOverwriteOutput(true);
		try {
			List<Path> pdfxOutFiles = pdfxXmlCreator.process(inputPath, null);

			for (Path p : pdfxOutFiles) {
				PdfxXmlToXmiConverter.convert(p.toString(), FilenameUtils.getBaseName(p.toString()) + ".xmi");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		}
	}
}
