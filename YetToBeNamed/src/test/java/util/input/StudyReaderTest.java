package util.input;

import org.junit.Test;

import util.output.DBManager;

public class StudyReaderTest {

	@Test
	public void testReadAll() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).dropAllTables().createTables());
		reader.read(-1);
	}

	@Test
	public void testReadN() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).dropAllTables().createTables());
		reader.read(10);
	}
}