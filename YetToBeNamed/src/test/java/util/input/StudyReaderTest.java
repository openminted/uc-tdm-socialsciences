package util.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import datamodel.Dataset;
import util.output.DBWriter;

public class StudyReaderTest {

	private File output;

	@Before
	public void setUp() throws Exception {
		output = new File("output/datasets.out");
	}

	@Test
	public void testReadAll() {
		StudyReader reader = new StudyReader(new DBWriter("testall2.sqlite", true));
		reader.read(-1);
	}

	@Test
	public void testReadFromFile() {
		Set<Dataset> data = readFromFile();

		Assert.assertNotNull(data);
		Assert.assertTrue(!data.isEmpty());
	}

	private Set<Dataset> readFromFile() {
		Set<Dataset> readData = null;

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(output))) {

			readData = new HashSet<>();
			Object readObject = in.readObject();
			if (readObject instanceof Dataset) {
				readData.add((Dataset) readObject);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return readData;
	}

	@Test
	public void testReadN() {
		StudyReader reader = new StudyReader(new DBWriter("testn.sqlite", true));
		Set<Dataset> data = reader.read(10);

		writeToFile(data);
	}

	private void writeToFile(Set<Dataset> data) {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(output))) {

			for (Dataset dataset : data) {
				out.writeObject(dataset);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
