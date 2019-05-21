package com.example.android.push_chinese;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.android.push_chinese.data.PushDbContract;

import org.json.JSONException;
import org.json.JSONObject;



public class Sentence {
    private Long id;
    private String[] sentence;
    private String raw_sentence;
    private String translation;
    private boolean audio;
    private Uri uri = null;

    public static final String LOG_TAG = Sentence.class.getSimpleName();

    public Sentence (Long id, String sentence, String translation, Boolean audio) {
        this.id = id;
        raw_sentence = sentence;
        this.sentence = raw_sentence.split("·");
        this.translation = translation;
        this.audio = audio != null && audio;
    }

    public static Sentence from_json(JSONObject json) {
        try {
            return new Sentence(
                    json.getLong("id"),
                    json.getString("sentence"),
                    json.optString("translation", null),
                    json.optBoolean("audio", false)
            );
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Could not parse sentence JSON");
            return null;
        }
    }

    public static Sentence from_cursor(Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return new Sentence(
                    cursor.getLong(cursor.getColumnIndex(PushDbContract.Sentences.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(PushDbContract.Sentences.COLUMN_SENTENCE)),
                    cursor.getString(cursor.getColumnIndex(PushDbContract.Sentences.COLUMN_TRANSLATION)),
                    cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_AUDIO)) == 1
            );
        } else {
            Log.w(LOG_TAG, "Could not read sentence from cursor");
            return null;
        }
    }

    public static Sentence from_id(Long id, Context context) {
        ;Cursor cursor =context.getContentResolver().query(
                Uri.parse(PushDbContract.Sentences.CONTENT_URI + "/" + String.valueOf(id)),
                null, null, null, null);
        return Sentence.from_cursor(cursor);
    }

    public Uri persist(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PushDbContract.Sentences.COLUMN_ID, getId());
        contentValues.put(PushDbContract.Sentences.COLUMN_SENTENCE, raw_sentence);
        contentValues.put(PushDbContract.Sentences.COLUMN_TRANSLATION, getTranslation());
        contentValues.put(PushDbContract.Sentences.COLUMN_AUDIO, audio);

        uri = context.getContentResolver().insert(PushDbContract.Sentences.CONTENT_URI, contentValues);
        return uri;
    }

    public Long getId() {
        return id;
    }

    public String[] getSentence() {
        return sentence;
    }

    public String getRawSentence() {
        return raw_sentence.replaceAll( "·", "");
    }

    public String getTranslation() {
        return translation;
    }

    public boolean hasAudio() {
        return audio;
    }

    public Uri uri() {
        return uri;
    }

    @Override
    public String toString() {
        return String.format("[%d|%s|%s|%b]", getId() ,raw_sentence, getTranslation(), hasAudio());
    }
}
