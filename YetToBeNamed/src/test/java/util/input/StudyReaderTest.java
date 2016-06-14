package util.input;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class StudyReaderTest {

	private String inputURL;
	private String DATASET_URI;
	private String STUDY_URI;

	@Before
	public void setUp() throws Exception {
		inputURL = "http://zacat.gesis.org/obj/fCatalog/ZACAT@datasets";

		DATASET_URI = "http://zacat.gesis.org:80/obj/fCatalog/ZACAT@datasets";
		STUDY_URI = "http://zacat.gesis.org:80/obj/fStudy/";
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
