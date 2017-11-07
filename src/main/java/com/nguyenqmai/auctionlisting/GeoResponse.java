package com.nguyenqmai.auctionlisting;

import java.util.List;

/**
 * Created by nguyenqmai on 6/24/2017.
 */
public class GeoResponse {
    enum AddressType {
        street_number, route, locality, postal_code, postal_code_suffix,
        political, administrative_area_level_3, administrative_area_level_2, administrative_area_level_1, country

    }
    class AddressComponent {
        private AddressType[] types;
        private String long_name;
        private String short_name;

        public AddressComponent() {
        }

        public AddressType[] getTypes() {
            return types;
        }

        public String getLong_name() {
            return long_name;
        }

        public String getShort_name() {
            return short_name;
        }
    }

    class GeoLocation {
        private double lat;
        private double lng;

        public GeoLocation() {
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

    class GeoBounds {
        private GeoLocation northeast;
        private GeoLocation southwest;

        public GeoBounds() {
        }

        public GeoLocation getNortheast() {
            return northeast;
        }

        public GeoLocation getSouthwest() {
            return southwest;
        }
    }

    class Geometry {
        private GeoBounds bounds;
        private GeoBounds viewport;
        private GeoLocation location;
        private String location_type;

        public Geometry() {
        }

        public GeoBounds getBounds() {
            return bounds;
        }

        public GeoBounds getViewport() {
            return viewport;
        }

        public GeoLocation getLocation() {
            return location;
        }

        public String getLocation_type() {
            return location_type;
        }
    }

    class GeoResult {
        private List<AddressComponent> address_components;
        private String formatted_address;
        private Geometry geometry;
        private String place_id;
        private String[] types;

        public GeoResult() {
        }

        public List<AddressComponent> getAddress_components() {
            return address_components;
        }

        public String getFormatted_address() {
            return formatted_address;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public String getPlace_id() {
            return place_id;
        }

        public String[] getTypes() {
            return types;
        }
    }

    private String status;
    private GeoResult[] results;

    public GeoResponse() {
    }

    public String getStatus() {
        return status;
    }

    public GeoResult[] getResults() {
        return results;
    }
}
