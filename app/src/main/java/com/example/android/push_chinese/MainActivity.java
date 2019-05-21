/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version test2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.push_chinese;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.android.push_chinese.data.PushDbContract;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MainActivity extends AppCompatActivity implements VocabularyFragment.Listener {

    SimpleFragmentPagerAdaptor adapter;
    TabLayout tabLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWorker();

        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        SimpleFragmentPagerAdaptor adapter = new SimpleFragmentPagerAdaptor(getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("notifications",true).apply();

    }

    @Override
    public void onWordSelected(Word word) {
        tabLayout.getTabAt(0).select();
    }

    public void startWorker() {
        //we set a tag to be able to cancel all work of this type if needed
        final String workTag = "notifications";

        //store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder().putInt("dummy", 1).build();
        // we then retrieve it inside the NotifyWorker with:
        // final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(
                NotifyWorker.class,
                15,
                TimeUnit.MINUTES
        ).setInputData(inputData).addTag(workTag).build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }
}
