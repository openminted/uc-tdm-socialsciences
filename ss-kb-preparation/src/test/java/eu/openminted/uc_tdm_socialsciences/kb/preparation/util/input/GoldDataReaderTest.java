package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import java.io.File;

import org.junit.Test;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

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
