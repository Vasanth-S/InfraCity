package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class RoadInfo {
    @SerializedName("area")
    private String area;

    @SerializedName("locality")
    private String locality;

    @SerializedName("street")
    private String street;

    @SerializedName("encroachments")
    private int encroachments;

    @SerializedName("photos")
    private String[] photos;

    @SerializedName("complaints")
    private String[] complaints;

    public String[] getComplaints() {
        return complaints;
    }

    public String[] getPhotos() {
        return photos;
    }

    public int getEncroachments() {
        return encroachments;
    }

    public String getStreet() {
        return street;
    }

    public String getLocality() {
        return locality;
    }

    public String getArea() {
        return area;
    }
}
