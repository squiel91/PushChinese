package com.example.android.push_chinese.entities;

import android.content.Context;
import android.database.Cursor;

import com.example.android.push_chinese.data.PushDbContract;
import com.example.android.push_chinese.entities.auxiliaries.Record;
import com.example.android.push_chinese.entities.auxiliaries.RecordUpdateEvent;
import com.example.android.push_chinese.entities.auxiliaries.UpdateRecordAsyncTask;
import com.example.android.push_chinese.utilities.Helper;

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

    public static Record[] getLastRecords(Context context, int quantity) {
        int today = Helper.daysSinceEpoch();
        Record[] records = new Record[quantity];

        Cursor recordCursor = context.getContentResolver().query(PushDbContract.Statics.CONTENT_URI,
                null,
                null,
                null,
                PushDbContract.Statics.COLUMN_ID + " DESC LIMIT " + quantity);

        Record lastConsultedRecord = null;
        for (int i = quantity; i > 0; i--) {
            if (lastConsultedRecord == null) {
                if (recordCursor.moveToPosition(quantity - i)) {
                    lastConsultedRecord = Record.fromCursor(recordCursor);
                }
            }
            int  iterationDay = today - (quantity - i);
            if (lastConsultedRecord == null || lastConsultedRecord.getDay() < iterationDay) {
                records[i - 1] = emptyRecord(iterationDay);
            } else {
                records[i - 1] = lastConsultedRecord;
                lastConsultedRecord = null;
            }
        }
        return records;
    }
}
