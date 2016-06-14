package util.input;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datamodel.Document;
import util.convert.Converter;
import util.convert.PDFConverter;

public class DocReader {

	private List<Path> toProcess;
	private Converter converter;

	public DocReader() {
		toProcess = new ArrayList<>();
		converter = Converter.TIKA;
	}

	public boolean setRootDir(Path root) {
		return false;
	}

	public Map<String, Document> readDocuments() {
		Map<String, Document> result = new HashMap<>();
		for (Path path : toProcess) {
			Document doc = PDFConverter.convert(path, converter);
			result.put(path.getFileName().toString(), doc);
		}
		return result;
	}

	// TODO: check if doc is PDF - using extension or tika magic things
	private boolean isPDFFile(Path path) {
		return false;
	}
}
