package com.infracity.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by pragadeesh on 14/12/16.
 */
public class Path {
    @SerializedName("steps")
    private ArrayList<Step> steps;

    public ArrayList<Step> getSteps() {
        return steps;
    }
}
