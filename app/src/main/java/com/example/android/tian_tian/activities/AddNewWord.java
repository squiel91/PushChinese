package com.example.android.tian_tian.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.tian_tian.BusEvents.StatsUpdate;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.data.DictionaryEntries;
import com.example.android.tian_tian.entities.Word;
import com.example.android.tian_tian.utilities.Helper;
import com.example.android.tian_tian.utilities.VoiceRecorder;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddNewWord  extends AppCompatActivity {

    Long wordId;
    Word wordToEdit = null;

    // image variables
    ImageView imageView;
    View imageContainerPanel;
    View addImageContainerPanel;
    Bitmap imageBitmap = null;
    boolean hasImage = false;

    String tempAudioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.3gp";

    EditText hanziEditText;
    EditText pronunciationEditText;
    VoiceRecorder voiceRecorder;
    LinearLayout possiblePronunciations;
    TextView lastPronunciationActive = null;
    LinearLayout translationContainer;
    LinearLayout measuresContainer;
    LinearLayout examplesContainer;

    void addRow(final ViewGroup container, boolean withFocus) {
        addRow(container, null, withFocus);
    }
    void addRow(final ViewGroup container, String content, boolean withFocus) {
        final LinearLayout row_container = new LinearLayout(this);
        EditText editText = new EditText(this);
        editText.setBackgroundTintList(this.getResources().getColorStateList(R.color.primary_color));
        if (content != null) {
            editText.setText(content);
        }
        MaterialIconView removeButton = new MaterialIconView(this);
        removeButton.setIcon(MaterialDrawableBuilder.IconValue.CLOSE);
        removeButton.setToActionbarSize();
        removeButton.setSizeDp(30);
        removeButton.setPadding(Helper.dpiToPixels(this, 10),
                Helper.dpiToPixels(this,10),
                Helper.dpiToPixels(this, 10),
                Helper.dpiToPixels(this, 10));
        removeButton.setColorResource(R.color.primary_color);
        row_container.addView(editText);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        editText.setLayoutParams(params);
        editText.setTag("input");
        row_container.addView(removeButton);
        container.addView(row_container);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.removeView(row_container);
            }
        });
        if (withFocus) editText.requestFocus();
    }

    void removeAllRows(final ViewGroup container) {
        container.removeAllViews();
    }

    private Long getId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int addedWords = preferences.getInt("addedWords", 0) + 1;
        preferences.edit().putInt("addedWords", addedWords).apply();
        return new Long(addedWords);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickImageIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPhoto.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Bitmap tempImageBitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                tempImageBitmap = (Bitmap) extras.get("data");
            }
            if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
                Uri selectedImage = intent.getData();
                try {
                    tempImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            imageBitmap = resizeBitmap(tempImageBitmap);
            processBitmap();
        }
    }

    private void processBitmap() {
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
            imageContainerPanel.setVisibility(View.VISIBLE);
            addImageContainerPanel.setVisibility(View.GONE);
            hasImage = true;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_word);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra("Word")) {
            wordToEdit = (Word) getIntent().getSerializableExtra("Word");
        }

        final DictionaryEntries dictionaryEntries = new DictionaryEntries(this);

        imageContainerPanel = findViewById(R.id.image_added_panel);;
        addImageContainerPanel = findViewById(R.id.add_image_panel);;
        imageView = findViewById(R.id.added_image_panel);
        hanziEditText = findViewById(R.id.characters_edit_text);
        pronunciationEditText = findViewById(R.id.pronunciation_edit_text);
        voiceRecorder = findViewById(R.id.voice_recorder_panel);
        possiblePronunciations = findViewById(R.id.possible_pronunciations);
        translationContainer = findViewById(R.id.translation_container);
        measuresContainer = findViewById(R.id.measures_container);
        examplesContainer = findViewById(R.id.examples_container);

        if (wordToEdit != null) {
            wordId = wordToEdit.getId();
            hanziEditText.setText(wordToEdit.getHeadWord());
            preloadWord(wordToEdit);
        } else {
            wordId = getId();
            addRow(translationContainer, false);
            String s = Word.getAudioURI(wordId);
            voiceRecorder.setAudioName(tempAudioFile, false);
        }

        findViewById(R.id.add_translation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(translationContainer, true);
            }
        });

        findViewById(R.id.add_measure_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(measuresContainer, true);
            }
        });
        findViewById(R.id.add_example_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(examplesContainer, true);
            }
        });

        findViewById(R.id.add_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        findViewById(R.id.pick_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchPickImageIntent();
            }
        });

        findViewById(R.id.remove_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage();
            }
        });

        hanziEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                Word[] possibleWords =  dictionaryEntries.getWord(query);
                possiblePronunciations.removeAllViews();
                lastPronunciationActive = null;
                if ((possibleWords != null) && (possibleWords.length > 0)) {
                    for (int wi= 0; wi < possibleWords.length; wi++) {
                        TextView button = createButton(possibleWords[wi].getPronunciation());
                        button.setTag(possibleWords[wi]);
                        possiblePronunciations.addView(button);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Word word = (Word) view.getTag();
                                preloadWord(word);
                                setActiveState((TextView) view);
                            }
                        });
                        if (wi == 0) {
                            preloadWord(possibleWords[wi]);
                            setActiveState(button);
                        }
                    }
                }
            }
        });
    }

    private void preloadWord(Word word) {
        if (word.hasImage()) {
            imageBitmap = BitmapFactory.decodeFile(Word.getImageURI(wordId));
            processBitmap();
        }
        pronunciationEditText.setText(word.getPronunciation());
        if (word.hasAudio()) {
            try {
                copy(new File(word.getAudioURI()), new File(tempAudioFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        voiceRecorder.setAudioName(tempAudioFile, word.hasAudio());
        removeAllRows(translationContainer);
        for (String translation : word.getTranslations()) {
            addRow(translationContainer, translation, false);
        }
        removeAllRows(measuresContainer);
        if (word.getMeasures() != null) {
            for (String measure : word.getMeasures()) {
                addRow(measuresContainer, measure, false);
            }
        }
        removeAllRows(examplesContainer);
        if (word.getExamples() != null) {
            for (String example : word.getExamples()) {
                addRow(examplesContainer, example, false);
            }
        }
    }

    private void removeImage() {
        addImageContainerPanel.setVisibility(View.VISIBLE);
        imageContainerPanel.setVisibility(View.GONE);
        hasImage = false;
    }

    private TextView createButton(String text) {
        TextView button = new TextView(this);
        button.setText(Helper.toPinyin(text));
        button.setPadding(Helper.dpiToPixels(this, 4),
                Helper.dpiToPixels(this, 4),
                Helper.dpiToPixels(this, 4),
                Helper.dpiToPixels(this, 4));

        setInactiveState(button);
        return button;
    }

    private void setActiveState(TextView button) {
        if (lastPronunciationActive != null) setInactiveState(lastPronunciationActive);
        lastPronunciationActive = button;
        button.setBackgroundColor(getResources().getColor(R.color.primary_color));
        button.setTextColor(getResources().getColor(R.color.white));
    }

    private void setInactiveState(TextView button) {
        button.setBackgroundColor(getResources().getColor(R.color.white));
        button.setTextColor(getResources().getColor(R.color.primary_color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_new_word_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.discard) {
            wordToEdit = null;
            removeImage();
            lastPronunciationActive = null;
            hanziEditText.setText("");
            pronunciationEditText.setText("");
            voiceRecorder.deleteAudio();
            possiblePronunciations.removeAllViews();
            removeAllRows(translationContainer);
            addRow(translationContainer, false);
            removeAllRows(measuresContainer);
            removeAllRows(examplesContainer);
        }
        if (id == R.id.save) {

            if (validInput()) saveWord();
        }
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validInput() {
        String pronunciation = pronunciationEditText.getText().toString();
        String characters = ((EditText) findViewById(R.id.characters_edit_text)).getText().toString();

        if (characters.isEmpty()) {
            callAttention(hanziEditText, "Write some chinese characters!");
            return false;
        }

        if (pronunciation.isEmpty()) {
            callAttention(pronunciationEditText, "Write some pronunciation");
            return false;
        }

        if (getInputs(translationContainer).length == 0) {
            callAttention(translationContainer, "Add at least one translation");
            return false;
        }
        return true;
    }

    private void callAttention(View input, String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (input != null) YoYo.with(Techniques.Shake).playOn(input);
    }
    private void saveWord() {
        String pronunciation = pronunciationEditText.getText().toString();
        String characters = ((EditText) findViewById(R.id.characters_edit_text)).getText().toString();

        if (!saveBitmap(imageBitmap, Word.getImageURI(wordId))) return;

        if (voiceRecorder.hasAudio()) {
            try {
                copy(new File(tempAudioFile), new File(Word.getAudioURI(wordId)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Word word;
        if (wordToEdit == null) {
            word = new Word(
                    wordId,
                    characters,
                    pronunciation.isEmpty()? null: pronunciation,
                    getInputs(translationContainer),
                    getInputs(measuresContainer),
                    getInputs(examplesContainer),
                    0,
                    hasImage,
                    voiceRecorder.hasAudio()
            );
            word.store(this);
            EventBus.getDefault().post(new Integer(0));
            EventBus.getDefault().post(new StatsUpdate());
        } else {
            word = wordToEdit;
            word.setHeadWord(characters);
            word.setPronunciation(pronunciation);
            word.setHasAudio(voiceRecorder.hasAudio());
            word.setTranslations(getInputs(translationContainer));
            word.setMeasures(getInputs(measuresContainer));
            word.setExamples(getInputs(examplesContainer));
            word.setHasImage(hasImage);
            word.update(this);
            EventBus.getDefault().post(new Float(0));
        }

        Toast.makeText(this, "Word saved", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    private String[] getInputs(ViewGroup container) {
        ArrayList<String> inputs = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            String row = ((EditText) container.getChildAt(i).findViewWithTag("input")).getText().toString();
            if (!row.isEmpty()) inputs.add(row);
        }
        return inputs.toArray(new String[0]);

    }

    private boolean saveBitmap(Bitmap bitmap, String fileName) {
        if (bitmap != null) {
            if (!hasPermission()) {
                requestPermissionAndContinue();
                return false;
            }
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(fileName, false);
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            int  newHeight;
            int  newWidth;
            if (height > width) {
                newHeight = Math.min(600, height);
                float compressRatio = (float) newHeight / height;
                newWidth = (int) (width * compressRatio);
            } else {
                newWidth = Math.min(1000, width);
                float compressRatio = (float) newWidth / width;
                newHeight = (int) (height * compressRatio);
            }
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

    }

    private static final int PERMISSION_REQUEST_CODE = 200;

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("External Storage Permission");
                alertBuilder.setMessage("Storage permission are necessary to read and write the vocabulary's images.");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(AddNewWord.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(AddNewWord.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (permissions.length > 0 && grantResults.length > 0) {

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        acceptAllPermissions();
                        return;
                    }
                }
                /* continue */ saveWord();
            } else {
                acceptAllPermissions();
            }
        }
    }

    private void acceptAllPermissions() {
        Toast.makeText(this, "You need to accept all permissions.", Toast.LENGTH_SHORT).show();
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
