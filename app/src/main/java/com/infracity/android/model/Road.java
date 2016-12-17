package com.infracity.android.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by pragadeesh on 15/12/16.
 */
public class Road {

    @SerializedName("id")
    private int roadId;

    @SerializedName("points")
    private String points;

    @SerializedName("summary")
    private String summary;

    @SerializedName("rating")
    private int rating;

    public int getRating() {
        return rating;
    }

    public String getSummary() {
        return summary;
    }

    public ArrayList<LatLng> getPoints() {
        return decodePoints(points);
    }

    public int getId() {
        return roadId;
    }

    private ArrayList<LatLng> decodePoints(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
