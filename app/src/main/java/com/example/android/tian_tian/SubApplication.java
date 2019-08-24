package com.example.android.tian_tian;

import android.app.Application;
import android.preference.PreferenceManager;

import com.example.android.tian_tian.utilities.Helper;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class SubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
