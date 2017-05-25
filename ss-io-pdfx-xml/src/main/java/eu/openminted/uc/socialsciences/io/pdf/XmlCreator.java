package eu.openminted.uc.socialsciences.io.pdf;

import java.nio.file.Path;
import java.util.List;

public interface XmlCreator
{
    List<String> getSkippedFileList();

    List<Path> process(String inputPathString, String outputPathString);

    boolean isOverwriteOutput();

    void setOverwriteOutput(boolean overwriteOutput);
}
