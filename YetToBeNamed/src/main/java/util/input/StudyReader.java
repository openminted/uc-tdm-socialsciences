package util.input;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class StudyReader {

	private Model model;

	public StudyReader() {
		model = ModelFactory.createDefaultModel();
	}

}
