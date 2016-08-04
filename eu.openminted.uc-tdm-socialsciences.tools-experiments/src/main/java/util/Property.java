package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Property {
	public static final String PROPERTY_GROBID_HOME = "gropid.home";
	public static final String PROPERTY_GROBID_PROPERTIES = "grobid.properties";
	public static final String PROPERTY_DOC_FOLDER = "doc.folder";
	public static final String PROPERTY_OUT_BASE = "out.base";

	private static Properties properties = null;

	public static String load(String propertyName) {

		if (propertyName == null) {
			return null;
		}

		if (properties != null) {
			return properties.getProperty(propertyName);
		}

		properties = new Properties();
		URL url;
		try {
			url = ClassLoader.getSystemResource(ProjectConstants.PROPERTY_FILENAME);
			properties.load(url.openStream());
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return properties.getProperty(propertyName);

	}

	public static String load(String propertyName, String defaultValue) {
		String value = load(propertyName);

		if (value != null) {
			return value;
		}

		return defaultValue;
	}
}
