package pdf_readers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.config.GrobidAnalysisConfig.GrobidAnalysisConfigBuilder;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

import util.Property;

public class Grobid {

	private static final String thiz = "grobid";

	public static void main(String[] args) {

		// Test documents can be obtained from the following URL
		// https://drive.google.com/file/d/0Bx1HpGFsGYhnbFh2SjAzU3p3X3c/view?usp=sharing
		final String[] fileNames = new String[] { "2819", "16597", "17527", "18479", "27939", "27940", "28005", "28189",
				"28681", "28750", "28835", "28862", "29294", "31259", "31451", "31457", "44921" };

		File outDir = new File(Property.load(Property.PROPERTY_OUT_BASE) + thiz + "/");
		//noinspection ResultOfMethodCallIgnored
		outDir.mkdir();

		for (String entry : fileNames) {
			String input = Property.load(Property.PROPERTY_DOC_FOLDER) + entry + ".pdf";
			String output = outDir + "/" + entry + ".xml";

			convertPdfToXml(input, output);
		}
	}

	private static void convertPdfToXml(String input, String output) {
		try {

			String home = Property.load(Property.PROPERTY_GROBID_HOME);
			String properties = Property.load(Property.PROPERTY_GROBID_PROPERTIES);

			MockContext.setInitialContext(home, properties);
			GrobidProperties.getInstance();

			System.out.println(">>>>>>>> GROBID_HOME=" + GrobidProperties.get_GROBID_HOME_PATH());

			Engine engine = GrobidFactory.getInstance().createEngine();

			// Biblio object for the result
			// BiblioItem resHeader = new BiblioItem();
			// String tei = engine.processHeader(pdfPath, false, resHeader);

			GrobidAnalysisConfigBuilder configBuilder = GrobidAnalysisConfig.builder();
			GrobidAnalysisConfig config = configBuilder.consolidateCitations(true).consolidateHeader(true).startPage(1)
					.build();
			String textToTEI = engine.fullTextToTEI(new File(input), config);

			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
			outputStream.write(textToTEI);
			outputStream.close();

			engine.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				MockContext.destroyInitialContext();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
