package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import org.junit.Ignore;
import org.junit.Test;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

public class StudyReaderTest {

	/*
	 * Ignoring this test because it takes too long to read all the study data
	 */
	@Test
	@Ignore
	public void testReadAll() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).createTables());
		reader.read(-1);
	}

	@Test
	public void testReadN() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).createTables());
		reader.read(4);
	}
}