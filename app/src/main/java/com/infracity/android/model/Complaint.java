package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class Complaint implements Serializable {
    @SerializedName("number")
    String id;

    @SerializedName("date")
    String date;

    @SerializedName("mobile_number")
    String phone;

    @SerializedName("email")
    String email;

    @SerializedName("address")
    String address;

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
