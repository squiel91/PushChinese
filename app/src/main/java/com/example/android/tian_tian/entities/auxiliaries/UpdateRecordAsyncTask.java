package com.example.android.tian_tian.entities.auxiliaries;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.android.tian_tian.data.PushDbContract.Statics;
import com.example.android.tian_tian.entities.Word;

import static com.example.android.tian_tian.utilities.Helper.daysSinceEpoch;

public class UpdateRecordAsyncTask extends AsyncTask<RecordUpdateEvent, Void, Void> {

    Context context;

    public UpdateRecordAsyncTask (Context context) {
        super();
        this.context = context;

    }
    @Override
    public Void doInBackground(RecordUpdateEvent... recordUpdateEvents) {
        RecordUpdateEvent recordUpdateEvent = recordUpdateEvents[0];

        int days = daysSinceEpoch();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Statics.CONTENT_URI, null, Statics.COLUMN_ID + " = ?", new String[] {
                String.valueOf(days),
        }, null);

        Record record;

        if (cursor.moveToFirst()) {
            record = Record.fromCursor(cursor);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Statics.COLUMN_ID, days);
            resolver.insert(Statics.CONTENT_URI, contentValues);
            record = new Record(days, 0, 0, 0, 0,0);
        }

        ContentValues contentValues = new ContentValues();
        if (recordUpdateEvent.learnedWord()) {
            contentValues.put(Statics.COLUMN_LEARNED, record.getLearned() + 1);
        }
        if (recordUpdateEvent.reviewedWord()) {
            contentValues.put(Statics.COLUMN_REVIEWED, record.getReviewed() + 1);
            switch (recordUpdateEvent.getDifficuly()) {
                case Word.EASY:
                    contentValues.put(Statics.COLUMN_EASY, record.getEasy() + 1);
                    break;
                case Word.NORMAL:
                    contentValues.put(Statics.COLUMN_NORMAL, record.getNormal() + 1);
                    break;
                case Word.HARD:
                    contentValues.put(Statics.COLUMN_HARD, record.getHard() + 1);
            }
        }
        resolver.update(Uri.parse(Statics.CONTENT_URI + "/" + record.getDay()), contentValues,
                null, null);
        return null;
    }
}