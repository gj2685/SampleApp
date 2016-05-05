package com.example.micro.sample;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {
    public static final String JSON_URL = "http://api.androidhive.info/json/movies.json";
    public static final int JSON_FETCH_COMPLETED = 101;
    public static final int JSON_FETCH_ERROR = 102;

    public static int getDefaultCacheSize() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        return cacheSize;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected())
            return true;
        return false;
    }
}
