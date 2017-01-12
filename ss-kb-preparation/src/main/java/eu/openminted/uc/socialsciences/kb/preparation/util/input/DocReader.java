package eu.openminted.uc.socialsciences.kb.preparation.util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import eu.openminted.uc.socialsciences.common.PDFChecker;
import org.apache.log4j.Logger;

import eu.openminted.uc.socialsciences.kb.preparation.util.convert.Converter;
import eu.openminted.uc.socialsciences.kb.preparation.util.convert.PDFConverter;
import eu.openminted.uc.socialsciences.kb.preparation.util.output.DBManager;

public class DocReader {

	private List<Path> toProcess;
	private Converter converter;

	private static final Logger logger = Logger.getLogger(DocReader.class);

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
			logger.info("Reading document " + path);
			String text = PDFConverter.convert(path, converter);
			String docName = path.getFileName().toString();
			docName = docName.substring(0, docName.lastIndexOf('.'));

			if (text != null && !text.isEmpty()) {
				writer.writeDocument(docName, text);
			}
		}
	}

	public List<Path> getToProcess() {
		return toProcess;
	}
}
