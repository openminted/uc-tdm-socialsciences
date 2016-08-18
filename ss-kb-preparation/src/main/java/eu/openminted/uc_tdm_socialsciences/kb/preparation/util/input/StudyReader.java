package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import java.io.IOException;
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
import org.unbescape.html.HtmlEscape;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.datamodel.Dataset;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.datamodel.Variable;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

public class StudyReader {

	private Model model;

	private String startURL = "http://zacat.gesis.org/obj/fCatalog/ZACAT@datasets";

	private String datasetURI = "http://zacat.gesis.org:80/obj/fCatalog/ZACAT@datasets";
	private String studyURIBase = "http://zacat.gesis.org:80/obj/fStudy/";

	private DBManager writer;

	public StudyReader(DBManager writer) {
		model = ModelFactory.createDefaultModel();
		this.writer = writer;
	}

	public void read(int maxNumber) {
		Set<Resource> resources = getResourcesInBag(startURL, datasetURI);
		int counter = 0;
		for (Resource res : resources) {
			if (counter == maxNumber) {
				break;
			}
			followDataset(res);
			counter++;
		}
	}

	private Set<Resource> getResourcesInBag(String URL, String URI) {
		Set<Resource> result = new HashSet<>();

		InputStream content = URLConnector.getStreamFromURL(URL);
		if (content == null) {
			return result;
		}
		model = ModelFactory.createDefaultModel();
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

	public void readDataset(String datasetID) {
		followDataset(ResourceFactory.createResource(studyURIBase + datasetID));
	}

	private void followDataset(Resource dataset) {
		try (InputStream content = URLConnector.getStreamFromURL(dataset.getURI())) {
			if (content == null) {
				return;
			}
			model = ModelFactory.createDefaultModel();
			model.read(content, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String n39 = model.getNsPrefixMap().get("n39");
		String n36 = model.getNsPrefixMap().get("n36");
		String n40 = model.getNsPrefixMap().get("n40");

		Statement extId = model.getProperty(dataset, ResourceFactory.createProperty(n36 + "externalId"));
		if (extId == null) {
			return;
		}
		Statement title = model.getProperty(dataset, ResourceFactory.createProperty(n40 + "title"));

		Dataset ds = new Dataset(extId.getString());
		if (title != null) {
			ds.setTitle(title.getString());
		}
		System.out.println("create new dataset: " + ds.toString());
		writer.write(ds);

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
			followVar(res, ds);
		}
	}

	private void followVar(Resource varRef, Dataset ds) {
		Variable var = null;

		InputStream content = URLConnector.getStreamFromURL(varRef.getURI());
		if (content == null) {
			return;
		}
		model = ModelFactory.createDefaultModel();
		model.read(content, null);
		try {
			content.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String s = model.getNsPrefixMap().get("s");
		String n43 = model.getNsPrefixMap().get("n43");

		var = new Variable();

		Statement labelStmt = model.getProperty(varRef, ResourceFactory.createProperty(s + "label"));
		if (labelStmt != null) {
			var.setLabel(labelStmt.getString());
		}

		Statement name = model.getProperty(varRef, ResourceFactory.createProperty(n43 + "name"));
		if (name != null) {
			var.setName(name.getString());
		}
		Statement qstnText = model.getProperty(varRef, ResourceFactory.createProperty(n43 + "questionText"));
		if (qstnText != null) {
			String text = cleanHTML(qstnText.getString());
			var.setQuestion(text);
			// var.setQuestion(qstnText.getString());
		}

		ds.addVariable(var);
		writer.write(var, ds.getId());
	}

	/**
	 * Cleans text containing HTML tags and entities. This method replaces the
	 * entities with characters and removes tags.
	 *
	 * @param html
	 * @return
	 */
	private String cleanHTML(String html) {
		String temp = HtmlEscape.unescapeHtml(html);
		temp = temp.replaceAll("(<script ?.*?>.+</script>)|(<!--.*?-->)|(<a href=.+?>.*?</a>)|(<.+?>)", " ")
				.replaceAll("\\s+", " ");
		return temp;
		// html = html.replaceAll("<a href=.+?>.*?</a>", "");
		// return Jsoup.parse(html).text();
	}
}
