package eu.openminted.uc.socialsciences.kb.preparation.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;


public class PDFChecker {

	private static final Logger logger = Logger.getLogger(PDFChecker.class);


	public static boolean isPDFFile(Path file) {
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
			logger.error("Could not detect MIME type because of IO Error.", e);
		}
		return false;
	}
}
