package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UIMAException;

public class Pipeline {

	public static void main(String[] args) {
		String inputPath = null, outputPath = null;

		switch (args.length) {
		case 0:
			break;
		case 2:
			outputPath = args[1];
		case 1:
			inputPath = args[0];
			break;
		default:
			System.err.println(
					"Illegal number of command line arguments given. Provide input path as first argument (mandatory) and output path as second argument (optional).");
			System.exit(0);
		}

		Scanner scanner = new Scanner(System.in);
		while (null == inputPath || inputPath.length() < 1) {
			System.out.println(
					"Please provide path to input directory containing pdf files or to the file you want to process:");
			inputPath = scanner.nextLine();
		}

		PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();
		System.out.println("Overwrite existing output? (y/n)");
		String answer = scanner.nextLine();
		scanner.close();

		switch (answer) {
		case "y":
		case "yes":
			pdfxXmlCreator.setOverwriteOutput(true);
			break;
		case "n":
		case "no":
			pdfxXmlCreator.setOverwriteOutput(false);
			break;
		default:
			System.out.println("Undefined answer. Setting overwriteOutput to false.");
		}

		try {
			List<Path> pdfxOutFiles = pdfxXmlCreator.process(inputPath, null);
			System.out.println(pdfxOutFiles.size() + " files have been processed by pdfx.");

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
