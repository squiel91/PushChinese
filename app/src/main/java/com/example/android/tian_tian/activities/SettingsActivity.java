package com.example.android.tian_tian.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.tian_tian.BusEvents.LearnQuantityUpdated;
import com.example.android.tian_tian.R;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends AppCompatActivity {

    int learningMultiplier = 2;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Push Chinese Notifications
        boolean notifications = preferences.getBoolean("notifications", true);

        final Switch notificationsSwitch = findViewById(R.id.settings_notifications_switch);
        notificationsSwitch.setChecked(notifications);

        findViewById(R.id.settings_notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Switch)findViewById(R.id.settings_notifications_switch)).toggle();
            }
        });

        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (!checked) preferences.edit().putBoolean("notifications",false).apply();
                else preferences.edit().putBoolean("notifications",true).apply();
            }
        });

        // Words each day
        final TextView word_quantity = (TextView)findViewById(R.id.settings_word_quantity);
        final TextView word_quantity_qualifier = (TextView)findViewById(R.id.settings_word_qualifier);
        SeekBar seekBar = (SeekBar) findViewById(R.id.settings_seekBar);

        int wordsEachDay = 4;
        if (preferences.contains("wordsEachDay")) {
            wordsEachDay = (preferences.getInt("wordsEachDay", 0))/ learningMultiplier;
        }
        updateVisualDifficulty(wordsEachDay, word_quantity, word_quantity_qualifier, seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateVisualDifficulty(i, word_quantity, word_quantity_qualifier, null);
                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                        .edit().putInt("wordsEachDay",i * learningMultiplier).apply();
                EventBus.getDefault().post(new LearnQuantityUpdated(i * learningMultiplier));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Learn new words
//        boolean learnNewWords = preferences.getBoolean("learnNewWords", true);
//        final CheckBox learNewWordsCheckbox = findViewById(R.id.preferences_learn_new_words_checkbox);
//        learNewWordsCheckbox.setChecked(learnNewWords);
//
//        findViewById(R.id.preferences_learn_new_words).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                learNewWordsCheckbox.toggle();
//            }
//        });
//
//        learNewWordsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                preferences.edit().putBoolean("learnNewWords",b).apply();
//            }
//        });

//        // Traditional characters
//        boolean traditionalCharacters = preferences.getBoolean("traditionalCharacters", false);
//        final CheckBox traditionalCharactersCheckbox = findViewById(R.id.preferences_traditional_characters_checkbox);
//        traditionalCharactersCheckbox.setChecked(traditionalCharacters);
//
//        findViewById(R.id.preferences_traditional_characters).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                traditionalCharactersCheckbox.toggle();
//            }
//        });
//
//        traditionalCharactersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
//                        .edit().putBoolean("traditionalCharacters",b).apply();
//            }
//        });

        // Open Source
        findViewById(R.id.preferences_open_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, OpenSource.class));
            }
        });

        // Feedback email
        findViewById(R.id.preferences_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                composeEmail("squiel91@gmail.com", "Tiān tiān Feedback", null);
            }
        });
    }

    public void composeEmail(String email, String subject, String bodyText) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (bodyText != null) { intent.putExtra(Intent.EXTRA_TEXT, bodyText);}
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void updateVisualDifficulty(int value, TextView word_quantity, TextView  word_quantity_qualifier, SeekBar seekBar) {
        final String[] difficulty_labels = new String[] {
                "Paused", "A breeze", "Relaxed", "Easy", "Recommended", "Tolerable", "Challenging", "Difficult", "Intense",  "Extreme", "Sensei"
        };
        if (seekBar != null) {
            seekBar.setProgress(value);
        }
        word_quantity.setText(String.valueOf((value)* learningMultiplier));
        word_quantity_qualifier.setText(difficulty_labels[value]);
        word_quantity_qualifier.setTextColor(getResources().getColor(value > 5? R.color.difficult_level : R.color.easy_level));
    }

}
