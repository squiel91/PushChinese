package com.example.android.push_chinese.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.push_chinese.others.NotificationsGuide;
import com.example.android.push_chinese.R;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    boolean justConfirmedDisable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Push Chinese Notifications
        boolean learnWordsIn = preferences.contains("notifications");
        Log.w("Contains", String.valueOf(learnWordsIn));
        boolean notifications = preferences.getBoolean("notifications", true);

        final Switch notificationsSwitch= (Switch)findViewById(R.id.settings_notifications_switch);
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
                if (!checked) {
                    if (!justConfirmedDisable) {
                        final CompoundButton notificationsSwitch = compoundButton;
                        compoundButton.setChecked(true);
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Disable lock screen notifications?")
                                .setMessage("Are you sure you want to disable an important part of the app?\n\nYou can limit the time notifications show in the option below instead.")
                                .setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        justConfirmedDisable = true;
                                        notificationsSwitch.setChecked(false);
                                        preferences.edit().putBoolean("notifications",false).apply();
                                    }})
                                .setNegativeButton("Cancel", null).show();
                    } else {
                        justConfirmedDisable = false;
                    }
                } else {
                    preferences.edit().putBoolean("notifications",true).apply();
                }
            }
        });

        // Words each day
        final TextView word_quantity = (TextView)findViewById(R.id.settings_word_quantity);
        final TextView word_quantity_qualifier = (TextView)findViewById(R.id.settings_word_qualifier);
        int wordsEachDay = 3;
        if (preferences.contains("wordsEachDay")) {
            wordsEachDay = (preferences.getInt("wordsEachDay", 0))/5 - 1;
        }
        SeekBar seekBar = (SeekBar)findViewById(R.id.settings_seekBar);
        updateVisualDifficulty(wordsEachDay, word_quantity, word_quantity_qualifier, seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateVisualDifficulty(i, word_quantity, word_quantity_qualifier, null);
                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                        .edit().putInt("wordsEachDay",(i + 1)*5).apply();
                EventBus.getDefault().post(new Integer((i + 1)*5));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Learn new words

        boolean learnNewWords = preferences.getBoolean("learnNewWords", true);
        final CheckBox learNewWordsCheckbox = findViewById(R.id.preferences_learn_new_words_checkbox);
        learNewWordsCheckbox.setChecked(learnNewWords);

        findViewById(R.id.preferences_learn_new_words).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                learNewWordsCheckbox.toggle();
            }
        });

        learNewWordsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean("learnNewWords",b).apply();
            }
        });

        // Traditional characters
        boolean traditionalCharacters = preferences.getBoolean("traditionalCharacters", false);
        final CheckBox traditionalCharactersCheckbox = findViewById(R.id.preferences_traditional_characters_checkbox);
        traditionalCharactersCheckbox.setChecked(traditionalCharacters);

        findViewById(R.id.preferences_traditional_characters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                traditionalCharactersCheckbox.toggle();
            }
        });

        traditionalCharactersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                        .edit().putBoolean("traditionalCharacters",b).apply();
            }
        });

        // Show exercises
        boolean showExercises = preferences.getBoolean("showExercises", true);
        final CheckBox showExerciesesCheckbox = findViewById(R.id.preferences_show_exercises_checkbox);
        showExerciesesCheckbox.setChecked(showExercises);

        findViewById(R.id.preferences_show_exercises).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExerciesesCheckbox.toggle();
            }
        });

        showExerciesesCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                        .edit().putBoolean("showExercises",b).apply();
            }
        });

        // Open Source
        findViewById(R.id.preferences_open_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, OpenSource.class));
            }
        });

        // Notifications Guide
        findViewById(R.id.preferences_notifications_guide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, NotificationsGuide.class));
            }
        });

        // Feedback email
        findViewById(R.id.preferences_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                composeEmail("ezequiel@pushlanguages.com", "Push Chinese Feedback", null);
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
                "(A breeze)", "(Relaxed)", "(Easy)", "(Recommended)", "(Tolerable)", "(Challenging)", "(Difficult)", "(Intense)",  "(Extreme)", "(Master)"
        };
        final int[] difficulty_colors = new int[] {
                R.color.difficulty_0, R.color.difficulty_1,  R.color.difficulty_2, R.color.difficulty_3,
                R.color.difficulty_4, R.color.difficulty_5, R.color.difficulty_6, R.color.difficulty_7,
                R.color.difficulty_8, R.color.difficulty_9,
        };
        if (seekBar != null) {
            seekBar.setProgress(value);
        }
        word_quantity.setText(String.valueOf((value + 1)*5));
        word_quantity_qualifier.setText(difficulty_labels[value]);
        word_quantity_qualifier.setTextColor(getResources().getColor(difficulty_colors[value]));
    }

}
