package com.example.android.push_chinese.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.push_chinese.utilities.Helper;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class EntryPoint extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean onboarded = preferences.getBoolean("onboarded", false);
        Log.w("ONBORDING", String.valueOf(onboarded));

        if(onboarded) {
            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainIntent);
        } else {
            Intent onboardingIntent = new Intent(getBaseContext(), Onboarding.class);
            startActivity(onboardingIntent);
        }
    }
}
