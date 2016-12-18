package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class ComplaintResponse {
    @SerializedName("complainant")
    Complaint complaint;

    @SerializedName("messages")
    ArrayList<Message> messages;

    public Complaint getComplaint() {
        return complaint;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}
