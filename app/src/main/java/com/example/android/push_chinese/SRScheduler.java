package com.example.android.push_chinese;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.android.push_chinese.data.PushDbContract;

public class SRScheduler {

    private Context context;
    private SharedPreferences preferences;
    Integer remainingWords = null;

    // Difficulty
    public static int EASY = 0;
    public static int NORMAL = 1;
    public static int HARD = 2;

    public SRScheduler(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    private int getWordsEachDay() {
        return preferences.getInt("wordsEachDay", 20);
    }

    private int getRemainingWords() {
        // TODO: handle new day
        if (remainingWords == null) {
            // first time that is called
            if (preferences.contains("todayWordsRemaining")) {
                remainingWords = preferences.getInt("todayWordsRemaining", 0);
            } else {
                remainingWords = getWordsEachDay();
                setRemainingWords(remainingWords);
            }
        }
        return remainingWords;
    }

    private void setRemainingWords(int value) {
        int remainingWords = value;
        preferences.edit().putInt("todayWordsRemaining",value).apply();
    }

    private void reduceByOneRemainingWords() {
        setRemainingWords(getRemainingWords()-1);
    }

    public void reschreduleWord(Word word, int difficulty) {
        reduceByOneRemainingWords();
        // TODO: reschedule word
    }

    private Word toRevise() {
        // TODO: return the next word to practice, null is none;
        return null;
    }

    private Word getNewWord() {
        Cursor randomWordCursor = context.getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI,
                null,
                PushDbContract.Vocabulary.COLUMN_BURIED + " = ?",
                new String[]{ String.valueOf(PushDbContract.Vocabulary.NOT_BURIED) },
                "RANDOM() LIMIT 1");
        Word randomWord = null;
        if (randomWordCursor.getCount() > 0) {
            randomWordCursor.moveToNext();
            randomWord = Word.from_cursor(randomWordCursor);
            randomWordCursor.close();
        }
        Toast.makeText(context, "Remaining Words Today: " + getRemainingWords(), Toast.LENGTH_SHORT);
        return randomWord;
    }

    public Word nextStudyWord() {
        // NOTICE: id doesn't remember last served word, so better ask where did you left off before calling this function
        Word nextWord = toRevise();
        if (nextWord == null) {
            if (getRemainingWords() > 0) {
                // Lean new word
                nextWord = getNewWord();
            }
        }
        return nextWord;
    }
}
