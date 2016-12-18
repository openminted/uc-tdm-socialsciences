package eu.openminted.uc.socialsciences.kb.preparation.datamodel;

import java.io.Serializable;

public class Variable implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5319869801726193913L;

	private String name;
	private String label;
	private String question;

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getQuestion() {
		return question;
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

	@Override
	public String toString() {
		return String.format("Var %s ('%s')", name, label);
	}
}
