package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import main.ProjectConstants;

public class Property {

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
			url = ClassLoader.getSystemResource(ProjectConstants.propertyFilename);
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
