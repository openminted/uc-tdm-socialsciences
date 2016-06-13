package datamodel;

public class Document {

	private String text;
	private final String name;

	public Document(String name) {
		this.name = name;
	}

	public void setText(String text) {
		if (this.text == null || this.text.isEmpty()) {
			this.text = text;
		}
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}
}
