package com.infracity.android.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.infracity.android.model.Path;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pragadeesh on 14/12/16.
 */
public class DirectionsApi extends RestApi {

    public static void loadPaths(Context context, LatLng start, LatLng stop, PathCallback pathCallback) {
        if(context == null) {
            if(pathCallback != null) pathCallback.onLoadFailure("Context is null");
        } else if (!isConnected(context)){
            if(pathCallback != null) pathCallback.onLoadFailure("Network not available");
        } else {
            LoadPathTask loadPathTask = new LoadPathTask();
            loadPathTask.setPathCallback(pathCallback);
            loadPathTask.execute(start, stop);
        }
    }

    private static class LoadPathTask extends AsyncTask<LatLng, Void, Path> {

        private PathCallback pathCallback;

        public void setPathCallback(PathCallback pathCallback) {
            this.pathCallback = pathCallback;
        }

        @Override
        protected Path doInBackground(LatLng... coordinates) {
            Path path = null;
            try {
                String requestFormat = "http://maps.googleapis.com/maps/api/directions/json?origin=%s,%s&destination=%s,%s";
                String request = String.format(requestFormat,
                        String.valueOf(coordinates[0].latitude), String.valueOf(coordinates[0].longitude),
                        String.valueOf(coordinates[1].latitude), String.valueOf(coordinates[1].longitude));
                String response = getRequest(request);
                JSONObject pathObject = null;
                JSONObject object = new JSONObject(response);
                if(object.has("routes")) {
                    JSONArray routes = object.getJSONArray("routes");
                    if(routes.length() > 0) {
                        JSONObject route = (JSONObject) routes.get(0);
                        if(route.has("legs")) {
                            JSONArray paths = route.getJSONArray("legs");
                            if(paths.length() > 0) {
                                pathObject = (JSONObject) paths.get(0);
                            }
                        }
                    }
                }
                if(pathObject != null) {
                    Gson gson = new Gson();
                    path = gson.fromJson(pathObject.toString(), Path.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return path;
        }

        @Override
        protected void onPostExecute(Path path) {
            super.onPostExecute(path);
            if(pathCallback != null) {
                pathCallback.onLoadSuccess(path);
            }
        }
    }
}
