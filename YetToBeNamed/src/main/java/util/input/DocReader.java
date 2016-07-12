package util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import util.PDFChecker;
import util.convert.Converter;
import util.convert.PDFConverter;
import util.output.DBManager;

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

	public void readDocuments(DBManager writer) {
		for (Path path : toProcess) {
			String text = PDFConverter.convert(path, converter);
			String docName = path.getFileName().toString();

			if (text != null && !text.isEmpty()) {
				writer.writeDocument(docName, text);
			}
		}
	}
}
