package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 15/12/16.
 */
public class Coordinates {
    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
