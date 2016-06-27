package util.input;

import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import datamodel.Dataset;

public class DBReaderTest {

	private static DBReader reader;

	@BeforeClass
	public static void setUpBeforeClass() {
		reader = new DBReader("test.sqlite");
	}

	@Test
	public void testRead() {
		Set<Dataset> readData = reader.readData();

		Assert.assertTrue(readData.isEmpty());
	}

}
