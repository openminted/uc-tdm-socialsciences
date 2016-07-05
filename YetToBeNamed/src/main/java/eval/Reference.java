package eval;
public class Reference {

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

	@Override
	public String toString() {
		return String.format("Reference in Paper %s: '%s'", paperID, referenceText);
	}
}
