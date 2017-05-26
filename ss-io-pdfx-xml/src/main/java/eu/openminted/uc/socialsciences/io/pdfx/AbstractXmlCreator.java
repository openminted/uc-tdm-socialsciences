package eu.openminted.uc.socialsciences.io.pdfx;

import eu.openminted.uc.socialsciences.io.pdf.XmlCreator;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractXmlCreator implements XmlCreator
{
    private final Logger LOG = Logger.getLogger(getClass());
    protected List<String> skippedFileList;
    @Option(name = "-overwrite", usage = "(Optional) if this option is set, program will overwrite files " +
            " that already exist in output directory.")
    protected boolean overwriteOutput = false;

    @Override
    public List<String> getSkippedFileList() {
        List<String> result = new ArrayList<>();
        result.addAll(skippedFileList);
        return result;
    }

    /**
     * Processes a single pdf file or all pdf files in a given directory with
     * pdfx service which
     * converts them into XML files. Stores the XML file(s) in a given output
     * directory. If outputPathString is null, the input directory will be used.
     *
     * @param inputPathString
     *            Path to pdf file to be processed. Or: The directory that
     *            contains PDF files. It is scanned
     *            recursively, non-pdf files will be ignored.
     * @param outputPathString
     *            The directory where the output XML file(s) will be stored.
     *            if set to <i>null</i> generated files will be written to the
     *            input directory.
     * @return a list of all the Paths of the generated output files
     */
    @Override
    public List<Path> process(String inputPathString, String outputPathString) {
        Path inputPath;
        skippedFileList = new ArrayList<>();
        try {
            inputPath = Paths.get(inputPathString);
            Path outputPath = outputPathString == null ? null : Paths.get(outputPathString);
            return process(inputPath, outputPath);
        } catch (InvalidPathException e) {
            LOG.error("Given String cannot be converted to valid path.", e);
            return null;
        }
    }

    /**
    * Processes all pdf files in a given directory with pdfx service which
    * converts them into XML files. Stores the XML files in a given output
    * directory.
    * @param inputPath
    * The directory that contains PDF files. It is scanned
    * recursively, non-pdf files will be ignored.
    * @param outputDirectoryPath
    * The directory where the output XML files will be stored.
    * @return a list of all the Paths of the generated output files
    */
    protected abstract List<Path> process(Path inputPath, Path outputDirectoryPath);

    /**
     * @return true iff parameter for overwriting existing output is set to true
     */
    @Override
    public boolean isOverwriteOutput() {
        return overwriteOutput;
    }

    /**
     * Set the parameter which controls if already existing output files should
     * be overwritten.
     *
     * @param overwriteOutput
     *            set to true if you want to overwrite existing output,
     *            otherwise to false.
     */
    @Override
    public void setOverwriteOutput(boolean overwriteOutput) {
        this.overwriteOutput = overwriteOutput;
    }
}
