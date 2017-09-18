package eu.openminted.uc.socialsciences.variabledetection.resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.tudarmstadt.ukp.dkpro.lexsemresource.core.ResourceFactory;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

/**
 * Copied from {@link ResourceFactory}
 */
public class KnowledgeBaseFactory
{
    public static final String ENV_DKPRO_HOME = "DKPRO_HOME";
    public final static String CONFIG_FILE = "resources.xml";

    private static KnowledgeBaseFactory loader;

    private FileSystemXmlApplicationContext context;

    public static synchronized KnowledgeBaseFactory getInstance() throws ResourceLoaderException
    {
        if (loader == null) {
            List<String> locs = new ArrayList<String>();
            URL resourceXmlUrl = null;

            // Check in workspace
            try {
                File f = new File(getWorkspace(), CONFIG_FILE);
                if (f.isFile()) {
                    try {
                        resourceXmlUrl = f.toURI().toURL();
                    }
                    catch (MalformedURLException e) {
                        throw new ResourceLoaderException(e);
                    }
                }
                locs.add(f.getAbsolutePath());
            }
            catch (IOException e) {
                locs.add("DKPro workspace not available");
            }

            // Check in classpath
            if (resourceXmlUrl == null) {
                resourceXmlUrl = ResourceFactory.class.getResource(CONFIG_FILE);
                locs.add("Classpath: " + CONFIG_FILE);
            }

            // Check in default file system location
            if (resourceXmlUrl == null && new File(CONFIG_FILE).isFile()) {
                try {
                    resourceXmlUrl = new File(CONFIG_FILE).toURI().toURL();
                }
                catch (MalformedURLException e) {
                    throw new ResourceLoaderException(e);
                }
                locs.add(new File(CONFIG_FILE).getAbsolutePath());
            }

            // Bail out if still not found
            if (resourceXmlUrl == null) {
                throw new ResourceLoaderException("Unable to locate configuration file ["
                        + CONFIG_FILE + "] in " + locs.toString());
            }

            loader = new KnowledgeBaseFactory(resourceXmlUrl.toString());
        }
        return loader;
    }

    /**
     * Constructor parameterized by the path to the configuration file.
     *
     * @param location
     *            location of the configuration file.
     */
    public KnowledgeBaseFactory(String location)
    {
        context = new FileSystemXmlApplicationContext(location);
    }

    /**
     * @return All registered resources. ResourceLoaderExceptions are catched and ignored to all for
     *         easy iteration over all resources runnalbe on the current system.
     */
    public Collection<KnowledgeBaseResource> getAll()
    {
        return context.getBeansOfType(KnowledgeBaseResource.class).values();
    }

    /**
     * Get the workspace directory.
     *
     * @return the workspace directory.
     * @throws IOException
     *             if the workspace cannot be obtained
     */
    private static File getWorkspace() throws IOException
    {
        if (System.getenv(ENV_DKPRO_HOME) != null) {
            File f = new File(System.getenv(ENV_DKPRO_HOME));
            return new File(f, ResourceFactory.class.getName());
        }

        throw new IOException("Environment variable [" + ENV_DKPRO_HOME + "] not set");
    }

    public KnowledgeBaseResource get(String name)
    {
        return (KnowledgeBaseResource) context.getBean(name, KnowledgeBaseResource.class);
    }
}
