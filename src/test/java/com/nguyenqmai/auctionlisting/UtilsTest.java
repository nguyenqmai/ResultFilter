package com.nguyenqmai.auctionlisting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by nguyenqmai on 6/24/2017.
 */
public class UtilsTest {
    @Test
    public void cleanupHtmlTag() throws Exception {
        assertEquals(Utils.REPLACEMENT_HTML_TAG, Utils.cleanupHtmlTag("asjkas sda klas <html asdfa sa ads asf >", Utils.REPLACEMENT_HTML_TAG));
    }
}