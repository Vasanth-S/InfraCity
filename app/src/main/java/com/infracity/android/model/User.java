package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class User {
    @SerializedName("email")
    private String email;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("id")
    private int UUID;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public int getUUID() {
        return UUID;
    }
}
