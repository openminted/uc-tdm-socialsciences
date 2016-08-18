package util.convert;

public enum Converter {

	TIKA("Tika"), GROBID("GROBID"), PDFBOX("PDFBox"), JPOD("jPod");

	private String name;

	private Converter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
