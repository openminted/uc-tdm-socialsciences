package datamodel;

public class Category<T> {

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
