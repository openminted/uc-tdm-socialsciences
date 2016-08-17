package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

import org.apache.log4j.Logger;
import util.PDFChecker;

public class PdfxXmlCreator {
	private static final Logger logger = Logger.getLogger(PdfxXmlCreator.class);
	private boolean overwriteOutput = false;

	public static final String SERVICE_URL = "http://pdfx.cs.man.ac.uk";

	public void process(Path inputDir, String outputDir) throws IOException {
		if (!inputDir.toFile().isDirectory()) {
			logger.error("Provided path is no directory.");
			return;
		}

		// create output directory
		Path out = inputDir.resolve(outputDir);
		if (!Files.exists(out)) {
			Files.createDirectory(out);
			logger.info("Successfully created output directory " + out.toUri());
		}

		// process each PDF in the input directory
		List<Path> pdffiles = getPdfsFromDir(inputDir);
		for (Path pdffile : pdffiles) {
			Path outFile = out.resolve(pdffile.getFileName() + ".xml");
			// TODO: process each pdf in own thread?
			processWithPdfx(pdffile.toFile(), outFile);
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

	private void processWithPdfx(File pdf, Path outFile) {
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

				String progressUrl = SERVICE_URL + body.split(":")[1].split("\"")[1];
				String jobId = body.split(":")[2].split("\"")[1];
				String resultUrl = SERVICE_URL + body.split(":")[resultPos].split("\"")[1];

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
					logger.error("Request for " + pdf.getName() + " was unsuccessful: "
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
	private void writeToFile(InputStream content, Path outputDir) {
		try {
			if (overwriteOutput){
				Files.copy(content,
						outputDir , StandardCopyOption.REPLACE_EXISTING );
			}else{
				Files.copy(content,
						outputDir);
			}
		} catch (FileAlreadyExistsException e) {
			logger.error("Output file ["+e.getFile()+"] already exists. Set 'overwriteOutput' attribute to true " +
					"to overwrite existing files.");
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
				logger.error("Request for " + pdf.getName() + " was unsuccessful: "
						+ response.getStatusLine().getReasonPhrase());
				return null;
			}

			return response.getEntity();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean isOverwriteOutput() {
		return overwriteOutput;
	}

	public void setOverwriteOutput(boolean overwriteOutput) {
		this.overwriteOutput = overwriteOutput;
	}
}