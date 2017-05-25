package eu.openminted.uc.socialsciences.io.pdf.cermine;

import eu.openminted.uc.socialsciences.io.pdf.XmlCreator;

import java.nio.file.Path;
import java.util.List;

/**
 *
 */
public class CermineXmlCreator implements XmlCreator
{
    @Override
    public List<String> getSkippedFileList()
    {
        return null;
    }

    @Override
    public List<Path> process(String inputPathString, String outputPathString)
    {
        return null;
    }

    @Override
    public boolean isOverwriteOutput()
    {
        return false;
    }

    @Override
    public void setOverwriteOutput(boolean overwriteOutput)
    {

    }
}
