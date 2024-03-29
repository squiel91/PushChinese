package com.example.android.tian_tian.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PushDbContract {

    private PushDbContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.tian_tian";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static abstract class Vocabulary implements BaseColumns {
        public static final String TABLE_NAME = "vocabulary";

        public static final String PATH_VOCABULARY = TABLE_NAME;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VOCABULARY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOCABULARY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOCABULARY;


        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_HEAD_WORD = "head_word";
        public static final String COLUMN_PRONUNCIATION = "pronunciation";
        public static final String COLUMN_PRONUNCIATION_SEARCHABLE = "pronunciation_searchable";
        public static final String COLUMN_TRANSLATION = "translation";
        public static final String COLUMN_MEASURES = "measures";
        public static final String COLUMN_EXAMPLES = "examples";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_AUDIO = "audio";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_SCHEDULE_FOR = "scheduled";
        public static final String COLUMN_LEARNING_STAGE = "learning_stage";

        // For assigning to LEARNING_STAGE
        public final static int TO_PRESENT = 0;
        public final static int SHOW_HEAD_WORD = 1;
        public final static int SHOW_PRONUNCIATION = 2;
        public final static int SHOW_TRANSLATION = 3;
        public final static int SHOW_IMAGE = 4;
        public final static int LEARNED = 5;


    }

    public static abstract class Statics implements BaseColumns {
        public static final String TABLE_NAME = "statics";

        public static final String PATH_STATICS = TABLE_NAME;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STATICS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATICS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATICS;


        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_LEARNED = "learned";
        public static final String COLUMN_REVIEWED = "reviewed";
        public static final String COLUMN_EASY = "easy";
        public static final String COLUMN_NORMAL = "normal";
        public static final String COLUMN_HARD = "hard";

    }

}
