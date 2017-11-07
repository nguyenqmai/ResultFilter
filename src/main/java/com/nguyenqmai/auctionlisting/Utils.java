package com.nguyenqmai.auctionlisting;

import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nguyenqmai on 6/24/2017.
 */

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String REPLACEMENT_HTML_TAG = "<?xml version=\"1.0\"?>\n" +
            "<html>";

    public static Map<String, String> getKnownEntities() {
        Map<String, String> ret = new HashMap<>();
        ret.put("&copy;", "");
        ret.put("&nbsp;", "");
        return ret;
    }

    public static String wget(URL url, Charset charset) throws IOException {
        ByteArrayOutputStream ret = new ByteArrayOutputStream();

        int read = -1;
        byte[] buffer = new byte[8 * 1024];
        try (InputStream is = url.openStream()) {
            while ((read = is.read(buffer)) != -1) {
                if (read > 0) {
                    ret.write(buffer, 0, read);
                }
            }
        }
        return new String(ret.toByteArray(), charset);
    }

    public static String cleanupHtmlTag(String inputHtml, String replacementHtmlTag) {
        int startTag = inputHtml.indexOf("<html");
        int closeOfStartTag = inputHtml.indexOf(">", startTag);

        return replacementHtmlTag + inputHtml.substring(closeOfStartTag + 1);
    }

    public static String replaceEntities(String input, Map<String, String> entities) {
        String ret = input;
        for (Map.Entry<String, String> pair : entities.entrySet()) {
            ret = ret.replaceAll(pair.getKey(), pair.getValue());
        }
        return ret;
    }

    public static OutputStream simpleTransform(InputStream xmlInput, Path xsltPath) throws TransformerException {
        OutputStream output = new ByteArrayOutputStream();
        simpleTransform(xmlInput, xsltPath, output);
        return output;
    }

    public static void simpleTransform(InputStream xmlInput, Path xsltPath, OutputStream output) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(xsltPath.toFile()));
        transformer.transform(new StreamSource(xmlInput), new StreamResult(output));
    }

    //http://maps.google.com/maps/api/geocode/json?=
    public static GeoResponse getGoogleGeoCode(URI service, String address, long timeToLiveSeconds) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder(service);
        builder.addParameter("address", address);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().
                setConnectionTimeToLive(timeToLiveSeconds, TimeUnit.SECONDS).build()) {
            HttpGet httpGet = new HttpGet(builder.build());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return (new Gson()).fromJson(new InputStreamReader(response.getEntity().getContent()), GeoResponse.class);
            }
        }
    }

    public static List<CaseInformation> fromHutchersSource(Reader input) throws IOException {
        List<CaseInformation> ret = new LinkedList<>();
        CSVParser parser = CSVFormat.RFC4180.parse(input);
        CaseInformation.Builder builder = CaseInformation.Builder.getBuilder();

        for (CSVRecord record : parser.getRecords()) {
            ret.add(builder.setCaseNumber(record.get(0)).
                    setSpNumber(record.get(1)).
                    setCounty(record.get(2)).
                    setSaleDate(record.get(3)).
                    setStreetAddress(record.get(4)).
                    setCountyStateZipAddress(record.get(5)).
                    setDeedBookPage(record.get(6)).
                    setNote(record.get(7)).
                    build());
        }
        return ret;
    }
}
