package eu.openminted.uc.socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import eu.openminted.uc.socialsciences.common.PDFChecker;
import eu.openminted.uc.socialsciences.io.pdf.XmlCreator;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

/**
 * This class is responsible for PDF to XML conversion by invoking the web
 * service of pdfx.
 */
public class PdfxXmlCreator extends AbstractXmlCreator
{
	private static final String SERVICE_URL = "http://pdfx.cs.man.ac.uk";
	private static final String REQUEST_PARAM_JOB_ID = "job_id";
	private static final String REQUEST_PARAM_CLIENT = "client";
	private static final String REQUEST_PARAM_SENT_SPLITTER = "sent_splitter";
	private static final String REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE = "web-interface";
	private static final String REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT = "punkt";
	private static final String REQUEST_PARAM_USERFILE = "userfile";
	private static final String REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF = "application/pdf";
	private static final String REQUEST_RESPONSE_VALUE_ERROR = "error";
	private static final Logger logger = Logger.getLogger(PdfxXmlCreator.class);

	@Option(name = "-i", required = true, usage = "path to input file or directory containing pdf files " +
			"you want to process")
	private String input;

	@Option(name = "-o", required = true, usage = "path to output directory")
	private String output;

	/**
	 * Main method. Used for invoking the converter from command line.
	 * @param args program arguments
	 * */
	public static void main(String[] args) {
		new PdfxXmlCreator().run(args);
	}

	private void run(String[] args) {
		new CommandLineArgumentHandler().parseInput(args, this);
		process(input, output);
	}

	@Override
	protected Path singleFileProcess(Path pdfFile, Path outFile) {
		Path result = null;
		try {
			logger.info("processing file: " + pdfFile.toUri());
			if (processWithPdfx(pdfFile.toFile(), outFile)) {
				// output file was created
				result = outFile;
			}
			logger.info("processing file [" + pdfFile.toUri() + "] finished.");
		} catch (IOException x) {
			logger.error(x.getMessage());
			logger.error("Failure!", x);
		}
		return result;
	}

	private boolean processWithPdfx(File pdf, Path outFile) throws IOException {
		if (!overwriteOutput && new File(outFile.toUri()).isFile()) {
			logger.error(
					"Output file [" + outFile.toUri() + "] already exists. Set 'overwriteOutput' attribute to true "
							+ "to overwrite existing files.");
			logger.info("Skipping process for file: " + pdf.getName());
			skippedFileList.add(pdf.getName());
			return false;
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpEntity httpEntity = getFirstResponse(pdf, httpclient);

		String messageBody;

		messageBody = EntityUtils.toString(httpEntity);

		final int progressUrlPosition = 1;
		final int jobIdPosition = 2;
		int resultPosition = 3;

		if (messageBody.split(":").length == 5) {
			// resource was previously processed by the server
			resultPosition = 4;
		}

		String progressUrl = SERVICE_URL + messageBody.split(":")[progressUrlPosition].split("\"")[1];
		String jobId = messageBody.split(":")[jobIdPosition].split("\"")[1];
		String resultUrl = SERVICE_URL + messageBody.split(":")[resultPosition].split("\"")[1];

		// dispose httpEntity
		EntityUtils.consume(httpEntity);

		// second post
		httpEntity = getSecondResponse(httpclient, jobId);
		String response = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);

		if (response.contains(REQUEST_RESPONSE_VALUE_ERROR)) {
			logger.error(
					"Request for '" + pdf.getName() + "' was unsuccessful: " + response.split(":")[1].split("\"")[1]);
			throw new IOException(
					"Second POST request failed." + System.lineSeparator() + "HTTP response: " + response);
		}

		// final post
		HttpGet httpGet = new HttpGet(resultUrl + ".xml");
		CloseableHttpResponse result = httpclient.execute(httpGet);
		InputStream content = result.getEntity().getContent();
		boolean outputFileCreated = writeToFile(content, outFile);

		result.close();

		return outputFileCreated;
	}

	private boolean writeToFile(InputStream content, Path outputFilePath) throws IOException {
		boolean fileWritten = false;
		try {
			if (overwriteOutput) {
				Files.copy(content, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(content, outputFilePath);
			}
			logger.info("File [" + outputFilePath.toUri() + "] created.");
			fileWritten = true;
		} catch (FileAlreadyExistsException e) {
			logger.error("Output file [" + e.getFile() + "] already exists. Set 'overwriteOutput' attribute to true "
					+ "to overwrite existing files.");
		}
		return fileWritten;
	}

	private static HttpEntity getFirstResponse(File pdf, CloseableHttpClient httpClient) throws IOException {
		HttpEntity result;
		HttpPost httpPost = new HttpPost(SERVICE_URL);

		HttpEntity entity = MultipartEntityBuilder.create()
				.addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT)
				.addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE)
				.addBinaryBody(REQUEST_PARAM_USERFILE, pdf,
						ContentType.create(REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF), pdf.getPath())
				.build();

		httpPost.setEntity(entity);
		CloseableHttpResponse response;

		response = httpClient.execute(httpPost);
		if (isHttpResponseSuccessful(response)) {
			result = response.getEntity();
		} else {
			logger.error("Request for " + pdf.getPath() + " was unsuccessful: "
					+ response.getStatusLine().getReasonPhrase());
			throw new IllegalStateException("Server returned null for parse request of file [" + pdf.getPath() + "].");
		}

		return result;
	}

	private HttpEntity getSecondResponse(CloseableHttpClient httpclient, String jobId) throws IOException {
		HttpPost httpPost = new HttpPost(SERVICE_URL);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(REQUEST_PARAM_JOB_ID, jobId);
		builder.addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE);
		builder.addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT);
		httpPost.setEntity(builder.build());

		return httpclient.execute(httpPost).getEntity();
	}

	/**
	 * Checks if a HttpResponse has status code 200 (=ok).
	 *
	 * @param response
	 *            a HttpResponse object
	 * @return true iff status code of response equals 200
	 */
	protected static boolean isHttpResponseSuccessful(CloseableHttpResponse response) {
		return response.getStatusLine().getStatusCode() == 200;
	}

}