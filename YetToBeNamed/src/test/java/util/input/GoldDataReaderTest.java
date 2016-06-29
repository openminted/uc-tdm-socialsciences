package util.input;

import java.io.File;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eval.GoldData;

public class GoldDataReaderTest {

	@Test
	public void testReadData() {
		GoldDataReader reader = new GoldDataReader(getFile("/xlsx/ALLBUS.xlsx").toPath());
		Set<GoldData> goldData = reader.readData();
		Assert.assertTrue(!goldData.isEmpty());

		for (GoldData entry : goldData) {
			System.out.println(entry);
		}
	}

	private File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

}
