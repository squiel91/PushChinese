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

import static com.example.android.tian_tian.utilities.Helper.listFromStringCursor;
import static com.example.android.tian_tian.utilities.Helper.withoutNumbersAndSpaces;

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

    public Cursor getCursor(String querry) {
        Cursor cursor = getPrepopulatedDb().query("dictionary", null,
                "INSTR(_ID, ?) > 0 OR INSTR(translations, ?) > 0 OR INSTR(pronunciation_searchable, ?) > 0",
                new String[] {
                        querry.toString(),
                        querry.toString(),
                        withoutNumbersAndSpaces(querry.toString())
                }, null, null,
                "length(_ID) ASC");
        return cursor;
    }

    public Word[] getWord(String query) {
        Cursor cursor = getPrepopulatedDb().query("dictionary", null,
                PushDbContract.Vocabulary.COLUMN_ID+ " = ?",
                new String[] { query },
                null, null, null);
        if (cursor.getCount() == 0) return null;
        final String sep = "·";
        Word[] possibleWords = new Word[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            Word newWord = new Word(
                    (long) 0,
                    cursor.getString(cursor.getColumnIndexOrThrow("_ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("pronunciation")),
                    listFromStringCursor(cursor, "translations", sep),
                    listFromStringCursor(cursor, "measures", sep),
                    listFromStringCursor(cursor, "examples", sep),
                    0,
                    false,
                    false
            );
            possibleWords[i] = newWord;
        }
        return possibleWords;
    }
}
