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
package com.example.android.push_chinese.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.android.push_chinese.utilities.NotifyWorker;
import com.example.android.push_chinese.R;
import com.example.android.push_chinese.utilities.SimpleFragmentPagerAdaptor;
import com.example.android.push_chinese.entities.Word;
import com.example.android.push_chinese.fragments.VocabularyFragment;
import com.example.android.push_chinese.utilities.SRScheduler;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements VocabularyFragment.Listener, SRScheduler.SRSchedulerInterface, VocabularyFragment.KeboardHandler {



    SimpleFragmentPagerAdaptor adapter;
    TabLayout tabLayout;
    SRScheduler sRScheduler = null;

    @Subscribe
    public void wordQuantityChanged(Integer newWordQuantity) {
//        Log.w("onWordSelectedEvent", newWordQuantity.toString());
        sRScheduler.wordsEachDay = newWordQuantity;
    };

    @Override
    public SRScheduler getSRScheduler() {
        if (sRScheduler == null) {
            sRScheduler = new SRScheduler(this);
        }
        return sRScheduler;
    }

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onCreate(savedInstanceState);
        startWorker();

        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        SimpleFragmentPagerAdaptor adapter = new SimpleFragmentPagerAdaptor(getSupportFragmentManager());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                closeKeyboard();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

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

    @Override
    public void openKeyboard(EditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void closeKeyboard() {
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View focused_view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (focused_view == null) {
            focused_view = new View(activity);
        }
        imm.hideSoftInputFromWindow(focused_view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}
