package com.nguyenqmai.auctionlisting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by nguyenqmai on 11/17/2017.
 */
public class HutchensProcessor {
    Queue<String> pages = new LinkedList<>();
    Map<String, Object> sessionForm = new HashMap<>();

    List<CaseInformation> process(SourceConfigSet.SourceConfig config, Charset charset, long secondsToProcessEachRecord) throws IOException, TransformerException, URISyntaxException {
        List<CaseInformation> cases = new LinkedList<>();
        boolean firstPage = true;
        Map<String, Object> requestForm = new HashMap<>();
        do {

            if (!firstPage) {
                requestForm = new HashMap<>();
                requestForm.putAll(sessionForm);
                requestForm.putAll(config.getQuery().getForm());
                requestForm.put("__EVENTTARGET", pages.poll());
            }

            String html = firstPage ?
                    Utils.wget(config.getQuery().getUri().toURL(), charset) :
                    Utils.syncRequest(config.getQuery().getUri(), requestForm, charset, secondsToProcessEachRecord);

            html = Utils.cleanupHtmlTag(html, Utils.REPLACEMENT_HTML_TAG);
            html = Utils.replaceEntities(html, Utils.getKnownEntities());
            html = Utils.removeElement(html, "colgroup");

            if (firstPage) {
                OutputStream rawPages = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("pages"));
                pages.addAll(HutchensProcessor.collectHutchersPages(new InputStreamReader(new ByteArrayInputStream(rawPages.toString().getBytes(charset)))));

                OutputStream rawForm = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("requestform"));
                sessionForm.putAll(HutchensProcessor.collectHutchersRequestForm(new InputStreamReader(new ByteArrayInputStream(rawForm.toString().getBytes(charset)))));
            }

            OutputStream rawCases = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("cases"));
            cases.addAll(HutchensProcessor.processHutchersCases(new InputStreamReader(new ByteArrayInputStream(rawCases.toString().getBytes(charset)))));
            firstPage = false;
        } while (!pages.isEmpty());
        return cases;
    }


    List<CaseInformation> processFirstPage(SourceConfigSet.SourceConfig config, Charset charset) throws IOException, TransformerException, URISyntaxException {
        List<CaseInformation> cases = new LinkedList<>();

        String html = Utils.wget(config.getQuery().getUri().toURL(), charset);

        html = Utils.cleanupHtmlTag(html, Utils.REPLACEMENT_HTML_TAG);
        html = Utils.replaceEntities(html, Utils.getKnownEntities());
        html = Utils.removeElement(html, "colgroup");

        OutputStream rawPages = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("pages"));
        pages.addAll(HutchensProcessor.collectHutchersPages(new InputStreamReader(new ByteArrayInputStream(rawPages.toString().getBytes(charset)))));

        OutputStream rawForm = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("requestform"));
        sessionForm.putAll(HutchensProcessor.collectHutchersRequestForm(new InputStreamReader(new ByteArrayInputStream(rawForm.toString().getBytes(charset)))));

        OutputStream rawCases = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("cases"));
        cases.addAll(HutchensProcessor.processHutchersCases(new InputStreamReader(new ByteArrayInputStream(rawCases.toString().getBytes(charset)))));

        return cases;
    }

    public static Map<String, String> collectHutchersRequestForm(Reader input) throws IOException {
        Map<String, String> ret = new HashMap<>();
        Scanner scanner = new Scanner(input);
        while (scanner.hasNextLine()) {
            String keyValue = scanner.nextLine().trim();
            if (!keyValue.isEmpty()) {
                int index = keyValue.indexOf("=");
                ret.put(keyValue.substring(0, index), keyValue.substring(index + 1));
            }
        }
        return ret;
    }

    public static List<String> collectHutchersPages(Reader input) throws IOException {
        List<String> ret = new LinkedList<>();
        Scanner scanner = new Scanner(input);
        while (scanner.hasNextLine()) {
            String nextPage = scanner.nextLine().trim();
            if (!nextPage.isEmpty()) {
                ret.add(nextPage);
            }
        }
        return ret;
    }

    public static List<CaseInformation> processHutchersCases(Reader input) throws IOException {
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
