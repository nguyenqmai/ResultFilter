package com.nguyenqmai.auctionlisting;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by nguyenqmai on 6/25/2017.
 */
class SourceConfigSet {
    private URI googleGeoCode;
    private Map<String, SourceConfig> sources;

    SourceConfigSet() {
    }

    public URI getGoogleGeoCode() {
        return googleGeoCode;
    }

    public Map<String, SourceConfig> getSources() {
        return sources;
    }

    static class SourceConfig {
        private Map<String, String> xslts;
        private Query query;

        public Path getXslt(String type) {
            return Paths.get(xslts.get(type));
        }

        public Query getQuery() {
            return query;
        }
    }

    static class Query {
        private URI uri;
        private long sleepSeconds;
        private String method;
        private Map<String, String> form;

        public URI getUri() {
            return uri;
        }

        public long getSleepSeconds() {
            return sleepSeconds;
        }

        public String getMethod() {
            return method;
        }

        public Map<String, String> getForm() {
            return form;
        }
    }

    static SourceConfigSet fromFile(Path filePath) throws IOException {
        try (Reader reader = new FileReader(filePath.toFile())) {
            return (new Gson()).fromJson(reader, SourceConfigSet.class);
        }
    }
}
