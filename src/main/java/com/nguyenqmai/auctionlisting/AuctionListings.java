package com.nguyenqmai.auctionlisting;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by nguyenqmai on 6/24/2017.
 */
public class AuctionListings {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListings.class);

    @Parameter(names = {"-c", "-sourceConfig"}, description = "JSON source config sets")
    private String sourceConfigSetFilePath;

    private AuctionListings() {
    }

//    void processSingleSource(URI googleGeoURI, SourceConfigSet.SourceConfig config, Charset charset, long secondsToProcessEachRecord) throws IOException, TransformerException, URISyntaxException {
//        String html = Utils.cleanupHtmlTag(Utils.syncRequest(config.getQuery(), charset, secondsToProcessEachRecord), Utils.REPLACEMENT_HTML_TAG);
//        html = Utils.replaceEntities(html, Utils.getKnownEntities());
//
//        OutputStream rawCases = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), config.getXslt("cases"));
//        List<CaseInformation> cases = HutchensProcessor.processHutchersCases(new InputStreamReader(new ByteArrayInputStream(rawCases.toString().getBytes(charset))));
//
//        for (CaseInformation _case : cases) {
//            logger.info("Pulling GeoCode of case# {}, at address {}", _case.getCaseNumber(), _case.getFullAddress());
//            _case.setGeoResponse(Utils.getGoogleGeoCode(googleGeoURI, _case.getFullAddress(), secondsToProcessEachRecord));
//        }
//    }

    void processAllSources() throws IOException {
        SourceConfigSet sourceConfigSet = SourceConfigSet.fromFile(Paths.get(this.sourceConfigSetFilePath));
        for (Map.Entry<String, SourceConfigSet.SourceConfig> pair : sourceConfigSet.getSources().entrySet()) {
            SourceConfigSet.SourceConfig config = pair.getValue();
            try {
                if ("hutchens".equalsIgnoreCase(pair.getKey())) {
                    HutchensProcessor processor = new HutchensProcessor();
                    processor.process(config, StandardCharsets.UTF_8, 5);
                    Collection<CaseInformation> cases = processor.getCases();
                }
            } catch (TransformerException | IOException | URISyntaxException e) {
                logger.error("Bad transforming data from source {}", pair.getKey(), e);
            }
        }
    }


    public static void main(String[] args) {
        try {
            AuctionListings auctionListings = new AuctionListings();
            JCommander.newBuilder()
                    .addObject(auctionListings)
                    .build()
                    .parse(args);
            auctionListings.processAllSources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
