package util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public Set<Document> readDocuments() {
		Set<Document> result = new HashSet<>();
		for (Path path : toProcess) {
			Document doc = PDFConverter.convert(path, converter);
			String docName = path.getFileName().toString();
			result.add(doc);
		}
		return result;
	}
}
