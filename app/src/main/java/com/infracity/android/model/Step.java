package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 15/12/16.
 */
public class Step {
    @SerializedName("end_location")
    private Coordinates endLocation;

    @SerializedName("start_location")
    private Coordinates startLocation;

    @SerializedName("polyline")
    private Road polyline;

    public Coordinates getEndLocation() {
        return endLocation;
    }

    public Coordinates getStartLocation() {
        return startLocation;
    }

    public Road getPolyline() {
        return polyline;
    }
}
