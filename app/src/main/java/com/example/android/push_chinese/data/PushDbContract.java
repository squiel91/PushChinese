package com.example.android.push_chinese.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PushDbContract {

    private PushDbContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.push_chinese";
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
        public static final String COLUMN_TRANSLATION = "translation";
        public static final String COLUMN_MEASURES = "measures";
        public static final String COLUMN_EXAMPLES = "examples";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_AUDIO = "audio";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DIFFICULTY = "difficulty";
        public static final String COLUMN_BURIED = "buried";

        // For assigning to difficulty
        public static final int UNKNOWN = 0;
        public static final int EASY = 1;
        public static final int NORMAL = 2;
        public static final int DIFFICULT = 3;

        // For assigning to BURRY
        public static final int NOT_BURIED = 0;
        public static final int BURIED = 1;

    }

    public static abstract class Sentences implements BaseColumns {
        public static final String TABLE_NAME = "sentences";

        public static final String PATH_SENTENCES = TABLE_NAME;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SENTENCES);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SENTENCES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SENTENCES;


        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_SENTENCE = "sentence";
        public static final String COLUMN_TRANSLATION = "translation";
        public static final String COLUMN_AUDIO = "audio";

    }

}
