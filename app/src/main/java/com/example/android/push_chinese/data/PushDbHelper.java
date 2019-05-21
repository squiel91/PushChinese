package com.example.android.push_chinese.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.push_chinese.data.PushDbContract.Vocabulary;
import com.example.android.push_chinese.data.PushDbContract.Sentences;



public class PushDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PushChineseDB";

    private static String SQL_CREATE_VOCABULARY_STATEMENT =
            "CREATE TABLE  " + Vocabulary.TABLE_NAME + " (" +
                    Vocabulary.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Vocabulary.COLUMN_HEAD_WORD + " TEXT UNIQUE NOT NULL, " +
                    Vocabulary.COLUMN_PRONUNCIATION + " TEXT, " +
                    Vocabulary.COLUMN_EXAMPLES + " TEXT, " +
                    Vocabulary.COLUMN_MEASURES+ " TEXT, " +
                    Vocabulary.COLUMN_LEVEL + " INTEGER DEFAULT 0, " +
                    Vocabulary.COLUMN_AUDIO + " INTEGER DEFAULT 0, " +
                    Vocabulary.COLUMN_IMAGE + " INTEGER DEFAULT 0, " +
                    Vocabulary.COLUMN_TRANSLATION + " TEXT NOT NULL, " +
                    Vocabulary.COLUMN_DIFFICULTY + " INTEGER DEFAULT 0, " +
                    Vocabulary.COLUMN_BURIED + " INTEGER DEFAULT 0" +
            ");";

    private static String SQL_CREATE_SENTENCES_STATEMENT =
            "CREATE TABLE  " + PushDbContract.Sentences.TABLE_NAME + " (" +
                    Sentences.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    Sentences.COLUMN_SENTENCE + " TEXT NOT NULL, " +
                    Sentences.COLUMN_TRANSLATION+ " TEXT, " +
                    Sentences.COLUMN_AUDIO + " INTEGER DEFAULT 0 " +
                    ");";

    private static String SQL_DELETE_VOCABULARY_STATEMENT = "DROP TABLE IF EXISTS vocabulary;";
    private static String SQL_DELETE_SENTENCES_STATEMENT = "DROP TABLE IF EXISTS sentences;";

    public PushDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SENTENCES_STATEMENT );
        db.execSQL(SQL_CREATE_VOCABULARY_STATEMENT );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_SENTENCES_STATEMENT);
        db.execSQL(SQL_DELETE_VOCABULARY_STATEMENT);
        onCreate(db);
    }
}
