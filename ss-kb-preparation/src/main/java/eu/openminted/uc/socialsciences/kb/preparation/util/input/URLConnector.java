package eu.openminted.uc.socialsciences.kb.preparation.util.input;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class URLConnector {

	private static final Logger logger = Logger.getLogger(URLConnector.class);

	public static String getStringFromURL(String inputURL) {
		HttpEntity entity = getEntity(inputURL);
		String entityContent = null;
		try {
			entityContent = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} catch (ParseException | IOException e) {
			logger.error(e.getMessage());
		}
		return entityContent;
	}

	public static InputStream getStreamFromURL(String inputURL) {
		HttpEntity entity = getEntity(inputURL);
		if (null == entity) {
			return null;
		}
		InputStream result = null;
		try {
			result = entity.getContent();
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage());
		}
		return result;
	}

	private static HttpEntity getEntity(String inputURL) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(inputURL);

		/*
		 * The underlying HTTP connection is still held by the response object
		 * to allow the response content to be streamed directly from the
		 * network socket. In order to ensure correct deallocation of system
		 * resources the user MUST call CloseableHttpResponse#close() from a
		 * finally clause. Please note that if response content is not fully
		 * consumed the underlying connection cannot be safely re-used and will
		 * be shut down and discarded by the connection manager.
		 */
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				logger.error(
						String.format("Status code not OK, returning null. (URL: %s; Code: %d)", inputURL, statusCode));
				return null;
			}
			HttpEntity entity = response.getEntity();
			return entity;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
