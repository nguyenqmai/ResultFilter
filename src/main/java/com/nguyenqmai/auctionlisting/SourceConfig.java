package com.nguyenqmai.auctionlisting;

import java.net.URL;
import java.nio.file.Path;

/**
 * Created by nguyenqmai on 6/25/2017.
 */
class SourceConfig {
    private URL url;
    private Path xsltPath;

    SourceConfig(URL url, Path xsltPath) {
        this.url = url;
        this.xsltPath = xsltPath;
    }

    public URL getUrl() {
        return url;
    }

    public Path getXsltPath() {
        return xsltPath;
    }
}
