package eu.openminted.uc.socialsciences.kb.preparation.util.input;

import eu.openminted.uc.socialsciences.kb.preparation.util.output.DBManager;
import org.junit.Ignore;
import org.junit.Test;

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

	//fixme this test has sql errors
	@Ignore
	@Test
	public void testReadN() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).createTables());
		reader.read(4);
	}
}