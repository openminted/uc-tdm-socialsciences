package eu.openminted.uc.socialsciences.variabledetection.resource;

public interface KnowledgeBaseResource
{
    public boolean containsConceptLabel(String conceptLabel);
    
    public boolean containsConceptLabel(String conceptLabel, String language);
}
