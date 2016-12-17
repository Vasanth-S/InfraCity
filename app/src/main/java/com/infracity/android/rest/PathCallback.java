package com.infracity.android.rest;

import com.infracity.android.model.Path;

/**
 * Created by pragadeesh on 14/12/16.
 */
public interface PathCallback {
    void onLoadSuccess(Path path);
    void onLoadFailure(String errorMessage);
}
