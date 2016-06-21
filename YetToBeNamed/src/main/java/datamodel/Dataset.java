package datamodel;

import java.util.List;
import java.util.Set;

public class Dataset {

	private String id;
	private String title;
	private List<String> topics;
	private Set<Variable> variables;
	private String language;

	public Dataset(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
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

	public void setId(String id) {
		this.id = id;
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

	@Override
	public String toString() {
		return String.format("Dataset %s ('%s')", id, title);
	}

	public String getLanguage() {
		return this.language;
	}

}
