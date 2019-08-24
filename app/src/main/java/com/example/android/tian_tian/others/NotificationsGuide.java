package com.example.android.tian_tian.others;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.example.android.tian_tian.R;

public class NotificationsGuide extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
