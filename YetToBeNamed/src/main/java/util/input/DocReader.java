package util.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;

import datamodel.Document;
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
			Files.walk(root).filter(Files::isRegularFile).filter(this::isPDFFile).forEach(toProcess::add);
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

	// TODO: auslagern (Java 8 Style)
	private boolean isPDFFile(Path file) {
		String fileName = file.getFileName().toString();

		TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
		Metadata metadata = new Metadata();
		MimeTypes mimeRegistry = tikaConfig.getMimeRepository();

		metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);

		MediaType type;
		try {
			// MIME type (based on filename)
			type = mimeRegistry.detect(null, metadata);

			if (null == type) {
				InputStream stream = TikaInputStream.get(file);
				// MIME type (based on MAGIC)
				type = mimeRegistry.detect(stream, metadata);

				if (null == type) {
					stream = TikaInputStream.get(file);
					Detector detector = tikaConfig.getDetector();
					// MIME type (based on the Detector interface)
					type = detector.detect(stream, metadata);
				}
			}
			return type.getSubtype().equals("pdf");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
