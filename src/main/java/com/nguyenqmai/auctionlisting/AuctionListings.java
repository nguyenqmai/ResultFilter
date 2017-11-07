package com.nguyenqmai.auctionlisting;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by nguyenqmai on 6/24/2017.
 */
public class AuctionListings {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListings.class);

    private Configuration settings;

    private AuctionListings(Configuration settings) {
        this.settings = settings;
    }


    Map<String, SourceConfig> getSources() {
        Map<String, SourceConfig> ret = new HashMap<>();
        for (String source : settings.getStringArray("sources")) {
            Configuration subs = settings.subset(source);
            String urlStr = subs.getString("url");
            try {
                ret.put(source, new SourceConfig(new URL(urlStr), Paths.get(subs.getString("xslt"))));
            } catch (MalformedURLException e) {
                logger.error("Bad auction listing source {} url {}", source, urlStr, e);
            }
        }
        return ret;
    }

    void processSingleSource(URL url, Path xsltFile, Charset charset, long secondsToProcessEachRecord) throws IOException, TransformerException, URISyntaxException {
        String html = Utils.cleanupHtmlTag(Utils.wget(url, charset), Utils.REPLACEMENT_HTML_TAG);
        html = Utils.replaceEntities(html, Utils.getKnownEntities());
        OutputStream out = Utils.simpleTransform(new ByteArrayInputStream(html.getBytes(charset)), xsltFile);

        List<CaseInformation> cases = Utils.fromHutchersSource(new InputStreamReader(new ByteArrayInputStream(out.toString().getBytes(charset))));

        URI googleGeoURI = new URI(settings.getString("googleGeoCode"));
        for (CaseInformation _case : cases) {
            logger.info("Pulling GeoCode of case# {}, at address {}", _case.getCaseNumber(), _case.getFullAddress());
            _case.setGeoResponse(Utils.getGoogleGeoCode(googleGeoURI, _case.getFullAddress(), secondsToProcessEachRecord));
        }
        int abc = 123;
    }

    void processAllSources() {
        for (Map.Entry<String, SourceConfig> pair : getSources().entrySet()) {
            SourceConfig config = pair.getValue();
            try {
                processSingleSource(config.getUrl(), config.getXsltPath(), StandardCharsets.UTF_8, 5);
            } catch (TransformerException | IOException | URISyntaxException e) {
                logger.error("Bad transforming data from source {}", pair.getKey(), e);
            }
        }
    }


    public static void main(String[] args) {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(args[0]));

            AuctionListings listings = new AuctionListings(builder.getConfiguration());
            listings.processAllSources();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
