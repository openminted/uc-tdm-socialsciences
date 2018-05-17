package eu.openminted.uc.socialsciences.variabledetection.pipelines;

public class VariableDisambiguationConstants
{
    public enum Mode
    {
        TRAIN, TEST, TEMP
    }
    
    public enum Dataset
    {
        ALL,
        MSRpar,
        MSRvid, 
        SMTeuroparl,
        TEMP;
    }
    
    public static final String FEATURES_DIR = "target/features";
    public static final String MODELS_DIR = "target/models";
    public static final String UTILS_DIR = "../data/utils";
}
