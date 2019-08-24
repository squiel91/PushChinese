package com.example.android.tian_tian.entities.auxiliaries;

import android.database.Cursor;

import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.utilities.Helper;

public class Record {
    private int day;
    private int learned;
    private int reviewed;
    private int  easy;
    private int  normal;
    private int  hard;

    public Record (int day, int learned, int reviewed, int easy, int normal, int hard) {
        this.day = day;
        this.learned = learned;
        this.reviewed = reviewed;
        this.easy = easy;
        this.normal = normal;
        this.hard = hard;
    }

    public static Record fromCursor(Cursor cursor) {
        return new Record(
                (int) cursor.getLong(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_ID)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_LEARNED)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_REVIEWED)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_EASY)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_NORMAL)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Statics.COLUMN_HARD))
        );
    }

    public int getDay() {
        return day;
    }

    public String getDateLiteral() {
        return Helper.daysSinceEpochToLiteral(day);
    }

    public int getLearned() {
        return learned;
    }

    public int getReviewed() {
        return reviewed;
    }

    public int getEasy() {
        return easy;
    }

    public int getNormal() {
        return normal;
    }

    public int getHard() {
        return hard;
    }
}