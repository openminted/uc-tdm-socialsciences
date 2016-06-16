package util.input;

import java.io.InputStream;
import java.util.HashSet;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import datamodel.Dataset;

public class StudyReader {

	private Model model;

	private String startURL = "http://zacat.gesis.org/obj/fCatalog/ZACAT@datasets";

	private String datasetURI = "http://zacat.gesis.org:80/obj/fCatalog/ZACAT@datasets";
	private String studyURIBase = "http://zacat.gesis.org:80/obj/fStudy/";

	private HashSet<Dataset> datasets;

	public StudyReader() {
		model = ModelFactory.createDefaultModel();
		datasets = new HashSet<Dataset>();
	}

	public void read() {
		InputStream content = URLConnector.getStreamFromURL(startURL);
		if (content == null) {
			return;
		}
		model.read(content, null);

		Bag bag = model.getBag(datasetURI);
		System.out.println("Bag URI: " + bag.getURI());

		NodeIterator bagIter = bag.iterator();
		while (bagIter.hasNext()) {
			RDFNode inBag = bagIter.nextNode();
			// System.out.println(inBag);
			if (inBag.isURIResource()) {
				followDataset((Resource) inBag);
			}
		}
	}

	private static void followDataset(Resource dataset) {
		Dataset ds = new Dataset(dataset.getLocalName());
		// TODO: don't store as Dataset but rather in the SQLite DB (use
		// DBWriter)

		System.out.println("create new dataset: " + ds.toString());

		Model model = ModelFactory.createDefaultModel();

		InputStream content = URLConnector.getStreamFromURL(dataset.getURI());
		if (content == null) {
			return;
		}
		model.read(content, null);

		String n39 = model.getNsPrefixMap().get("n39");

		Statement varRefStmt = model.getProperty(dataset, ResourceFactory.createProperty(n39 + "variables"));
		// System.out.println(varRefStmt);
		if (varRefStmt != null) {
			RDFNode varRef = varRefStmt.getObject();
			if (varRef != null && varRef.isURIResource()) {
				followVars((Resource) varRef, ds);
			}
		} else {
			System.err.println("No variables found for var " + dataset);
		}

	}

	private static void followVars(Resource varsRef, Dataset ds) {
		Model model = ModelFactory.createDefaultModel();

		InputStream content = URLConnector.getStreamFromURL(varsRef.getURI());
		if (content == null) {
			return;
		}
		model.read(content, null);

		Bag bag = model.getBag(varsRef.getURI());
		System.out.println("Bag URI: " + bag.getURI());

		NodeIterator bagIter = bag.iterator();
		while (bagIter.hasNext()) {
			RDFNode inBag = bagIter.nextNode();
			// System.out.println(inBag);
			if (inBag.isURIResource()) {
				followVar((Resource) inBag);
			}
		}

	}

	private static void followVar(Resource varRef) {
		Model model = ModelFactory.createDefaultModel();

		InputStream content = URLConnector.getStreamFromURL(varRef.getURI());
		if (content == null) {
			return;
		}
		model.read(content, null);

		String s = model.getNsPrefixMap().get("s");
		Statement labelStmt = model.getProperty(varRef, ResourceFactory.createProperty(s + "label"));
		if (labelStmt != null) {
			// System.out.println(labelStmt.getObject());
		} else {
			System.err.println("No label found for var " + varRef);
		}
	}

}
