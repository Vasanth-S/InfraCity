package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class Message {
    @SerializedName("date")
    String date;

    @SerializedName("message")
    String message;

    @SerializedName("user")
    String user;

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }
}
