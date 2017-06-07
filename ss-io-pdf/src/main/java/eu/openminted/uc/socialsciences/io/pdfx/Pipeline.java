package eu.openminted.uc.socialsciences.io.pdfx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import org.apache.uima.UIMAException;
import org.kohsuke.args4j.Option;

public class Pipeline
{
	@Option(name="-i", usage="Path to input PDF document(s) (file or directory)", required = true)
	private String input = null;

	@Option(name="-o", usage="Output directory", required = true)
	private String output = null;

	@Option(name="-overwrite", usage = "(Optional) if this option is set, program will overwrite files " +
			" that already exist in output directory.")
	private boolean overwriteOutput = false;

	@Option(name="-lang", usage="Language of input documents. Possible values: "
			+ PdfxXmlToXmiConverter.LANGUAGE_CODE_EN + ", " + PdfxXmlToXmiConverter.LANGUAGE_CODE_DE, required = true)
	private String language = null;

	@Option(name="-home", usage = "Path to application home where required files (e.g. dictionary files) are located",
			required = true)
	private String homePath = null;

	/**
	 * The pipeline for converting a collection of PDF documents to XMI format
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		new Pipeline().run(args);
	}

	public void run()
	{
		assertFields();
		runInternal();
	}

	private void assertFields()
	{
		if(input==null)
			throw new IllegalArgumentException("input can not be null!");
		if(output==null)
			throw new IllegalArgumentException("output can not be null!");
		if(language==null)
			throw new IllegalArgumentException("language can not be null!");
		if(homePath==null)
			throw new IllegalArgumentException("homePath can not be null!");
	}

	private void run(String[] args)
	{
		new CommandLineArgumentHandler().parseInput(args, this);

		runInternal();
	}

	private void runInternal() {
		PdfxXmlCreator pdfxXmlCreator = new PdfxXmlCreator();
		pdfxXmlCreator.setOverwriteOutput(overwriteOutput);

		try
		{
			List<Path> pdfxOutFiles = pdfxXmlCreator.process(input, output);
			System.out.println(pdfxOutFiles.size() + " files have been processed by Pdfx.");

			PdfxXmlToXmiConverter pdfxXmlToXmiConverter = new PdfxXmlToXmiConverter(homePath, overwriteOutput);
			pdfxXmlToXmiConverter.convertToXmi(output, output, language);
		} catch (IOException | UIMAException e)
		{
			e.printStackTrace();
		}
	}

	public void setInput(String input)
	{
		this.input = input;
	}
	public String getInput()
	{
		return input;
	}
	public void setOutput(String output)
	{
		this.output = output;
	}
	public String getOutput()
	{
		return output;
	}
	public void setOverwriteOutput(boolean value)
	{
		this.overwriteOutput = value;
	}
	public boolean isOverwriteOutput()
	{
		return overwriteOutput;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}
	public String getLanguage()
	{
		return language;
	}

	public void setHomePath(String homePath)
	{
		this.homePath = homePath;
	}
	public String getHomePath()
	{
		return homePath;
	}
}