package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
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
import org.apache.uima.UIMAException;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.PDFChecker;

/**
 * This class is responsible for PDF to XML conversion by invoking the web
 * service of pdfx.
 *
 * @author neumanmy
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

	private boolean overwriteOutput = false;

	/**
	 * Main method. Used for invoking the converter from command line.
	 *
	 * @param args
	 *            one or two paths to directories may be given as arguments.
	 *            First arg (mandatory) is input directory containing pdf files.
	 *            Second (optional) is output directory where XML files should
	 *            be stored. If no output directory is specified, output will be
	 *            stored in subdirectory of input directory called "pdfx-out".
	 */
	public static void main(String[] args) {
		PdfxXmlCreator creator = new PdfxXmlCreator();

		String inputPath = null, outputPath = null;

		switch (args.length) {
		case 0:
			break;
		case 2:
			outputPath = args[1];
		case 1:
			inputPath = args[0];
			break;
		default:
			logger.error(
					"Illegal number of command line arguments given. Provide input path as first argument (mandatory) and output path as second argument (optional).");
		}

		Scanner scanner = new Scanner(System.in);
		while (null == inputPath || inputPath.length() < 1) {
			System.out.println(
					"Please provide path to input directory containing pdf files or to the file you want to process:");
			inputPath = scanner.nextLine();
		}
		scanner.close();

		creator.process(inputPath, outputPath);
	}

	/**
	 * Processes all pdf files in a given directory with pdfx service which
	 * converts them into XML files. Stores the XML files in a given output
	 * directory. If outputPathString is null, the input directory will be used.
	 *
	 * @param inputPathString
	 *            The directory that contains PDF files. It is scanned
	 *            recursively, non-pdf files will be ignored.
	 * @param outputPathString
	 *            The directory where the output XML files will be stored.
	 * @return a list of all the Paths of the generated output files
	 */
	public List<Path> process(String inputPathString, String outputPathString) {
		Path inputPath;
		try {
			inputPath = Paths.get(inputPathString);
			Path outputPath = outputPathString == null ? null : Paths.get(outputPathString);
			return process(inputPath, outputPath);
		} catch (InvalidPathException e) {
			logger.error("Given String cannot be converted to valid path.", e);
			return null;
		}
	}

	/**
	 * Processes all pdf files in a given directory with pdfx service which
	 * converts them into XML files. Stores the XML files in a given output
	 * directory.
	 *
	 * @param inputDirectoryPath
	 *            The directory that contains PDF files. It is scanned
	 *            recursively, non-pdf files will be ignored.
	 * @param outputDirectoryPath
	 *            The directory where the output XML files will be stored.
	 * @return a list of all the Paths of the generated output files
	 */
	public List<Path> process(Path inputPath, Path outputDirectoryPath) {
		logger.info("PdfxXmlCreator process started...");
		logger.info("Input directory: " + inputPath.toUri());

		List<Path> outputFiles = new ArrayList<>();

		if (!inputPath.toFile().isDirectory()) {
			logger.debug("Provided path is not a directory: " + inputPath.toUri());
			outputDirectoryPath = outputDirectoryPath == null ? inputPath : outputDirectoryPath;
			Path processed = singleFileProcess(inputPath, outputDirectoryPath);
			if (null != processed) {
				outputFiles.add(processed);
			}
			return outputFiles; // done
		} else {
			logger.debug("Provided path is a directory: " + inputPath.toUri());
			outputDirectoryPath = outputDirectoryPath == null ? inputPath.resolve("pdfx-out") : outputDirectoryPath;
			logger.info("Output directory: " + outputDirectoryPath.toUri());

			// create output directory
			if (!Files.exists(outputDirectoryPath)) {
				try {
					Files.createDirectory(outputDirectoryPath);
					logger.info("Successfully created output directory: " + outputDirectoryPath.toUri());
				} catch (IOException e) {
					logger.error("IO Exception occurred when trying to create output directory.", e);
				}
			}

			// process each PDF in the input directory
			List<Path> pdfFiles = getPdfListFromDirectory(inputPath);
			logger.info(pdfFiles.size() + " pdf files found.");
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
	}

	private Path singleFileProcess(Path pdfFile, Path outFile) {
		try {
			logger.info("processing file: " + outFile.toUri());
			if (processWithPdfx(pdfFile.toFile(), outFile)) {
				// output file was created
				return outFile;
			}
			logger.info("processing file [" + pdfFile.toUri() + "] finished.");
		} catch (IOException x) {
			logger.error(x.getMessage());
			logger.error("Failure!", x);
		}
		return null;
	}

	private static List<Path> getPdfListFromDirectory(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter(PDFChecker::isPDFFile).forEach(toProcess::add);
		} catch (IOException e) {
			logger.error("Exception occurred in reading the directory: " + inputDir.toUri());
			// todo change to throw exception
			e.printStackTrace();
		}
		return toProcess;
	}

	private boolean processWithPdfx(File pdf, Path outFile) throws IOException {
		if (!overwriteOutput && new File(outFile.toUri()).isFile()) {
			logger.error(
					"Output file [" + outFile.toUri() + "] already exists. Set 'overwriteOutput' attribute to true "
							+ "to overwrite existing files.");
			logger.info("Skipping process for file: " + pdf.getName());
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