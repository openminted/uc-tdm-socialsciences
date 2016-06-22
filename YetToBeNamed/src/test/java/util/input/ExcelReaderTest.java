package util.input;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ExcelReaderTest {

	@Test
	public void testReadText() {
		ExcelReader reader = new ExcelReader();
		String text = reader.simpleTextExtraction(getFile("/xlsx/ALLBUS.xlsx"));
		Assert.assertNotNull(text);
		System.out.println(text);

	}

	@Test
	public void testReadData() {
		ExcelReader reader = new ExcelReader();
		reader.read(getFile("/xlsx/ALLBUS.xlsx"));
	}

	File getFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

}
