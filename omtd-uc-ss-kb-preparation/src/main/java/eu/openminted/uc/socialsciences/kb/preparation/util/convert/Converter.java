package eu.openminted.uc.socialsciences.kb.preparation.util.convert;

public enum Converter {

	TIKA("Tika"), GROBID("GROBID"), PDFBOX("PDFBox"), JPOD("jPod");

	private String name;

	Converter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
