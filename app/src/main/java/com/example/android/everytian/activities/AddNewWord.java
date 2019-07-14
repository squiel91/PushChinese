package com.example.android.everytian.activities;

import android.animation.Animator;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.easing.linear.Linear;
import com.example.android.everytian.R;
import com.example.android.everytian.entities.Sentence;
import com.example.android.everytian.entities.Word;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddNewWord  extends AppCompatActivity {

    LinearLayout translationContainer;
    LinearLayout measuresContainer;
    LinearLayout examplesContainer;
    ImageView imageView;
    Long wordId;

    void addRow(final ViewGroup container) {
        final LinearLayout row_container = new LinearLayout(this);
        EditText editText = new EditText(this);
        Button removeButton = new Button(this);
        removeButton.setText("-");
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
    }

    private Long getId() {
        Long baseId = new Long(7000);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int addedWords = preferences.getInt("addedWords", 0) + 1;
        preferences.edit().putInt("addedWords", addedWords).apply();
        return baseId + addedWords;
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
        String fileName = "image_" + wordId + ".png";
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(imageBitmap);
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = intent.getData();
            try {
                imageView.setVisibility(View.VISIBLE);
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                saveBitmap(imageBitmap, fileName);
                imageView.setImageBitmap(imageBitmap);
//                imageView.setImageURI(selectedImage);
//                Bitmap myBitmap = BitmapFactory.decodeFile(destinationFileName);
//                imageView.setImageBitmap(myBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_word);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wordId = getId();
        translationContainer = findViewById(R.id.translation_container);
        measuresContainer = findViewById(R.id.measures_container);
        examplesContainer = findViewById(R.id.examples_container);
        findViewById(R.id.add_translation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(translationContainer);
            }
        });
        findViewById(R.id.add_measure_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(measuresContainer);
            }
        });
        findViewById(R.id.add_example_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow(examplesContainer);
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

        imageView = findViewById(R.id.main_image);
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
            Toast.makeText(this, "Word Discarded", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.save) {
            String pronunciation = ((EditText) findViewById(R.id.pronunciation_edit_text)).getText().toString();
            String characters = ((EditText) findViewById(R.id.characters_edit_text)).getText().toString();

            if (pronunciation == null || pronunciation == "") {
                Toast.makeText(this, "Pronunciation can no tbe empty", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (characters == null || characters == "") {
                Toast.makeText(this, "Characters can no tbe empty", Toast.LENGTH_SHORT).show();
                return true;
            }

            String[] examples = getInputs(examplesContainer);
            Long[] examplesIds = new Long[examples.length];
            for (int i = 0; i < examples.length; i ++) {
                Long sentenceId = wordId * 100 + i;
                Sentence sentence = new Sentence(sentenceId, examples[i], null, false);
                sentence.persist(this);
                examplesIds[i] = sentenceId;
            }

            Word word = new Word(
                    wordId,
                    characters,
                    pronunciation,
                    getInputs(translationContainer),
                    getInputs(measuresContainer),
                    examplesIds,
                    0,
                    false,
                    false
            );
            word.store(this);
            Toast.makeText(this, "Word Saved", Toast.LENGTH_SHORT).show();
        }
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] getInputs(ViewGroup container) {
        ArrayList<String> inputs = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            String row = ((EditText) container.getChildAt(i).findViewWithTag("input")).getText().toString();
            if (row != null && row != "") inputs.add(row);
        }
        return inputs.toArray(new String[0]);

    }

    void saveBitmap(Bitmap bitmap, String fileName) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                 File.separatorChar;
        File file = new File(filePath, fileName);
        FileOutputStream fOut = null;
        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                try {
                    fOut = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 200;
    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission_necessary");
                alertBuilder.setMessage("Storage permission is necessary to write event");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(AddNewWord.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(AddNewWord.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openActivity();
                } else {
                    finish();
                }

            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openActivity() {
        Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        //add your further process after giving permission or to download images from remote server.
    }
}
