package com.nguyenqmai.auctionlisting;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by nguyenqmai on 6/24/2017.
 */
public class CaseInformation {

    private String caseNumber;
    private String spNumber;
    private String county;
    private Date saleDate;
    private String badDate;
    private String streetAddress;
    private String countyStateZipAddress;
    private String deedBookPage;
    private String biddingNote;
    private GeoResponse geoResponse;

    private CaseInformation() {
    }

    public void setGeoResponse(GeoResponse geoResponse) {
        this.geoResponse = geoResponse;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public String getSpNumber() {
        return spNumber;
    }

    public String getCounty() {
        return county;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public String getBadDate() {
        return badDate;
    }

    public boolean hasBadDate() {
        return (badDate != null);
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCountyStateZipAddress() {
        return countyStateZipAddress;
    }

    public String getDeedBookPage() {
        return deedBookPage;
    }

    public String getBiddingNote() {
        return biddingNote;
    }

    public String getFullAddress() {
        return getStreetAddress() + getCountyStateZipAddress();
    }

    public GeoResponse getGeoResponse() {
        return geoResponse;
    }

    public GeoResponse.GeoLocation getGeoLocation() {
        if (geoResponse != null &&
                "OK".equalsIgnoreCase(geoResponse.getStatus()) &&
                geoResponse.getResults() != null && geoResponse.getResults().length > 0) {
            return geoResponse.getResults()[0].getGeometry().getLocation();
        }
        return null;
    }

    static class Builder {
        private static FastDateFormat FORMATTER = FastDateFormat.getInstance("M/d/yyyy");
        private CaseInformation caseInfo;

        private Builder() {
            caseInfo = new CaseInformation();
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public Builder setCaseNumber(String caseNumber) {
            caseInfo.caseNumber = caseNumber;
            return this;
        }

        public Builder setSpNumber(String spNumber) {
            caseInfo.spNumber = spNumber;
            return this;
        }

        public Builder setCounty(String county) {
            caseInfo.county = county;
            return this;
        }

        public Builder setSaleDate(String date) {
            try {
                caseInfo.saleDate = FORMATTER.parse(date);
            } catch (ParseException e) {
                caseInfo.badDate = date;
                caseInfo.saleDate = new Date(System.currentTimeMillis());
            }
            return this;
        }


        public Builder setStreetAddress(String streetAddress) {
            caseInfo.streetAddress = streetAddress;
            return this;
        }

        public Builder setCountyStateZipAddress(String countyStateZipAddress) {
            caseInfo.countyStateZipAddress = countyStateZipAddress;
            return this;
        }

        public Builder setDeedBookPage(String deedBookPage) {
            caseInfo.deedBookPage = deedBookPage;
            return this;
        }

        public Builder setNote(String note) {
            caseInfo.biddingNote = note;
            return this;
        }

        public CaseInformation build() {
            CaseInformation ret = caseInfo;
            caseInfo = new CaseInformation();
            return ret;
        }
    }
}
