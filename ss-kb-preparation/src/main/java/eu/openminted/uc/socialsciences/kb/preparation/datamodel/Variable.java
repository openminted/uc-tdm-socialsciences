package eu.openminted.uc.socialsciences.kb.preparation.datamodel;

import java.io.Serializable;

/**
 * This class represents a survey variable.
 * A variable has a unique (at least in the context of one dataset) name, a
 * label and the question that was used to create this variable in a survey.
 *
 * @author neumanmy
 */
public class Variable implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5319869801726193913L;

	private String name;
	private String label;
	private String question;

	/**
	 * Getter for name.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for label.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Getter for question.
	 *
	 * @return question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * Set the variable's name.
	 *
	 * @param name
	 *            Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the variable's label
	 *
	 * @param label
	 *            Label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Set the variable's question text.
	 *
	 * @param question
	 *            Question text
	 */
	public void setQuestion(String question) {
		this.question = question;
	}

	@Override
	public String toString() {
		return String.format("Var %s ('%s')", name, label);
	}
}
