package com.infracity.android.model;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.infracity.android.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class InfraCityApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        setIP();
    }

    private void setIP () {
        StringBuilder text = new StringBuilder();
        File file = new File("/mnt/sdcard/ip.txt");
        try {
            BufferedReader e = new BufferedReader(new FileReader(file));
            String line;
            while((line = e.readLine()) != null) {
                text.append(line);
            }
            e.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Constants.SERVER = String.format("http://%s", text.toString());
        System.out.println("Server IP " + Constants.SERVER);
    }
}
