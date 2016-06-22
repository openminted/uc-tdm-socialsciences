package datamodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dataset {

	private String title;
	private List<String> topics;
	private Set<Variable> variables;
	private String language;
	private String externalID;
	private int id;

	private static int counter = 1;

	public Dataset(String id) {
		this.variables = new HashSet<>();
		this.externalID = id;
		this.id = counter++;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getTopics() {
		return topics;
	}

	public Set<Variable> getVariables() {
		return variables;
	}

	public int getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}

	@SuppressWarnings("unused")
	private void setVariables(Set<Variable> variables) {
		this.variables = variables;
	}

	public void addVariable(Variable var) {
		this.variables.add(var);
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return this.language;
	}

	public String getExternalID() {
		return externalID;
	}

	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	@Override
	public String toString() {
		return String.format("Dataset %s ('%s')", id, title);
	}
}
