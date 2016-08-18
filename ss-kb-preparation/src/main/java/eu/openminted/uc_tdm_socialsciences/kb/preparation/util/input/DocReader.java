package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.PDFChecker;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.convert.Converter;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.convert.PDFConverter;
import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

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

	public List<Path> getToProcess() {
		return toProcess;
	}
}
