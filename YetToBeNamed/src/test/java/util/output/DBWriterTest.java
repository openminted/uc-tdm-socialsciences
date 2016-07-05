package util.output;

import java.sql.SQLException;

import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import datamodel.Dataset;
import datamodel.Variable;
import util.input.StudyReader;

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
		writer = DBManager.getInstance(true).dropAllTables().createTables();
		writer.write(dataset);
		writer.write(dataset);
	}

	@Test
	public void testWriteVariable() throws SQLException {
		writer = DBManager.getInstance(true).dropAllTables().createTables();
		writer.write(v1, 1);
		writer.write(v2, 1);
	}

	@Test
	public void testCreateTables() {
		writer = DBManager.getInstance(true).dropAllTables().createTables();
	}

	@Test
	public void testWriteSingleRealData() {
		DBManager writer = DBManager.getInstance(true).dropAllTables().createTables();
		StudyReader reader = new StudyReader(writer);

		reader.followDataset(ResourceFactory.createResource("http://zacat.gesis.org:80/obj/fStudy/ZA3779"));
	}

	@Test
	public void testWriteNRealData() {
		StudyReader reader = new StudyReader(DBManager.getInstance(true).dropAllTables().createTables());
		reader.read(10);
	}
}
