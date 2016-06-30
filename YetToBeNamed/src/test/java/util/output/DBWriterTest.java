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
	private DBWriter writer;

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
		writer = DBWriter.getInstance("testsimple.sqlite", true);
		writer.write(dataset);
		writer.write(dataset);
	}

	@Test
	public void testWriteVariable() throws SQLException {
		writer = DBWriter.getInstance("testsimple.sqlite", true);
		writer.write(v1, 1);
		writer.write(v2, 1);
	}

	@Test
	public void testWriteSingleRealData() {
		StudyReader reader = new StudyReader(DBWriter.getInstance("testsingle.sqlite", true));

		reader.followDataset(ResourceFactory.createResource("http://zacat.gesis.org:80/obj/fStudy/ZA3779"));
	}

	@Test
	public void testWriteNRealData() {
		StudyReader reader = new StudyReader(DBWriter.getInstance("testN.sqlite", true));

		reader.read(10);
	}

	// @AfterClass
	// public void printDatabases() throws SQLException {
	// writer.printDatabases();
	// }
}
