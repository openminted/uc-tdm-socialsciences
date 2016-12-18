package eu.openminted.uc.socialsciences.kb.preparation.util.input;

import java.io.File;

import eu.openminted.uc.socialsciences.kb.preparation.util.output.DBManager;
import org.junit.Test;

public class GoldDataReaderTest {

	@Test
	public void testReadData() {
		GoldDataReader reader = new GoldDataReader(getFile("/xlsx/ALLBUS.xlsx").toPath());
		reader.readData(DBManager.getInstance(true).createTables());
	}

	private File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}
}
