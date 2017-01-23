package eu.openminted.uc.socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import eu.openminted.uc.socialsciences.common.CommandLineArgumentHandler;
import eu.openminted.uc.socialsciences.common.PDFChecker;
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
public class PdfxXmlCreator {
	public static final String SERVICE_URL = "http://pdfx.cs.man.ac.uk";

	public static final String REQUEST_PARAM_JOB_ID = "job_id";
	public static final String REQUEST_PARAM_CLIENT = "client";
	public static final String REQUEST_PARAM_SENT_SPLITTER = "sent_splitter";
	public static final String REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE = "web-interface";
	public static final String REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT = "punkt";
	public static final String REQUEST_PARAM_USERFILE = "userfile";
	public static final String REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF = "application/pdf";

	public static final String REQUEST_RESPONSE_VALUE_ERROR = "error";
	private static final Logger logger = Logger.getLogger(PdfxXmlCreator.class);
	public static final String DEFAULT_OUTPUT_PATH = "pdfx-out";

	private List<String> skippedFileList;

	@Option(name = "-overwrite", usage = "(Optional) if this option is set, program will overwrite files " +
			" that already exist in output directory.")
	private boolean overwriteOutput = false;

	@Option(name = "-i", required = true, usage = "path to input file or directory containing pdf files " +
			"you want to process")
	private String input;

	@Option(name = "-o", required = true, usage = "path to output directory")
	private String output;

	/**
	 * Main method. Used for invoking the converter from command line.
	 * */
	public static void main(String[] args) {
		new PdfxXmlCreator().run(args);
	}

	private void run(String[] args) {
		new CommandLineArgumentHandler().parseInput(args, this);
		process(input, output);
	}

	public List<String> getSkippedFileList() {
		List<String> result = new ArrayList<>();
		result.addAll(skippedFileList);
		return result;
	}

	/**
	 * Processes a single pdf file or all pdf files in a given directory with
	 * pdfx service which
	 * converts them into XML files. Stores the XML file(s) in a given output
	 * directory. If outputPathString is null, the input directory will be used.
	 *
	 * @param inputPathString
	 *            Path to pdf file to be processed. Or: The directory that
	 *            contains PDF files. It is scanned
	 *            recursively, non-pdf files will be ignored.
	 * @param outputPathString
	 *            The directory where the output XML file(s) will be stored.
	 *            if set to <i>null</i> generated files will be written to the
	 *            input directory.
	 * @return a list of all the Paths of the generated output files
	 */
	public List<Path> process(String inputPathString, String outputPathString) {
		Path inputPath;
		skippedFileList = new ArrayList<>();
		try {
			inputPath = Paths.get(inputPathString);
			Path outputPath = outputPathString == null ? null : Paths.get(outputPathString);
			return process(inputPath, outputPath);
		} catch (InvalidPathException e) {
			logger.error("Given String cannot be converted to valid path.", e);
			return null;
		}
	}

	/*
	 * Processes all pdf files in a given directory with pdfx service which
	 * converts them into XML files. Stores the XML files in a given output
	 * directory.
	 * @param inputDirectoryPath
	 * The directory that contains PDF files. It is scanned
	 * recursively, non-pdf files will be ignored.
	 * @param outputDirectoryPath
	 * The directory where the output XML files will be stored.
	 * @return a list of all the Paths of the generated output files
	 */
	private List<Path> process(Path inputPath, Path outputDirectoryPath) {
		List<Path> outputFiles = new ArrayList<>();
		if (!inputPath.toFile().exists()) {
			logger.error("Given path doesn't exist on the file system.");
			return outputFiles;
		}

		logger.info("PdfxXmlCreator process started...");
		logger.info("Input path: " + inputPath.toUri());

		List<Path> pdfFiles = new ArrayList<>();

		if (!inputPath.toFile().isDirectory()) {
			logger.info("Provided path is not a directory: " + inputPath.toUri());
			pdfFiles.add(inputPath);

			outputDirectoryPath = outputDirectoryPath == null ? inputPath.getParent()
					: outputDirectoryPath;
			logger.info("Output path: " + outputDirectoryPath.toUri());
		} else {
			logger.info("Provided path is a directory: " + inputPath.toUri());
			// get each PDF in the input directory
			pdfFiles = getPdfListFromDirectory(inputPath);
			logger.info(pdfFiles.size() + " pdf files found.");

			outputDirectoryPath = outputDirectoryPath == null ? inputPath.resolve(DEFAULT_OUTPUT_PATH)
					: outputDirectoryPath;
			logger.info("Output path: " + outputDirectoryPath.toUri());
		}

		// create output directory
		if (!Files.exists(outputDirectoryPath)) {
			try {
				Files.createDirectories(outputDirectoryPath);
				logger.info("Successfully created output directory: " + outputDirectoryPath.toUri());
			} catch (IOException e) {
				logger.error("IO Exception occurred when trying to create output directory.", e);
				throw new IllegalArgumentException("[" + outputDirectoryPath + "] directory can not be created.");
			}
		}

		for (Path pdfFile : pdfFiles) {
			Path outFile = outputDirectoryPath.resolve(FilenameUtils.getBaseName(pdfFile.toString()) + ".xml");
			Path processed = singleFileProcess(pdfFile, outFile);
			if (null != processed) {
				outputFiles.add(processed);
			}
		}

		logger.info("PdfxXmlCreator process finished.");
		return outputFiles;

	}

	private Path singleFileProcess(Path pdfFile, Path outFile) {
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

	private static List<Path> getPdfListFromDirectory(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter(PDFChecker::isPDFFile).forEach(toProcess::add);
		} catch (IOException e) {
			logger.error("Exception occurred in reading the directory: " + inputDir.toUri());
			logger.error("Exception in getPdfListFromDirectory", e);
			throw new IllegalArgumentException(e);
		}
		return toProcess;
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
			throw new IllegalArgumentException("Server returned null for parse request of file [" + pdf.getPath() + "]."
					+ System.lineSeparator() + " Entity contents: " + EntityUtils.toString(entity));
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
	public static boolean isHttpResponseSuccessful(CloseableHttpResponse response) {
		return response.getStatusLine().getStatusCode() == 200;
	}

	/**
	 * @return true iff parameter for overwriting existing output is set to true
	 */
	public boolean isOverwriteOutput() {
		return overwriteOutput;
	}

	/**
	 * Set the parameter which controls if already existing output files should
	 * be overwritten.
	 *
	 * @param overwriteOutput
	 *            set to true if you want to overwrite existing output,
	 *            otherwise to false.
	 */
	public void setOverwriteOutput(boolean overwriteOutput) {
		this.overwriteOutput = overwriteOutput;
	}
}