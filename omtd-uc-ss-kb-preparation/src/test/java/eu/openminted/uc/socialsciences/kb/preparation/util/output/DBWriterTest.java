package eu.openminted.uc.socialsciences.kb.preparation.util.output;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.openminted.uc.socialsciences.kb.preparation.datamodel.Dataset;
import eu.openminted.uc.socialsciences.kb.preparation.datamodel.Variable;

public class DBWriterTest {

	private static Dataset dataset;
	private static Variable v1;
	private static Variable v2;
	private DBManager writer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataset = new Dataset("test");
		dataset.setLanguage("English");
		dataset.setTitle("test dataset");

		v1 = new Variable();
		v1.setLabel("testVar1");
		v1.setName("v1");
		v1.setQuestion("test Question 1");

		v2 = new Variable();
		v2.setLabel("testVar2");
		v2.setName("v2");
		v2.setQuestion("test Question 2");

		dataset.addVariable(v1);
		dataset.addVariable(v2);
	}

	@Test
	public void testWriteDataset() throws SQLException {
		writer = DBManager.getInstance(true).createTables();
		writer.write(dataset);
		writer.write(dataset);
	}

	@Test
	public void testWriteVariable() throws SQLException {
		writer = DBManager.getInstance(true).createTables();
		writer.write(dataset);
		writer.write(v1, dataset.getExternalID());
		writer.write(v2, dataset.getExternalID());
	}
}
