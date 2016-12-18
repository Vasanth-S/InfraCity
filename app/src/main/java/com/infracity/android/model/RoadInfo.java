package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

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

    @SerializedName("encroachment")
    private int encroachments;
    @SerializedName("safety")
    private int safety;
    @SerializedName("platform_usability")
    private int platformUsability;
    @SerializedName("road_quality")
    private int roadQuality;

    @SerializedName("photos")
    private ArrayList<String> photos;

    @SerializedName("complaints")
    private String[] complaints;

    public String[] getComplaints() {
        return complaints;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public int getEncroachments() {
        return encroachments < 0 ? 0 : encroachments;
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

    public int getSafety() {
        return safety < 0 ? 0 : safety;
    }

    public int getPlatformUsability() {
        return platformUsability < 0 ? 0 : platformUsability;
    }

    public int getRoadQuality() {
        return roadQuality < 0 ? 0 : roadQuality;
    }
}
