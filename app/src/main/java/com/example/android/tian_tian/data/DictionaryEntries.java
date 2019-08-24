package com.example.android.tian_tian.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.example.android.tian_tian.entities.Word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DictionaryEntries {
    Context context;

    public DictionaryEntries(Context context) {
        this.context = context;
    }

    private SQLiteDatabase getPrepopulatedDb() {
        MyDatabaseHelper myDatabase = new MyDatabaseHelper(context);
        SQLiteDatabase databaseReader = myDatabase.getReadableDatabase();
        return databaseReader;
    }

    public ArrayList<String> getAutocomplete(String querry) {
//        Cursor cursor = getPrepopulatedDb().rawQuery("SELECT _ID FROM dictionary WHERE ", null);
//        Toast.makeText(this, "Rows: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
        return null;
    }

    private String[] jsonArrayToStringArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) return new String[0];
        String[] strings;
        strings = new String[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            strings[i] = jsonArray.getString(i);
        }
        return strings;
    }

    private int[] jsonArrayToIntArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) return new int[0];
        int[] ints;
        ints = new int[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            ints[i] = jsonArray.getInt(i);
        }
        return ints;
    }

    public Pair<Word[], String[]> getWord(String query) {
        Cursor cursor = getPrepopulatedDb().query("dictionary", null,
                PushDbContract.Vocabulary.COLUMN_ID+ " = ?",
                new String[] { query },
                null, null, null);
        if (cursor.getCount() == 0) return new Pair<>(null, new String[0]);
        try {
            cursor.moveToFirst();
            final int data_index = cursor.getColumnIndexOrThrow("data");
            String json_string = cursor.getString(data_index);
            JSONObject json_root = new JSONObject(json_string);

            JSONArray json_words = json_root.getJSONArray("words");
            String[] examples = jsonArrayToStringArray(json_root.optJSONArray("examples"));
            Word[] possibleWords = new Word[json_words.length()];
            for (int i = 0; i < json_words.length(); i++) {
                JSONObject json_word = json_words.getJSONObject(i);
                Word word = new Word(
                        null,
                        query,
                        json_word.getString("p"),
                        jsonArrayToStringArray(json_word.getJSONArray("t")),
                        jsonArrayToStringArray(json_word.optJSONArray("m")),
                        null,
                        null,
                        false,
                        false);
                possibleWords[i] = word;
            }
            return new Pair<>(possibleWords, examples.length > 0? examples : new String[0]);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair<>(null, new String[0]);
        }
    }
}
