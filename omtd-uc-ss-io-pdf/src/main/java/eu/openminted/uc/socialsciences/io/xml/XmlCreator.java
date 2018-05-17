package eu.openminted.uc.socialsciences.io.xml;

import java.nio.file.Path;
import java.util.List;

/**
 * Classes implementing this interface provide PDF to XML conversion
 *
 */
public interface XmlCreator
{
    List<String> getSkippedFileList();

    List<Path> process(String inputPathString, String outputPathString);

    boolean isOverwriteOutput();

    void setOverwriteOutput(boolean overwriteOutput);
}
