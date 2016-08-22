package eu.openminted.uc_tdm_socialsciences.io.pdfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Path> process(String inputDirectory, String outputDirectory) throws IOException {
        Path inputDirectoryPath = Paths.get(inputDirectory);
        //create output directory inside input directory
        Path outputDirectoryPath = inputDirectoryPath.resolve(outputDirectory);

        return process(inputDirectoryPath, outputDirectoryPath);
    }

    public List<Path> process(Path inputDirectoryPath, Path outputDirectoryPath) throws IOException {
        logger.info("PdfxXmlCreator process stated...");
		logger.info("Input directory: " + inputDirectoryPath.toUri());
        logger.info("Output directory: " + outputDirectoryPath.toUri());

        if (!inputDirectoryPath.toFile().isDirectory()) {
            //todo throw exception
            //todo or better, support single files as well
            logger.error("Provided path is not a directory: " + inputDirectoryPath.toUri());
            return null;
        }

        // create output directory
        if (!Files.exists(outputDirectoryPath)) {
            Files.createDirectory(outputDirectoryPath);
            logger.info("Successfully created output directory: " + outputDirectoryPath.toUri());
        }

        // process each PDF in the input directory
        List<Path> pdfFiles = getPdfListFromDirectory(inputDirectoryPath);
        List<Path> outputFiles = new ArrayList<>();
        logger.info(pdfFiles.size() + " pdf files found.");
        for (Path pdfFile : pdfFiles) {
            Path outFile = outputDirectoryPath.resolve(pdfFile.getFileName() + ".xml");
            try{
                logger.info("processing file: " + outFile.toUri());
                if(processWithPdfx(pdfFile.toFile(), outFile))
                    //output file was created
                    outputFiles.add(outFile);
                logger.info("processing file [" + pdfFile.toUri() + "] finished.");
            }catch (Exception x){
                logger.error(x.getMessage());
                logger.error("failure!", x);
            }
        }

        logger.info("PdfxXmlCreator process finished.");
        return outputFiles;
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

    private boolean processWithPdfx(File pdf, Path outFile)
            throws IOException
    {
        //todo skip the process if file already exists and overwriteOutput == false
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpEntity httpEntity = getFirstResponse(pdf, httpclient);

        String messageBody;

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
            throw new IOException("Second POST request failed." + System.lineSeparator() +
                    "HTTP response: " + response);
        }

        // final post
        HttpGet httpGet = new HttpGet(resultUrl + ".xml");
        CloseableHttpResponse result = httpclient.execute(httpGet);
        InputStream content = result.getEntity().getContent();
        boolean outputFileCreated = writeToFile(content, outFile);

        result.close();

        return outputFileCreated;
    }

    private boolean writeToFile(InputStream content, Path outputFilePath) throws IOException
    {
        boolean fileWritten = false;
        try {
            if (overwriteOutput) {
                Files.copy(content,
                        outputFilePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(content,
                        outputFilePath);
            }
            logger.info("File [" + outputFilePath.toUri() + "] created.");
            fileWritten = true;
        } catch (FileAlreadyExistsException e) {
            logger.error("Output file [" + e.getFile() + "] already exists. Set 'overwriteOutput' attribute to true " +
                    "to overwrite existing files.");
        }
        return fileWritten;
    }

    private static HttpEntity getFirstResponse(File pdf, CloseableHttpClient httpClient)
            throws IOException
    {
        HttpEntity result;
        HttpPost httpPost = new HttpPost(SERVICE_URL);

        HttpEntity entity = MultipartEntityBuilder.create().
                addTextBody(REQUEST_PARAM_SENT_SPLITTER, REQUEST_PARAM_SENT_SPLITTER_VALUE_PUNKT)
                .addTextBody(REQUEST_PARAM_CLIENT, REQUEST_PARAM_CLIENT_VALUE_WEB_INTERFACE)
                .addBinaryBody(REQUEST_PARAM_USERFILE, pdf,
                        ContentType.create(REQUEST_PARAM_USERFILE_TYPE_APPLICATION_PDF), pdf.getPath()).build();

        httpPost.setEntity(entity);
        CloseableHttpResponse response;

        response = httpClient.execute(httpPost);
        if (isHttpResponseSuccessful(response)) {
            result = response.getEntity();
        } else {
            logger.error("Request for " + pdf.getPath() + " was unsuccessful: "
                    + response.getStatusLine().getReasonPhrase());
            throw new IllegalArgumentException("Server returned null for parse request of file [" + pdf.getPath() + "]." +
                    System.lineSeparator() + " Entity contents: " + EntityUtils.toString(entity));
        }

        return result;
    }

    private HttpEntity getSecondResponse(CloseableHttpClient httpclient, String jobId)
            throws IOException
    {
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