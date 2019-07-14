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
package com.example.android.everytian.activities;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.everytian.utilities.NotifyWorker;
import com.example.android.everytian.R;
import com.example.android.everytian.utilities.SimpleFragmentPagerAdaptor;
import com.example.android.everytian.entities.Word;
import com.example.android.everytian.fragments.VocabularyFragment;
import com.example.android.everytian.utilities.SRScheduler;
import com.example.android.everytian.utilities.VoiceRecorder;
import com.google.android.material.tabs.TabLayout;
import com.raodevs.touchdraw.TouchDrawView;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements VocabularyFragment.Listener, SRScheduler.SRSchedulerInterface, VocabularyFragment.KeboardHandler {



    SimpleFragmentPagerAdaptor adapter;
    TabLayout tabLayout;
    SRScheduler sRScheduler = null;
    View drawingBoard;
    TouchDrawView touchDrawView;
    boolean showingdrawingBoard = false;
    VoiceRecorder voiceRecorder;

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

    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
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
        if (id == R.id.draw_item) {
            if (!showingdrawingBoard) {
                item.setIcon(R.drawable.icon_close);
                showingdrawingBoard = true;
                drawingBoard.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.Landing).duration(300).playOn(drawingBoard);

                // Close voice recording
                menu.getItem(0).setIcon(R.drawable.icon_record);
                voiceRecorder.hide();
            }
            else {
                showingdrawingBoard = false;
                item.setIcon(R.drawable.gesture);
                YoYo.with(Techniques.TakingOff).duration(300).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        drawingBoard.setVisibility(View.GONE);
                    }
                }).playOn(drawingBoard);
            }
        }
        if (id == R.id.voice_recorder_item) {
            if (voiceRecorder.toggle()) {
                item.setIcon(R.drawable.icon_close);

                // Close drawing panel
                showingdrawingBoard = false;
                menu.getItem(1).setIcon(R.drawable.gesture);
                YoYo.with(Techniques.TakingOff).duration(300).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        drawingBoard.setVisibility(View.GONE);
                    }
                }).playOn(drawingBoard);
            }
            else  item.setIcon(R.drawable.icon_record);

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
        drawingBoard = findViewById(R.id.drawing_board);
        touchDrawView = (TouchDrawView) findViewById(R.id.touch_draw_view);
        touchDrawView.setPaintColor(R.color.black);// for changing paint color
        touchDrawView.setStrokeWidth(20f);// for changing stroke width
        touchDrawView.clear();

        voiceRecorder = findViewById(R.id.voice_recorder_panel);

        findViewById(R.id.undo_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).playOn(view);
                touchDrawView.undo();
            }
        });
        findViewById(R.id.clear_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).playOn(view);
                touchDrawView.clear();
            }
        });
        findViewById(R.id.redo_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).playOn(view);
                touchDrawView.redo();
            }
        });

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        voiceRecorder.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}