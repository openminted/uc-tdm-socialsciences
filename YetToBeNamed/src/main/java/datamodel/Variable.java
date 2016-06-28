package datamodel;

import java.io.Serializable;
import java.util.List;

public class Variable implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5319869801726193913L;

	private String name;
	private String label;
	private String question;
	private List<Category<Type>> categories;

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getQuestion() {
		return question;
	}

	public List<Category<Type>> getCategories() {
		return categories;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setCategories(List<Category<Type>> categories) {
		this.categories = categories;
	}

	@Override
	public String toString() {
		return String.format("Var %s ('%s')", name, label);
	}
}
