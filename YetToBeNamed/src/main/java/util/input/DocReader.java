package util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datamodel.Document;
import util.PDFChecker;
import util.convert.Converter;
import util.convert.PDFConverter;

public class DocReader {

	private List<Path> toProcess;
	private Converter converter;

	public DocReader(Path root) {
		toProcess = new ArrayList<>();
		converter = Converter.TIKA;
		setRootDir(root);
	}

	private void setRootDir(Path root) {
		try {
			Files.walk(root).filter(Files::isRegularFile).filter(PDFChecker::isPDFFile).forEach(toProcess::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Document> readDocuments() {
		Map<String, Document> result = new HashMap<>();
		for (Path path : toProcess) {
			Document doc = PDFConverter.convert(path, converter);
			result.put(path.getFileName().toString(), doc);
		}
		return result;
	}
}
