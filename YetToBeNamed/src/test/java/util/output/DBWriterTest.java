package util.output;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import datamodel.Dataset;
import datamodel.Variable;

public class DBWriterTest {

	private static Dataset dataset;
	private static Variable v1;
	private static Variable v2;
	private static DBWriter writer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		writer = new DBWriter("test.sqlite", true);

		dataset = new Dataset("test");
		dataset.setLanguage("English");
		dataset.setTitle("test dataset");

		v1 = new Variable();
		v1.setId("1");
		v1.setLabel("testVar1");
		v1.setName("v1");
		v1.setQuestion("test Question 1");

		v2 = new Variable();
		v2.setId("2");
		v2.setLabel("testVar2");
		v2.setName("v2");
		v2.setQuestion("test Question 2");

		List<Variable> variables = new ArrayList<>();
		variables.add(v1);
		variables.add(v2);

		dataset.setVariables(variables);
	}

	@Test
	public void testWriteDataset() throws SQLException {
		writer.write(dataset);
		writer.write(dataset);
	}

	@Test
	public void testWriteVariable() throws SQLException {
		writer.write(v1, 1);
		writer.write(v2, 1);
	}

	@AfterClass
	public static void printDatabases() throws SQLException {
		writer.printDatabases();
	}
}
