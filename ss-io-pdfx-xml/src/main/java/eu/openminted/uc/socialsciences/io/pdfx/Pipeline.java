package eu.openminted.uc.socialsciences.io.pdfx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UIMAException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Pipeline {

	@Option(name="-i", usage="Path to input PDF document(s) (file or directory)", required = true)
	private String input = null;

	@Option(name="-o", usage="Output directory", required = true)
	private String output = null;

	@Option(name="-overwrite", usage = "(Optional) if set to true, program will overwrite files that already exist " +
			"in output directory.")
	private boolean overwriteOutput = false;

	@Option(name="-lang", usage="Language of input documents. Possible values: "
			+ PdfxXmlToXmiConverter.LANGUAGE_CODE_EN + ", " + PdfxXmlToXmiConverter.LANGUAGE_CODE_DE, required = true)
	private String language = null;

	private void parseInput(String[] args)
	{
		CmdLineParser parser = new CmdLineParser(this);
		try
		{
			parser.parseArgument(args);
		} catch( CmdLineException e )
		{
			System.err.println(e.getMessage());
			System.err.println(String.format("java %s [options...] arguments...", Pipeline.class.getSimpleName()));
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			System.exit(1);
		}
	}

	/**
	 * The pipeline for converting a collection of PDF documents to XMI format
	 */
	public static void main(String[] args) {
		new Pipeline().run(args);
	}

	private void run(String[] args)
	{
		parseInput(args);

		PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();
		pdfxXmlCreator.setOverwriteOutput(overwriteOutput);

		try {
			List<Path> pdfxOutFiles = pdfxXmlCreator.process(input, output);
			System.out.println(pdfxOutFiles.size() + " files have been processed by Pdfx.");

			for (Path p : pdfxOutFiles) {
				new PdfxXmlToXmiConverter().
						convertToXmi(p.toString(), FilenameUtils.getBaseName(p.toString()) + ".xmi",
								language);
			}

		} catch (IOException | UIMAException e) {
			e.printStackTrace();
		}
	}
}
