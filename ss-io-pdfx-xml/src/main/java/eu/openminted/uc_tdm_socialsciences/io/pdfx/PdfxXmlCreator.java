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
    public static final String REQUEST_PARAM_JOB_ID = "job_id";
    public static final String REQUEST_PARAM_CLIENT = "client";
    public static final String REQUEST_PARAM_SENT_SPLITTER = "sent_splitter";
    public static final String REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE = "web-interface";
    public static final String REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT = "punkt";
    public static final String REQUEST_PARAM_USERFILE = "userfile";
    public static final String REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF = "application/pdf";
    private boolean overwriteOutput = false;

    public static final String SERVICE_URL = "http://pdfx.cs.man.ac.uk";

    public void process(Path inputDir, String outputDir) throws IOException {
        if (!inputDir.toFile().isDirectory()) {
            logger.error("Provided path is not a directory: " + inputDir.toUri());
            return;
        }

        // create output directory
        Path out = inputDir.resolve(outputDir);
        if (!Files.exists(out)) {
            Files.createDirectory(out);
            logger.info("Successfully created output directory: " + out.toUri());
        }

        // process each PDF in the input directory
        List<Path> pdfFiles = getPdfsFromDir(inputDir);
        for (Path pdfFile : pdfFiles) {
            Path outFile = out.resolve(pdfFile.getFileName() + ".xml");
            processWithPdfx(pdfFile.toFile(), outFile);
        }
    }

    private static List<Path> getPdfsFromDir(Path inputDir) {
        List<Path> toProcess = new ArrayList<>();
        try {
            Files.walk(inputDir).filter(Files::isRegularFile).filter(PDFChecker::isPDFFile).forEach(toProcess::add);
        } catch (IOException e) {
            logger.error("Exception occurred in reading the directory: " + inputDir.toUri());
            //todo change to throw exception
            e.printStackTrace();
        }
        return toProcess;
    }

    private void processWithPdfx(File pdf, Path outFile) {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpEntity entity = getFirstResponse(pdf, httpclient);

        if (entity != null) {
            String messageBody;
            try {
                messageBody = EntityUtils.toString(entity);

                final int progressUrlPosition = 1;
                final int jobIdPosition = 2;
                int resultPosition = 3;

                if (messageBody.split(":").length == 5) {
                    //resource was previously processed by the server
                    resultPosition = 4;
                }

                String progressUrl = SERVICE_URL + messageBody.split(":")[progressUrlPosition].split("\"")[1];
                String jobId = messageBody.split(":")[jobIdPosition].split("\"")[1];
                String resultUrl = SERVICE_URL + messageBody.split(":")[resultPosition].split("\"")[1];

                // second post
                HttpPost httpPost = new HttpPost(SERVICE_URL);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody(REQUEST_PARAM_JOB_ID, jobId);
                builder.addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE);
                builder.addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT);
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
            if (overwriteOutput) {
                Files.copy(content,
                        outputDir, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(content,
                        outputDir);
            }
        } catch (FileAlreadyExistsException e) {
            logger.error("Output file [" + e.getFile() + "] already exists. Set 'overwriteOutput' attribute to true " +
                    "to overwrite existing files.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpEntity getFirstResponse(File pdf, CloseableHttpClient httpclient) {
        HttpPost httpPost = new HttpPost(SERVICE_URL);

        HttpEntity entity = MultipartEntityBuilder.create().
                addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT)
                .addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE)
                .addBinaryBody(REQUEST_PARAM_USERFILE, pdf,
                        ContentType.create(REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF), pdf.getName()).build();

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