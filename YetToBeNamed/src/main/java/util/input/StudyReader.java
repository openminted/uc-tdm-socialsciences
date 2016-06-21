package util.input;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import datamodel.Dataset;
import datamodel.Variable;

public class StudyReader {

	private Model model;

	private String startURL = "http://zacat.gesis.org/obj/fCatalog/ZACAT@datasets";

	private String datasetURI = "http://zacat.gesis.org:80/obj/fCatalog/ZACAT@datasets";
	private String studyURIBase = "http://zacat.gesis.org:80/obj/fStudy/";

	public StudyReader() {
		model = ModelFactory.createDefaultModel();
	}

	public void read() {
		Set<Resource> resources = getResourcesInBag(startURL, datasetURI);
		for (Resource res : resources) {
			followDataset(res);
		}
	}

	private Set<Resource> getResourcesInBag(String URL, String URI) {
		Set<Resource> result = new HashSet<>();

		InputStream content = URLConnector.getStreamFromURL(URL);
		if (content == null) {
			return result;
		}
		model.read(content, null);

		Bag bag = model.getBag(URI);
		System.out.println("Bag URI: " + bag.getURI());

		NodeIterator bagIter = bag.iterator();
		while (bagIter.hasNext()) {
			RDFNode inBag = bagIter.nextNode();
			if (inBag.isURIResource()) {
				result.add((Resource) inBag);
			}
		}
		return result;
	}

	private void followDataset(Resource dataset) {
		// TODO don't use local name but rather content from <externalId>

		// TODO: don't store as Dataset but rather in the SQLite DB (use
		// DBWriter)

		InputStream content = URLConnector.getStreamFromURL(dataset.getURI());
		if (content == null) {
			return;
		}
		model.read(content, null);

		String n39 = model.getNsPrefixMap().get("n39");
		String n36 = model.getNsPrefixMap().get("n36");
		String n40 = model.getNsPrefixMap().get("n40");

		Statement id = model.getProperty(dataset, ResourceFactory.createProperty(n36 + "externalId"));
		Statement title = model.getProperty(dataset, ResourceFactory.createProperty(n40 + "title"));

		Dataset ds = new Dataset(id.getString());
		ds.setTitle(title.getString());
		System.out.println("create new dataset: " + ds.toString());

		Statement varRefStmt = model.getProperty(dataset, ResourceFactory.createProperty(n39 + "variables"));
		if (varRefStmt != null) {
			RDFNode varRef = varRefStmt.getObject();
			if (varRef != null && varRef.isURIResource()) {
				followVars((Resource) varRef, ds);
			}
		} else {
			System.err.println("No variables found for var " + dataset);
		}

	}

	private void followVars(Resource varsRef, Dataset ds) {
		Set<Resource> resources = getResourcesInBag(varsRef.getURI(), varsRef.getURI());
		for (Resource res : resources) {
			followVar(res);
		}
	}

	private void followVar(Resource varRef) {
		Variable var = new Variable();

		InputStream content = URLConnector.getStreamFromURL(varRef.getURI());
		if (content == null) {
			return;
		}
		model.read(content, null);

		String s = model.getNsPrefixMap().get("s");
		String n43 = model.getNsPrefixMap().get("n43");

		Statement labelStmt = model.getProperty(varRef, ResourceFactory.createProperty(s + "label"));
		if (labelStmt != null) {
			var.setLabel(labelStmt.getString());
		}

		Statement varID = model.getProperty(varRef, ResourceFactory.createProperty(n43 + "varID"));
		if (varID != null) {
			var.setId(varID.getString()); // TODO: das ist nicht die ID für die
											// DB!
		}

		Statement name = model.getProperty(varRef, ResourceFactory.createProperty(n43 + "name"));
		if (name != null) {
			var.setName(name.getString());
		}
		Statement qstnText = model.getProperty(varRef, ResourceFactory.createProperty(n43 + "questionText"));
		if (qstnText != null) {
			var.setQuestion(qstnText.getString());
		}

	}

}
