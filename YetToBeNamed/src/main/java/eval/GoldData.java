package eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldData {

	private String datasetID;
	private Map<String, List<Reference>> references;

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
		List<Reference> refs = references.getOrDefault(varRef, new ArrayList<Reference>());
		refs.add(new Reference(refText, paperRef));
	}

}

class Reference {

	private String paperID;
	private String referenceText;

	public Reference(String refText, String paperRef) {
		setPaperID(paperRef);
		setReferenceText(refText);
	}

	public String getPaperID() {
		return paperID;
	}

	private void setPaperID(String paperID) {
		this.paperID = paperID;
	}

	public String getReferenceText() {
		return referenceText;
	}

	private void setReferenceText(String referenceText) {
		this.referenceText = referenceText;
	}
}
