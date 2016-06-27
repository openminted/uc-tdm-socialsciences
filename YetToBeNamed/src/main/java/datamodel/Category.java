package datamodel;

import java.io.Serializable;

public class Category<T> implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -6704829696171816784L;

	private String label;
	private T value;

	public String getLabel() {
		return label;
	}

	public T getValue() {
		return value;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("Category %s ('%s')", value.toString(), label);
	}

}
