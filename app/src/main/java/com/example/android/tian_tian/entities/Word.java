package com.example.android.tian_tian.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.utilities.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import static com.example.android.tian_tian.utilities.Helper.withoutNumbersAndSpaces;

public class Word implements Serializable {
    private Long id;
    private String headWord;
    private String pronunciation;
    private String[] translations;
    private String[] measures;
    private Integer level;
    private String[] examples;
    private Boolean image;
    private Boolean audio;

    public void setHeadWord(String headWord) {
        this.headWord = headWord;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public void setTranslations(String[] translations) {
        this.translations = translations;
    }

    public void setMeasures(String[] measures) {
        this.measures = measures;
    }

    public void setExamples(String[] examples) {
        this.examples = examples;
    }

    public void setHasImage(Boolean image) {
        this.image = image;
    }

    private int learningStage;

    private Uri word_uri = null;

    public final static int EASY = 0;
    public final static int NORMAL = 1;
    public final static int HARD = 2;

    final private static String sep = "·";

    private int scheduledTo;

    // For assigning to LEARNING_STAGE
    public final static int TO_PRESENT = 0;
    public final static int SHOW_HEAD_WORD = 1;
    public final static int SHOW_PRONUNCIATION = 2;
    public final static int SHOW_TRANSLATION = 3;
    public final static int SHOW_IMAGE = 4;
    public final static int LEARNED = 5;

    public Word (Long id, String headWord, String pronunciation, String[] translations, String[] measures, String[] examples,  Integer level, Boolean image, Boolean audio) {
        this.id = id;
        this.headWord = headWord;
        this.pronunciation = pronunciation;
        this.translations = translations;
        this.measures = measures;
        this.examples = examples;
        if (level != null) {
            this.level = level;
        } else {
            level = null;
        }
        this.image = (image != null) && image;
        this.audio =  (audio != null) && audio;
    }

    public static Word from_cursor(Cursor cursor) {
        String translationsString =  cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_TRANSLATION));
        String[] translations;
        if ((translationsString != null) && (!translationsString.trim().isEmpty())) {
            translations = translationsString.split(sep);
        } else {
            translations = null;
        }

        String examplesString = cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_EXAMPLES));

        String[] examples;
        if ((examplesString != null) && (!examplesString.trim().isEmpty())) {
            examples = examplesString.split(sep);
        } else {
            examples = new String[0];
        }

        String measuresString = cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_MEASURES));
        String[] measures;
        if ((measuresString != null) && (!measuresString.trim().isEmpty())) {
            measures = measuresString.split(sep);
        } else {
            measures = new String[0];
        }

        Word newWord = new Word(
                cursor.getLong(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_HEAD_WORD)),
                cursor.getString(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION)),
                translations,
                measures,
                examples,
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_LEVEL)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_IMAGE)) == 1,
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_AUDIO))  == 1
        );

        newWord.scheduledTo = cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_SCHEDULE_FOR));
        newWord.learningStage = cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE));

        return newWord;
    }

    public static Word from_json(JSONObject json) throws JSONException {

        JSONArray jsonExamples = json.getJSONArray(PushDbContract.Vocabulary.COLUMN_EXAMPLES);
        String[] examples;
        if (jsonExamples != null) {
            examples = new String[jsonExamples.length()];
            for(int i = 0; i < jsonExamples.length(); i++){
                examples[i] = jsonExamples.getString(i);
            }
        } else {
            examples = new String[0];
        }

        JSONArray jsonTranslations = json.getJSONArray("translations");
        String[] translations;
        if (jsonTranslations != null) {
            translations = new String[jsonTranslations.length()];
            for(int i = 0; i < jsonTranslations.length(); i++){
                translations[i] = jsonTranslations.getString(i);
            }
        } else {
            translations = new String[0];
        }

        JSONArray jsonMeasures = json.optJSONArray("measure");
        String[] measures;
        if (jsonMeasures != null) {
            measures = new String[jsonMeasures.length()];
            for(int i = 0; i < jsonMeasures.length(); i++){
                measures [i] = jsonMeasures.getString(i);
            }
        } else {
            measures = new String[0];
        }

        String pronunciation = json.optString(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION);
        String numericPinyin = null;
        if (pronunciation != null) {
            numericPinyin = withoutNumbersAndSpaces(pronunciation);
        }

        Word newWord = new Word(
                json.getLong("id"),
                json.getString(PushDbContract.Vocabulary.COLUMN_HEAD_WORD),
                pronunciation,
                translations,
                measures,
                examples,
                json.optInt(PushDbContract.Vocabulary.COLUMN_LEVEL),
                json.optBoolean("image", false),
                json.optBoolean("audio", false)
        );
        return newWord;
    }

    public static Word from_id(Context context, Long id) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse(PushDbContract.Vocabulary.CONTENT_URI + "/" + String.valueOf(id)),
                null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return Word.from_cursor(cursor);
        }  else {
            return null;
        }
    }

    public Long getId() {
        return id;
    }

    public String getHeadWord() {
        return headWord;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String[] getTranslations() {
        return translations;
    }

    public int getLevel() {
        return level;
    }

    public void setStage(int newStage) {
        learningStage = newStage;
    }

    public int moveToNextStage() {
        learningStage = nextStage();
        return learningStage;
    }


    public int nextStage() {
        int nextStage = getStage() + 1;
        while (nextStage < Word.LEARNED) {
            if ((nextStage == Word.TO_PRESENT) || (nextStage == Word.SHOW_HEAD_WORD)) break;
            if (nextStage == Word.SHOW_PRONUNCIATION) {
                if (hasPronunciation()) break;
            }
            if (nextStage == Word.SHOW_TRANSLATION) {
                if (hasTranslations()) break;
            }
            if (nextStage == Word.SHOW_IMAGE) {
                boolean hasImage = hasImage();
                if (hasImage) break;
            }
            nextStage++;
        }
        return nextStage;
    }


    public ArrayList<Integer> getPossibleStages() {
        ArrayList<Integer> stageList = new ArrayList();
        stageList.add(Word.SHOW_HEAD_WORD);
        if (hasPronunciation()) stageList.add(Word.SHOW_PRONUNCIATION);
        if (hasTranslations()) stageList.add(Word.SHOW_TRANSLATION);
        if (hasImage()) stageList.add(Word.SHOW_TRANSLATION);
        return stageList;
    }

    public int getStage() {
        return learningStage;
    }

    public boolean hasTranslations() {
        return translations.length > 0;
    }

    public boolean hasPronunciation() {
        return pronunciation != null;
    }

    public boolean hasImage() {
        return image;
    }

    public boolean hasAudio() {
        return audio;
    }

    public void setHasAudio(Boolean audio) {
        this.audio = audio;
    }

    public String[] getExamples() {
        return getExamples(false);
    }

    private String maskSentence(String sentence, String mask) {
        String mask_symbols = mask;
        for (int head_word_index = 1; head_word_index < getHeadWord().length(); head_word_index++) {
            mask_symbols += " __";
        }
        return sentence.replaceAll(this.headWord,
                this.headWord.replaceAll(getHeadWord(), mask_symbols));
    }

    public String[] getExamples(boolean masked) {
        String[] examplesList = new String[examples.length];
        for (int i = 0; i < examples.length; i++) {
            examplesList[i] = masked? maskSentence(examples[i], "__") : examples[i];
        }
        return examplesList;
    }

    public String[] getMeasures() {
        return measures;
    }

    private ContentValues getVariableContent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PushDbContract.Vocabulary.COLUMN_SCHEDULE_FOR, this.scheduledTo);
        contentValues.put(PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE, learningStage);
        return contentValues;
    }

    public Uri store(Context context) {
        ContentValues contentValues = getVariableContent();
        contentValues.put(PushDbContract.Vocabulary.COLUMN_ID, this.getId());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_HEAD_WORD,this.getHeadWord());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION, this.getPronunciation());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION_SEARCHABLE, this.getPronunciation() == null?
                null : Helper.withoutNumbersAndSpaces(this.getPronunciation()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_TRANSLATION, Helper.join(sep, this.getTranslations()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_MEASURES,Helper.join(sep, this.getMeasures()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_EXAMPLES, Helper.join(sep, this.getExamples()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_LEVEL,this.getLevel());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_AUDIO, hasAudio());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_IMAGE, hasImage());

        // description is column in items table, item.description has value for description
        word_uri =context.getContentResolver().insert(PushDbContract.Vocabulary.CONTENT_URI, contentValues);
        return word_uri;
    }

    // just updates the variable content
    public int persist(Context context) {
        ContentValues contentValues = getVariableContent();
        int affectedRows = context.getContentResolver().update(
                Uri.parse(PushDbContract.Vocabulary.CONTENT_URI + "/" + String.valueOf(this.getId())),
                contentValues,
                null,
                null
        );
        return affectedRows;
    }

    // updates the static information
    public int update(Context context) {
        ContentValues contentValues = getVariableContent();
        contentValues.put(PushDbContract.Vocabulary.COLUMN_HEAD_WORD,this.getHeadWord());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION, this.getPronunciation());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION_SEARCHABLE, Helper.withoutNumbersAndSpaces(this.getPronunciation()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_TRANSLATION, Helper.join(sep, this.getTranslations()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_MEASURES,Helper.join(sep, this.getMeasures()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_EXAMPLES, Helper.join(sep, this.getExamples()));
        contentValues.put(PushDbContract.Vocabulary.COLUMN_LEVEL,this.getLevel());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_AUDIO, hasAudio());
        contentValues.put(PushDbContract.Vocabulary.COLUMN_IMAGE, hasImage());
        int affectedRows = context.getContentResolver().update(
                Uri.parse(PushDbContract.Vocabulary.CONTENT_URI + "/" + String.valueOf(this.getId())),
                contentValues,
                null,
                null
        );
        return affectedRows;

    }

    public static String getAudioURI(Long wordId) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_" + wordId + ".3gp";
    }

    public String getImageURI() {
        return getImageURI(getId());
    }

    public static String getImageURI(Long wordId) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +   "/" + "image_" + wordId + ".png";
    }


    public String getAudioURI() {
        return getAudioURI(getId());
    }

    public void delete(Context context) {
        word_uri = Uri.parse(PushDbContract.Vocabulary.CONTENT_URI + "/" + String.valueOf(this.getId()));
        int word_deleted = context.getContentResolver().delete(word_uri, null, null);
        if (word_deleted == 1) {
            Toast.makeText(context, "Word deleted", Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(context, "Could not delete it", Toast.LENGTH_SHORT);
        }
    }

    public void setScheduledTo(int scheduledTo) {
        this.scheduledTo = scheduledTo;
    }
}
