package datamodel;

import java.util.List;

public class Dataset {

	private String id;
	private String title;
	private List<String> topics;
	private List<Variable> variables;

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

	public List<Variable> getVariables() {
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

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		return String.format("Dataset %s ('%s')", id, title);
	}

}
