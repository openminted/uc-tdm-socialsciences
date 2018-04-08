package eu.openminted.uc.socialsciences.kb.preparation.util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.openminted.uc.socialsciences.common.PDFChecker;
import eu.openminted.uc.socialsciences.kb.preparation.util.convert.Converter;
import eu.openminted.uc.socialsciences.kb.preparation.util.convert.PDFConverter;
import eu.openminted.uc.socialsciences.kb.preparation.util.output.DBManager;

/**
 * Class for reading documents in PDF format from a directory, converting them
 * to text and storing this text in the database.
 * Documents must have unique names.
 *
 * @author neumanmy
 */
public class DocReader {

	private List<Path> toProcess;
	private Converter converter;

	private static final Logger logger = Logger.getLogger(DocReader.class);

	/**
	 * Constructor. Sets the root directory from which the reader will read
	 * input documents recursively.
	 *
	 * @param root
	 *            root directory
	 */
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

	/**
	 * Read all documents recursively starting from the root directory and store
	 * their texts into the database.
	 *
	 * @param writer
	 *            Database writer
	 */
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

	/**
	 * Get the list of files that this instance will be able to process
	 * (collected from the root directory and all its sub-folders).
	 *
	 * @return list of documents to be processed
	 */
	public List<Path> getToProcess() {
		return toProcess;
	}
}
