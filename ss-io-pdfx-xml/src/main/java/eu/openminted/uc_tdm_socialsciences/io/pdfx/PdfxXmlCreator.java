package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.PDFChecker;

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

    public void process(String inputDirectory, String outputDirectory) throws IOException {
        Path inputDirectoryPath = Paths.get(inputDirectory);
        //create output directory inside input directory
        Path outputDirectoryPath = inputDirectoryPath.resolve(outputDirectory);

        process(inputDirectoryPath, outputDirectoryPath);
    }

    public void process(Path inputDirectoryPath, Path outputDirectoryPath) throws IOException {
        logger.info("PdfxXmlCreator process stated...");
        logger.info("Output directory: " + inputDirectoryPath.toUri());
        logger.info("Output directory: " + outputDirectoryPath.toUri());

        if (!inputDirectoryPath.toFile().isDirectory()) {
            //todo throw exception
            logger.error("Provided path is not a directory: " + inputDirectoryPath.toUri());
            return;
        }

        // create output directory
        if (!Files.exists(outputDirectoryPath)) {
            Files.createDirectory(outputDirectoryPath);
            logger.info("Successfully created output directory: " + outputDirectoryPath.toUri());
        }

        // process each PDF in the input directory
        List<Path> pdfFiles = getPdfListFromDirectory(inputDirectoryPath);
        logger.info(pdfFiles.size() + " pdf files found.");
        for (Path pdfFile : pdfFiles) {
            Path outFile = outputDirectoryPath.resolve(pdfFile.getFileName() + ".xml");
            processWithPdfx(pdfFile.toFile(), outFile);
        }

        logger.info("PdfxXmlCreator process finished.");
    }

    private static List<Path> getPdfListFromDirectory(Path inputDir) {
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
        logger.info("processing file: " + pdf.getName());
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpEntity httpEntity = getFirstResponse(pdf, httpclient);

        if (httpEntity != null) {
            String messageBody;
            try {
                messageBody = EntityUtils.toString(httpEntity);

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

                //dispose httpEntity
                EntityUtils.consume(httpEntity);

                // second post
                httpEntity = getSecondResponse(httpclient, jobId);
                String response = EntityUtils.toString(httpEntity);
                EntityUtils.consume(httpEntity);

                if (response.contains(REQUEST_RESPONSE_VALUE_ERROR)) {
                    logger.error("Request for '" + pdf.getName() + "' was unsuccessful: "
                            + response.split(":")[1].split("\"")[1]);
                    return;
                }

                // final post
                HttpGet httpGet = new HttpGet(resultUrl + ".xml");
                CloseableHttpResponse result = httpclient.execute(httpGet);
                InputStream content = result.getEntity().getContent();
                writeToFile(content, outFile);

                result.close();
            } catch (ParseException | IOException e) {
                //todo throw exception
                e.printStackTrace();
            }
        }

        logger.info("processing file [" + pdf.getName() + "] finished.");
    }

    private void writeToFile(InputStream content, Path outputFilePath) {
        try {
            if (overwriteOutput) {
                Files.copy(content,
                        outputFilePath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("File [" + outputFilePath.toUri() + "] created.");
            } else {
                Files.copy(content,
                        outputFilePath);
                logger.info("File [" + outputFilePath.toUri() + "] created.");
            }
        } catch (FileAlreadyExistsException e) {
            logger.error("Output file [" + e.getFile() + "] already exists. Set 'overwriteOutput' attribute to true " +
                    "to overwrite existing files.");
        } catch (IOException e) {
            //todo throw exception or log error?
            e.printStackTrace();
        }
    }

    private static HttpEntity getFirstResponse(File pdf, CloseableHttpClient httpClient) {
        HttpEntity result = null;
        HttpPost httpPost = new HttpPost(SERVICE_URL);

        HttpEntity entity = MultipartEntityBuilder.create().
                addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT)
                .addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE)
                .addBinaryBody(REQUEST_PARAM_USERFILE, pdf,
                        ContentType.create(REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF), pdf.getName()).build();

        httpPost.setEntity(entity);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPost);
            if (isHttpResponseSuccessful(response)) {
                result = response.getEntity();
            } else {
                logger.error("Request for " + pdf.getName() + " was unsuccessful: "
                        + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            //todo throw exception?
            e.printStackTrace();
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

    public static boolean isHttpResponseSuccessful(CloseableHttpResponse response){
        return response.getStatusLine().getStatusCode() == 200;
    }

    public boolean isOverwriteOutput() {
        return overwriteOutput;
    }

    public void setOverwriteOutput(boolean overwriteOutput) {
        this.overwriteOutput = overwriteOutput;
    }
}