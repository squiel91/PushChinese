package com.example.android.tian_tian.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.tian_tian.BusEvents.LearnQuantityUpdated;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.entities.Word;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {

    String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tiantian/tiantian.backup.json";

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
        // Export
        findViewById(R.id.preferences_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Make a backup")
                        .setMessage("A database backup file will be created in the Tiantian folder.\n\n" +
                                "The file does not include the media (images and audio) so save the complete Tiantian folder.\n\n" +
                                "This process may take a few seconds to complete.")
                        .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (backupData())
                                    Toast.makeText(getApplicationContext(), "Successfully created the backup", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Failed at making the backup", Toast.LENGTH_LONG).show();
                            }})
                        .setNegativeButton("Later", null).show()
                        .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor( getResources().getColor(R.color.primary_color));
            }
        });

        // Import
        findViewById(R.id.preferences_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Restore from backup")
                        .setMessage("Procedure:\n\n" +
                                "1) If not already there, the a folder called 'Tiantian' in the root folder of your phone\n" +
                                "2) Copy the tiantian.backup.json file previously created by the app to the Tiantian folder\n" +
                                "3) Copy the media (images and audios) to the Tiantian folder\n" +
                                "\nNotes:\n" +
                                "- This process will not delete any stored word\n" +
                                "- It may take a few seconds to complete\n" +
                                "\nPress start when you feel ready.\n")
                        .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int addedWords = restoreData();
                                Toast.makeText(getApplicationContext(), "Added " + addedWords + " words from backup", Toast.LENGTH_LONG).show();
                            }})
                        .setNegativeButton("Later", null).show()
                        .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor( getResources().getColor(R.color.primary_color));
            }
        });

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

    private boolean backupData() {
        Cursor cursor = getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, null, null, null, null);
        File backupFile = new File(backupPath);
        JSONArray allWordsJSON = new JSONArray();
        while (cursor.moveToNext()) {
            Word currentWord = Word.from_cursor(cursor);
            JSONObject wordJSON = currentWord.to_json(true);
            allWordsJSON.put(wordJSON);
        }
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(backupFile));
            output.write(allWordsJSON.toString());
            output.flush();
            output.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private int restoreData() {
        int addedWords = 0;
        File backupFile = new File(backupPath);
        try {
            JSONArray wordsBackupJSON = new JSONArray(getFileContents(backupFile));
            long largestId = 0;
            for (int i = 0; i < wordsBackupJSON.length(); i++) {
                try {
                    JSONObject wordJSON = wordsBackupJSON.getJSONObject(i);
                    Word word = Word.from_json(wordJSON);
                    word.store(this);
                    word.persist(this);
                    largestId = Math.max(largestId, word.getId());
                    addedWords += 1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("addedWords", (int) largestId).apply();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return addedWords;
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

    public static String getFileContents(final File file) throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;

        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }

        reader.close();
        inputStream.close();

        return stringBuilder.toString();
    }
}
