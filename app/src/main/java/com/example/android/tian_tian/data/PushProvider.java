package com.example.android.tian_tian.data;

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

import static com.example.android.tian_tian.data.PushDbContract.CONTENT_AUTHORITY;
import static com.example.android.tian_tian.data.PushDbContract.Vocabulary.PATH_VOCABULARY;
import static com.example.android.tian_tian.data.PushDbContract.Statics.PATH_STATICS;

import com.example.android.tian_tian.data.PushDbContract.Vocabulary;
import com.example.android.tian_tian.data.PushDbContract.Statics;

public class PushProvider extends ContentProvider {

    private PushDbHelper dbHelper;
    public static final String LOG_TAG = PushProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int VOCABULARY = 100;
    private static final int VOCABULARY_ID = 101;
    private static final int STATICS = 300;
    private static final int STATICS_ID = 301;

    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_VOCABULARY, VOCABULARY);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_VOCABULARY + "/#", VOCABULARY_ID);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_STATICS, STATICS);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_STATICS + "/#", STATICS_ID);
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
                cursor = database.query(Vocabulary.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case VOCABULARY_ID:
                selection = Vocabulary._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(Vocabulary.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STATICS:
                cursor = database.query(Statics.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STATICS_ID:
                selection = Statics._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(Statics.TABLE_NAME, projection, selection, selectionArgs,
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
                return Vocabulary.CONTENT_LIST_TYPE;
            case VOCABULARY_ID:
                return Vocabulary.CONTENT_ITEM_TYPE;
            case STATICS:
                return Statics.CONTENT_LIST_TYPE;
            case STATICS_ID:
                return Statics.CONTENT_ITEM_TYPE;
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
            case STATICS:
                return insertStatic(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertWord(Uri uri, ContentValues contentValues) {
        sanityCheck(contentValues, true);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // description is column in items table, item.description has value for description
        long word_id = db.insert(Vocabulary.TABLE_NAME, null, contentValues);

        if (word_id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            return ContentUris.withAppendedId(uri, word_id);
        }
    }

    private Uri insertStatic(Uri uri, ContentValues contentValues) {
        if (!contentValues.containsKey(Statics.COLUMN_ID)) {
            throw new IllegalArgumentException("Static id should be the inserted day number");
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        long static_id = db.insert(Statics.TABLE_NAME, null, contentValues);

        if (static_id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            return ContentUris.withAppendedId(uri, static_id);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return delete(uri);
    }

    public int delete(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        ContentValues args = new ContentValues();
        String entity_id = String.valueOf(ContentUris.parseId(uri));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted_quantity = 0;
        switch (match) {
            case VOCABULARY_ID:
                deleted_quantity = db.delete(Vocabulary.TABLE_NAME,
                        Vocabulary._ID + " = ?", new String[]{entity_id});
                break;
            case STATICS_ID:
                deleted_quantity = db.delete(Statics.TABLE_NAME,
                        Statics._ID + " = ?", new String[]{entity_id});
                break;
            default:
                // TODO: correct to a throw
                Log.e(LOG_TAG, "Delete is not supported for " + uri);
                // throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
        return deleted_quantity;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        sanityCheck(contentValues, false);

        if(contentValues.size() > 0) {
            int match = uriMatcher.match(uri);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String entity_id = String.valueOf(ContentUris.parseId(uri));
            switch (match) {
                case VOCABULARY_ID:
                    return db.update(Vocabulary.TABLE_NAME, contentValues,
                            Vocabulary._ID + " = ?", new String[]{entity_id});
                case STATICS_ID:
                    return db.update(Statics.TABLE_NAME, contentValues,
                            Vocabulary._ID + " = ?", new String[]{entity_id});
                default:
                    throw new IllegalArgumentException("updateBury is not supported for " + uri);
            }
        }
        return 1;
    }

    private void sanityCheck(ContentValues contentValues, boolean required) {
        // TODO: check that there are not more parameters that there should be
        String head_word = contentValues.getAsString(PushDbContract.Vocabulary.COLUMN_HEAD_WORD);
        if (required && head_word == null) { throw new IllegalArgumentException("Word requires a head word"); }

        Integer level = contentValues.getAsInteger(PushDbContract.Vocabulary.COLUMN_LEVEL);
        if (level != null && (level < 0 || level > 6)) { throw new IllegalArgumentException("If level specified, it should be between 0 and 6"); }

        String translation = contentValues.getAsString(PushDbContract.Vocabulary.COLUMN_TRANSLATION);
        if (required && translation == null) { throw new IllegalArgumentException("Word requires a translation"); }
    }
}
