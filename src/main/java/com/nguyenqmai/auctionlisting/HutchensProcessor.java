package com.nguyenqmai.auctionlisting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by nguyenqmai on 11/17/2017.
 */
public class HutchensProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HutchensProcessor.class);


    static final String SEARCH_ANY_TEXT = "";
    static final String SEARCH_ALL = "AllRadio";
    static final String SEARCH_TEXT_BOX = "SearchTextBox";
    static final String SEARCH_GROUP = "SearchGroup";
    static final String EVENT_TARGET = "__EVENTTARGET";
    static final String COL_GROUP_ELEMENT = "colgroup";
    static final String SCRIPT_ELEMENT = "script";
    static final String[] STRIP_ELEMENTS = {COL_GROUP_ELEMENT, SCRIPT_ELEMENT};



    boolean readAllCases = false;
    Map<String, String> querySettings = new HashMap<>();
    Map<String, String> sessionForm = new HashMap<>();
    Set<String> processedPage = new HashSet<>();
    Queue<String> pages = new LinkedList<>();
    Map<String, CaseInformation> cases = new HashMap<>();

    Collection<CaseInformation> getCases() {
        return cases.values();
    }

    void process(SourceConfigSet.SourceConfig config, Charset charset, long secondsToProcessEachRecord) throws IOException, TransformerException, URISyntaxException {
        identifyQuerySettings(config.getQuery().getForm());
        processFirstPage(config, charset);

        Map<String, Object> requestForm;
        do {
            requestForm = new HashMap<>();
            requestForm.putAll(sessionForm);
            requestForm.putAll(querySettings);
            requestForm.remove("Button1");
            if (!pages.isEmpty()) {
                requestForm.remove("SearchButton");
                String toBePulledPage = pages.poll();
                if (processedPage.contains(toBePulledPage)) {
                    continue;
                }
                processedPage.add(toBePulledPage);
                requestForm.put(EVENT_TARGET, toBePulledPage);
                requestForm.put("__EVENTARGUMENT", "");
            }

            try {
                logger.info("Sleeping for {} seconds", config.getQuery().getSleepSeconds());
                Thread.sleep(config.getQuery().getSleepSeconds() * 1000);
            } catch (InterruptedException e) {
                logger.info("Thread sleep was interrupted");
            }
            String html = scrubHtml(Utils.syncRequest(config.getQuery().getUri(), requestForm, charset, secondsToProcessEachRecord));

            OutputStream rawForm = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("requestform"));
            sessionForm.putAll(HutchensProcessor.collectHutchersRequestForm(new InputStreamReader(new ByteArrayInputStream(rawForm.toString().getBytes(charset)))));

            OutputStream rawPages = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("pages"));
            putFoundPages(HutchensProcessor.collectHutchersPages(new InputStreamReader(new ByteArrayInputStream(rawPages.toString().getBytes(charset)))));

            OutputStream rawCases = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("cases"));
            putFoundCases(HutchensProcessor.processHutchersCases(new InputStreamReader(new ByteArrayInputStream(rawCases.toString().getBytes(charset)))));
        } while (!pages.isEmpty());
    }


    void processFirstPage(SourceConfigSet.SourceConfig config, Charset charset) throws IOException, TransformerException, URISyntaxException {
        String html = scrubHtml( Utils.wget(config.getQuery().getUri().toURL(), charset));

        OutputStream rawForm = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("requestform"));
        sessionForm.putAll(HutchensProcessor.collectHutchersRequestForm(new InputStreamReader(new ByteArrayInputStream(rawForm.toString().getBytes(charset)))));

        if (readAllCases) {
            OutputStream rawPages = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("pages"));
            putFoundPages(HutchensProcessor.collectHutchersPages(new InputStreamReader(new ByteArrayInputStream(rawPages.toString().getBytes(charset)))));

            OutputStream rawCases = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("cases"));
            putFoundCases(HutchensProcessor.processHutchersCases(new InputStreamReader(new ByteArrayInputStream(rawCases.toString().getBytes(charset)))));
        }
    }

    void identifyQuerySettings(Map<String, String> queryFormConfig) {
        String searchText = queryFormConfig.get(SEARCH_TEXT_BOX);
        searchText = searchText != null ? searchText : SEARCH_ANY_TEXT;
        querySettings.put(SEARCH_TEXT_BOX, searchText);

        String searchGroup = queryFormConfig.get(SEARCH_GROUP);
        searchGroup = searchGroup != null ? searchGroup : SEARCH_ALL;
        querySettings.put(SEARCH_GROUP, searchGroup);

        if (SEARCH_ALL.equals(searchGroup) && SEARCH_ANY_TEXT.equals(searchText)) {
            readAllCases = true;
        }
    }

    String scrubHtml(String html) {
        String tmp = Utils.cleanupHtmlTag(html, Utils.REPLACEMENT_HTML_TAG);
        tmp = Utils.replaceEntities(tmp, Utils.getKnownEntities());
        for (String element : STRIP_ELEMENTS) {
            tmp = Utils.removeElement(tmp, element);
        }
        return tmp;
    }

    void putFoundCases(Collection<CaseInformation> items) {
        for (CaseInformation item : items) {
            cases.put(item.getCaseNumber(), item);
        }
    }

    void putFoundPages(Collection<String> foundPages) {
        for (String page : foundPages) {
            if (processedPage.contains(page) || pages.contains(page)) {
                continue;
            }
            pages.add(page);
        }
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
