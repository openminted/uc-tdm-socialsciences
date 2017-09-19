package eu.openminted.uc.socialsciences.variabledetection.resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    private Map<Pair<String, String>, Boolean> cache;
    private final String NO_LANGUAGE = "";

    public TheSozResource(String theSozPath)
    {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(theSozPath);
        if (in == null) {
            throw new IllegalArgumentException("Can not file TheSoz file: [" + theSozPath + "]");
        }
        model.read(in, null);
        
        prefixString = "prefix skos: <http://www.w3.org/2004/02/skos/core#>";
        cache = new HashMap<>();
    }

    @Override
    public boolean containsConceptLabel(String conceptLabel)
    {
        Optional<Boolean> valueFromCache = getFromCache(conceptLabel, NO_LANGUAGE);
        if (valueFromCache.isPresent())
        {
            return valueFromCache.get();
        }

        boolean result = false;
        try {
            Query query = createSelectQuery(conceptLabel);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet resultSet = qexec.execSelect();

            result = resultSet.hasNext();
        }
        catch (QueryException ex) {
            // TODO log
            System.err.println("Cannot parse query for conceptLabel=[" + conceptLabel + "]");
        }
        putInCache(conceptLabel, NO_LANGUAGE, result);
        return result;
    }

    @Override
    public boolean containsConceptLabel(String conceptLabel, String language)
    {
        Optional<Boolean> valueFromCache = getFromCache(conceptLabel, language);
        if (valueFromCache.isPresent())
        {
            return valueFromCache.get();
        }

        boolean result = false;
        try {
            Query query = createSelectQuery(conceptLabel);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet resultSet = qexec.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
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
        putInCache(conceptLabel, language, result);
        return result;
    }
    
    private Optional<Boolean> getFromCache(String conceptLabel, String language)
    {
        Pair<String, String> cacheEntryId = new MutablePair<>(conceptLabel, language);
        return Optional.ofNullable(cache.get(cacheEntryId));
    }
    
    private void putInCache(String conceptLabel, String language, boolean value)
    {
        Pair<String, String> cacheEntryId = new MutablePair<>(conceptLabel, language);
        cache.put(cacheEntryId, value);
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
