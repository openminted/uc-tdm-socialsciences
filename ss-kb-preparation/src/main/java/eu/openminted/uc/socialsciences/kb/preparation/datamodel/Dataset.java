package eu.openminted.uc.socialsciences.kb.preparation.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a dataset.
 * A dataset in the social sciences is a collection of survey variables and has
 * a unique ID and a set of associated topics that are covered by this dataset.
 * It also has a title.
 *
 * @author neumanmy
 */
public class Dataset implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -554107093493784151L;

	private String title;
	private List<String> topics;
	private Set<Variable> variables;
	private String language;
	private String externalID;
	private int id;

	private static int counter = 0;

	/**
	 * Constructor. Create a new dataset with given ID.
	 *
	 * @param id
	 *            dataset ID
	 */
	public Dataset(String id) {
		this.variables = new HashSet<>();
		setExternalID(id);
		this.id = counter++;
	}

	/**
	 * Get dataset title.
	 *
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get topics associated with that dataset.
	 *
	 * @return list of topics
	 */
	public List<String> getTopics() {
		return topics;
	}

	/**
	 * Get all variables in dataset.
	 *
	 * @return list of variables
	 */
	public Set<Variable> getVariables() {
		return variables;
	}

	/**
	 * Get dataset ID.
	 *
	 * @return ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set dataset title.
	 *
	 * @param title
	 *            Title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Set dataset topics.
	 *
	 * @param topics
	 *            Topics
	 */
	public void setTopics(List<String> topics) {
		this.topics = topics;
	}

	/**
	 * Add a variable to this dataset.
	 *
	 * @param var
	 *            Variable to add.
	 */
	public void addVariable(Variable var) {
		this.variables.add(var);
	}

	/**
	 * Set dataset language
	 *
	 * @param language
	 *            language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Get the language of this dataset
	 *
	 * @return language
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Get the external ID of this dataset.
	 *
	 * @return external ID
	 */
	public String getExternalID() {
		return externalID;
	}

	private void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	@Override
	public String toString() {
		return String.format("Dataset %s ('%s')", externalID, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Dataset)) {
			return false;
		}
		Dataset other = (Dataset) obj;
		return other.externalID.equals(this.externalID);
	}

	@Override
	public int hashCode() {
		return externalID.hashCode();
	}

	public void addVariables(Set<Variable> set) {
		this.variables.addAll(set);
	}
}
