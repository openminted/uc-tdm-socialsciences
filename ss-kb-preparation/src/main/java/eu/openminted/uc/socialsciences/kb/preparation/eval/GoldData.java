package eu.openminted.uc.socialsciences.kb.preparation.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class GoldData {

	private String datasetID;
	private Map<String, List<Reference>> references;

	private static final Logger logger = Logger.getLogger(GoldData.class);

	public GoldData() {
		references = new HashMap<>();
	}

	public void setDatasetID(String datasetID) {
		this.datasetID = datasetID;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public Map<String, List<Reference>> getReferences() {
		return references;
	}

	public void addRef(String varRef, String refText, String paperRef) {
		if (null == varRef) {
			logger.warn("VarRef is null!");
		}
		List<Reference> refs = references.getOrDefault(varRef, new ArrayList<Reference>());
		refs.add(new Reference(refText, paperRef));
		references.put(varRef, refs);
	}

	@Override
	public String toString() {
		// for (String var : references.keySet()) {
		// List<Reference> refs = references.get(var);
		//
		// }
		return String.format("Dataset ID: %s (%s)", datasetID, references.keySet().toString());
	}

}

