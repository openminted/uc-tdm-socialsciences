package util.input;

import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import datamodel.Dataset;
import datamodel.Variable;

public class DBReaderTest {

	private static DBReader reader;

	@BeforeClass
	public static void setUpBeforeClass() {
		reader = new DBReader("test.sqlite");
	}

	@Test
	public void testRead() {
		Set<Dataset> readData = reader.readData();

		Assert.assertFalse(readData.isEmpty());

		for (Dataset dataset : readData) {
			System.out.println(dataset);
			Set<Variable> variables = dataset.getVariables();
			for (Variable variable : variables) {
				System.out.println(variable);
			}
		}
	}
}
