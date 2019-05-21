package com.example.android.push_chinese.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import static com.example.android.push_chinese.data.PushDbContract.CONTENT_AUTHORITY;
import static com.example.android.push_chinese.data.PushDbContract.Vocabulary.PATH_VOCABULARY;
import static com.example.android.push_chinese.data.PushDbContract.Sentences.PATH_SENTENCES;

public class PushProvider extends ContentProvider {

    private PushDbHelper dbHelper;
    public static final String LOG_TAG = PushProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int VOCABULARY = 100;
    private static final int VOCABULARY_ID = 101;
    private static final int SENTENCES = 200;
    private static final int SENTENCES_ID = 201;

    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_VOCABULARY, VOCABULARY);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_VOCABULARY + "/#", VOCABULARY_ID);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_SENTENCES, SENTENCES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_SENTENCES + "/#", SENTENCES_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PushDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY:
                cursor = database.query(PushDbContract.Vocabulary.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case VOCABULARY_ID:
                selection = PushDbContract.Vocabulary._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PushDbContract.Vocabulary.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SENTENCES:
                cursor = database.query(PushDbContract.Sentences.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SENTENCES_ID:
                selection = PushDbContract.Sentences._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                Log.w("TEST_ID", String.valueOf(selectionArgs[0]));
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PushDbContract.Sentences.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY:
                return PushDbContract.Vocabulary.CONTENT_LIST_TYPE;
            case VOCABULARY_ID:
                return PushDbContract.Vocabulary.CONTENT_ITEM_TYPE;
            case SENTENCES:
                return PushDbContract.Sentences.CONTENT_LIST_TYPE;
            case SENTENCES_ID:
                return PushDbContract.Sentences.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY:
                return insertWord(uri, contentValues);
            case SENTENCES:
                return insertSentence(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertWord(Uri uri, ContentValues contentValues) {
        sanityCheck(contentValues, true);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // description is column in items table, item.description has value for description
        long word_id = db.insert(PushDbContract.Vocabulary.TABLE_NAME, null, contentValues);

        db.close();

        if (word_id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            return ContentUris.withAppendedId(uri, word_id);
        }
    }

    private Uri insertSentence(Uri uri, ContentValues contentValues) {
        if (!contentValues.containsKey(PushDbContract.Sentences.COLUMN_SENTENCE)) {
            throw new IllegalArgumentException("Sentence can not be empty");
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // description is column in items table, item.description has value for description
        long sentence_id = db.insert(PushDbContract.Sentences.TABLE_NAME, null, contentValues);

        db.close();

        if (sentence_id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            return ContentUris.withAppendedId(uri, sentence_id);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return delete(uri);
    }

    public int delete(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY_ID:
                ContentValues args = new ContentValues();
                String word_id = String.valueOf(ContentUris.parseId(uri));
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                return db.delete(PushDbContract.Vocabulary.TABLE_NAME, PushDbContract.Vocabulary._ID + " = ?", new String[]{word_id});
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        sanityCheck(contentValues, false);
        int updated_row;
        if (contentValues.containsKey(PushDbContract.Vocabulary.COLUMN_DIFFICULTY)) {
            updateDifficulty(uri, contentValues.getAsInteger(PushDbContract.Vocabulary.COLUMN_DIFFICULTY));
            contentValues.remove(PushDbContract.Vocabulary.COLUMN_DIFFICULTY);
        }
        if (contentValues.containsKey(PushDbContract.Vocabulary.COLUMN_BURIED)) {
            updateBury(uri, contentValues.getAsInteger(PushDbContract.Vocabulary.COLUMN_BURIED));
            contentValues.remove(PushDbContract.Vocabulary.COLUMN_BURIED);
        }

        if(contentValues.size() > 0) {
            int match = uriMatcher.match(uri);
            switch (match) {
                case VOCABULARY_ID:
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    String word_id = String.valueOf(ContentUris.parseId(uri));

                    db.update(PushDbContract.Vocabulary.TABLE_NAME, contentValues,
                            PushDbContract.Vocabulary._ID + " = ?", new String[]{word_id});
                default:
                    throw new IllegalArgumentException("updateBury is not supported for " + uri);
            }
        }
        return 1;
    }

    public int updateDifficulty(@NonNull Uri uri, int difficulty) {
        if (difficulty < PushDbContract.Vocabulary.UNKNOWN || difficulty > PushDbContract.Vocabulary.DIFFICULT) {
            throw new IllegalArgumentException("difficulty parameter should be a number between 0 and 3");
        }
        int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY_ID:
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String word_id = String.valueOf(ContentUris.parseId(uri));

                ContentValues args = new ContentValues();
                ContentValues  contentValues= new ContentValues();
                contentValues.put(PushDbContract.Vocabulary.COLUMN_DIFFICULTY, difficulty);

                return db.update(PushDbContract.Vocabulary.TABLE_NAME, contentValues,
                        PushDbContract.Vocabulary._ID + " = ?", new String[]{word_id});
            default:
                throw new IllegalArgumentException("updateBury is not supported for " + uri);
        }
    }

    public int updateBury(@NonNull Uri uri, int bury) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case VOCABULARY_ID:
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String word_id = String.valueOf(ContentUris.parseId(uri));

                ContentValues args = new ContentValues();
                ContentValues  contentValues= new ContentValues();
                contentValues.put(PushDbContract.Vocabulary.COLUMN_BURIED, bury);
//                Log.w("ID", String.valueOf(word_id));
                int toReturn = db.update(PushDbContract.Vocabulary.TABLE_NAME, contentValues,
                        PushDbContract.Vocabulary._ID + " = ?", new String[]{word_id});
//                Log.w("RETURM", String.valueOf(toReturn));
                return toReturn;
            default:
                throw new IllegalArgumentException("updateBury is not supported for " + uri);
        }
    }

    private void sanityCheck(ContentValues contentValues, boolean required) {
        // TODO: check that there are not more parameters that there should be
        String name = contentValues.getAsString(PushDbContract.Vocabulary.COLUMN_HEAD_WORD);
        if (required && name == null) { throw new IllegalArgumentException("Word requires a name"); }

        Integer level = contentValues.getAsInteger(PushDbContract.Vocabulary.COLUMN_LEVEL);
        if (level != null && (level < 0 || level > 6)) { throw new IllegalArgumentException("If level specified, it should be between 0 and 6"); }

        String translation = contentValues.getAsString(PushDbContract.Vocabulary.COLUMN_TRANSLATION);
        if (required && translation == null) { throw new IllegalArgumentException("Word requires a translation"); }
    }
}
