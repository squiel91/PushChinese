package com.example.android.everytian.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;

import com.example.android.everytian.R;

public class OpenSource extends AppCompatActivity {

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

        // CC-CEDICT
        View.OnClickListener ccCedictOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://cc-cedict.org/wiki/start";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

            }
        };
        findViewById(R.id.link_cc_cedict).setOnClickListener(ccCedictOnClickListener);
        findViewById(R.id.link_cc_cedict_expand).setOnClickListener(ccCedictOnClickListener);

        // Tatoeba
        View.OnClickListener tatoebaOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://tatoeba.org/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

            }
        };
        findViewById(R.id.link_tatoeba).setOnClickListener(tatoebaOnClickListener);
        findViewById(R.id.link_tatoeba_expand).setOnClickListener(tatoebaOnClickListener);

        // Android View Animations
        View.OnClickListener AVAOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://github.com/daimajia/AndroidViewAnimations";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

            }
        };
        findViewById(R.id.link_android_view_animations).setOnClickListener(AVAOnClickListener);
        findViewById(R.id.link_android_view_animations_expand).setOnClickListener(AVAOnClickListener);

        // WilliamChart
        View.OnClickListener williamchartOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://github.com/diogobernardino/WilliamChart";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

            }
        };
        findViewById(R.id.link_williamchart).setOnClickListener(williamchartOnClickListener);
        findViewById(R.id.link_williamchart_expand).setOnClickListener(williamchartOnClickListener);

        // EventBus
        View.OnClickListener eventbusOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://github.com/greenrobot/EventBus";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

            }
        };
        findViewById(R.id.link_eventbus).setOnClickListener(eventbusOnClickListener);
        findViewById(R.id.link_eventbus_expand).setOnClickListener(eventbusOnClickListener);
    }
}
