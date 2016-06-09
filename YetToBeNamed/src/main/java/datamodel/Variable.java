package datamodel;

import java.util.List;

public class Variable {

	private String id;
	private String name;
	private String label;
	private String question;
	private List<Category<Type>> categories;

	public String getId() {
		return id;
	}

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

	public void setId(String id) {
		this.id = id;
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
		return String.format("Var %s ('%s')", id, label);
	}
}
