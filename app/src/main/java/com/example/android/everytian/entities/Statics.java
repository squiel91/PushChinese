package com.example.android.everytian.entities;

import android.content.Context;
import android.database.Cursor;

import com.example.android.everytian.data.PushDbContract;
import com.example.android.everytian.entities.auxiliaries.Record;
import com.example.android.everytian.entities.auxiliaries.RecordUpdateEvent;
import com.example.android.everytian.entities.auxiliaries.UpdateRecordAsyncTask;
import com.example.android.everytian.utilities.Helper;

import java.util.ArrayList;

public class Statics {

    public static void addLearnedWord(Context context) {

        RecordUpdateEvent recordUpdateEvent = new RecordUpdateEvent();
        recordUpdateEvent.addLearnedWord();
        UpdateRecordAsyncTask task = new UpdateRecordAsyncTask(context);
        task.execute(recordUpdateEvent);
    }

    public static void addReviewedWord(Context context, int difficulty) {
        RecordUpdateEvent recordUpdateEvent = new RecordUpdateEvent();
        recordUpdateEvent.addReviewed(difficulty);
        UpdateRecordAsyncTask task = new UpdateRecordAsyncTask(context);
        task.execute(recordUpdateEvent);
    }

    public static Record emptyRecord(int day) {
        return new Record(day, 0,0,0,0,0);
    }

    public static ArrayList<Record> getLastRecords(Context context, int maxQuantity) {
        int today = Helper.daysSinceEpoch();
        ArrayList<Record> records = new ArrayList<>();

        Cursor recordCursor = context.getContentResolver().query(PushDbContract.Statics.CONTENT_URI,
                null,
                PushDbContract.Statics.COLUMN_ID + " <= " + today,
                null,
                PushDbContract.Statics.COLUMN_ID + " DESC LIMIT " + maxQuantity);

        Record lastConsultedRecord;
        if (recordCursor.moveToFirst()) {
            lastConsultedRecord = Record.fromCursor(recordCursor);
        } else {
            lastConsultedRecord = emptyRecord(today);
        }

        for (int i = 0; i < maxQuantity; i++) {
            int  iterationDay = today - i;
            if (lastConsultedRecord.getDay() == iterationDay) records.add(lastConsultedRecord);
            else {
                records.add(emptyRecord(iterationDay));
                continue;
            }

            if (recordCursor.moveToNext()) {
                lastConsultedRecord = Record.fromCursor(recordCursor);
            } else break;
        }
        return records;
    }
}
