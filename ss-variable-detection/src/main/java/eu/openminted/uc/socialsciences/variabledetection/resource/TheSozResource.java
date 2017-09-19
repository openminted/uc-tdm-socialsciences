package eu.openminted.uc.socialsciences.variabledetection.resource;

import java.io.InputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class TheSozResource
    implements KnowledgeBaseResource
{
    public static final String NAME = "TheSoz";

    private Model model;
    private final String prefixString;

    public TheSozResource(String theSozPath)
    {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(theSozPath);
        if (in == null) {
            throw new IllegalArgumentException("Can not file TheSoz file: [" + theSozPath + "]");
        }
        model.read(in, null);
        
        prefixString = "prefix skos: <http://www.w3.org/2004/02/skos/core#>";
    }

    @Override
    public boolean containsConceptLabel(String conceptLabel)
    {
        try {
            Query query = createSelectQuery(conceptLabel);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();

            return results.hasNext();
        }
        catch (QueryException ex) {
            // TODO log
            System.err.println("Cannot parse query for conceptLabel=[" + conceptLabel + "]");
            return false;
        }
    }

    @Override
    public boolean containsConceptLabel(String conceptLabel, String language)
    {
        boolean result = false;
        try {
            Query query = createSelectQuery(conceptLabel);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                Literal literal = solution.getLiteral("?label");
                if (literal.getLanguage() != null && language.equals(literal.getLanguage())) {
                    result = true;
                    break;
                }
            }
        }
        catch (QueryException ex) {
            // TODO log
            System.err.println("Cannot parse query for conceptLabel=[" + conceptLabel + "]");
        }
        return result;
    }

    private Query createSelectQuery(String conceptLabel)
            throws QueryException
    {
        Query query;
        query = QueryFactory
                .create(prefixString
                        + "SELECT ?label WHERE { "
                        + "     ?term ?p ?label . "
                        + "     FILTER ((?p IN (skos:prefLabel, skos:altLabel)) "
                        + "         && str(?label) = \"" + conceptLabel + "\") }");
        return query;
    }
}
