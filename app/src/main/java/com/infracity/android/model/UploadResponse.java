package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class UploadResponse {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }
}
