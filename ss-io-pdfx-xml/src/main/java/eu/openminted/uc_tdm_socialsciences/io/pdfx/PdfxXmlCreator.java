package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import util.PDFChecker;

public class PdfxXmlCreator {

	public static final String OUTPUT_RESOURCE_DIR_NAME = "pdfx-out";
	public static final String SERVICE_URL = "http://pdfx.cs.man.ac.uk";

	public static void process(Path inputDir) throws IOException {
		if (!inputDir.toFile().isDirectory()) {
			System.err.println("Provided path is no directory.");
			return;
		}

		// create output directory
		Path outputDir = inputDir.resolve(OUTPUT_RESOURCE_DIR_NAME);
		if (!Files.exists(outputDir)) {
			Files.createDirectory(outputDir);
			System.out.println("Successfully created output directory " + outputDir.toUri());
		}

		// process each PDF in the input directory
		List<Path> pdffiles = getPdfsFromDir(inputDir);
		for (Path pdffile : pdffiles) {
			Path out = outputDir.resolve(pdffile.getFileName() + ".xml");
			// TODO: process each pdf in own thread?
			processWithPdfx(pdffile.toFile(), out);
		}
	}

	private static List<Path> getPdfsFromDir(Path inputDir) {
		List<Path> toProcess = new ArrayList<>();
		try {
			Files.walk(inputDir).filter(Files::isRegularFile).filter(PDFChecker::isPDFFile).forEach(toProcess::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toProcess;
	}

	private static void processWithPdfx(File pdf, Path outFile) {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpEntity entity = getFirstResponse(pdf, httpclient);

		if (entity != null) {
			String body;
			try {
				body = EntityUtils.toString(entity);
				// System.out.println(body);

				int resultPos = 3;
				if (body.split(":").length == 5) {
					resultPos = 4;
				}

				String progressUrl = SERVICE_URL + (body.split(":")[1]).split("\"")[1];
				String jobId = (body.split(":")[2]).split("\"")[1];
				String resultUrl = SERVICE_URL + (body.split(":")[resultPos]).split("\"")[1];

				// second post
				HttpPost httpPost = new HttpPost(SERVICE_URL);
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.addTextBody("job_id", jobId);
				builder.addTextBody("client", "web-interface");
				builder.addTextBody("sent_splitter", "punkt");
				httpPost.setEntity(builder.build());
				String response = EntityUtils.toString(httpclient.execute(httpPost).getEntity());
				// System.out.println(response);

				if (response.contains("error")) {
					System.err.println("Request for " + pdf.getName() + " was unsuccessful: "
							+ response.split(":")[1].split("\"")[1]);
					return;
				}

				// System.out.println("progressUrl: " + progressUrl);
				// System.out.println("jobId: " + jobId);
				// System.out.println("resultUrl: " + resultUrl);

				// final post
				HttpGet httpGet = new HttpGet(resultUrl + ".xml");
				CloseableHttpResponse result = httpclient.execute(httpGet);

				InputStream content = result.getEntity().getContent();

				writeToFile(content, outFile);

				EntityUtils.consume(entity);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO decide if overwrite existing or ignore
	private static void writeToFile(InputStream content, Path outputDir) {
		try {
			Files.copy(content,
					outputDir/* , StandardCopyOption.REPLACE_EXISTING */);
		} catch (FileAlreadyExistsException e) {
			System.err.println("Output file already exists.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static HttpEntity getFirstResponse(File pdf, CloseableHttpClient httpclient) {
		HttpPost httpPost = new HttpPost(SERVICE_URL);

		HttpEntity entity = MultipartEntityBuilder.create().addTextBody("sent_splitter", "punkt")
				.addTextBody("client", "web-interface")
				.addBinaryBody("userfile", pdf, ContentType.create("application/pdf"), pdf.getName()).build();

		httpPost.setEntity(entity);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.err.println("Request for " + pdf.getName() + " was unsuccessful: "
						+ response.getStatusLine().getReasonPhrase());
				return null;
			}

			return response.getEntity();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}