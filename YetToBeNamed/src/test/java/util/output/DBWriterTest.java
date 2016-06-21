package util.output;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import datamodel.Dataset;
import datamodel.Variable;

public class DBWriterTest {

	private Dataset dataset;
	private Variable v1;
	private Variable v2;

	@Before
	public void setUp() throws Exception {
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
		DBWriter writer = new DBWriter("test.sqlite", true);
		writer.write(dataset);

		writer.printDatabases();
	}

	@Test
	public void testWriteVariable() throws SQLException {
		DBWriter writer = new DBWriter("test.sqlite", false);
		writer.write(v1, 1);
		writer.write(v2, 1);

		writer.printDatabases();
	}

}
