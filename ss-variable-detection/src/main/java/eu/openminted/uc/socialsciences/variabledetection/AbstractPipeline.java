package eu.openminted.uc.socialsciences.variabledetection;

import java.util.Map;

public abstract class AbstractPipeline
{
    /**
     * Asserts that DKPRO_HOME environment variable is set.
     * 
     * If DKPRO_HOME is already set, nothing is done (in order not to override already working
     * environments).
     */
    public static void assertDkproHomeVariableIsSet()
    {
        String dkproHome = "DKPRO_HOME";
        Map<String, String> env = System.getenv();
        if (!env.containsKey(dkproHome)) {
            throw new IllegalStateException(dkproHome + " environment variable is not set!");
        }
        else {
            System.out.println(dkproHome + " is set to: " + env.get(dkproHome));
        }
    }
}
