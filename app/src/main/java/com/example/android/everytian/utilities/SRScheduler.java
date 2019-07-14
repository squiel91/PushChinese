package com.example.android.everytian.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.everytian.data.PushDbContract;
import com.example.android.everytian.entities.Statics;
import com.example.android.everytian.entities.Word;

import java.util.ArrayList;
import java.util.Random;

import static com.example.android.everytian.utilities.Helper.daysSinceEpoch;

public class SRScheduler {

    private Context context;
    private SharedPreferences preferences;
    int newWordsStudied;
    public int wordsEachDay;
    Random random;
    int firstDayInteraction;

    final static public int YESTERDAY = -1;
    final static public int TODAY = 0;
    final static public int TOMORROW = 1;

    public interface SRSchedulerInterface {
        SRScheduler getSRScheduler();
    }

    public SRScheduler(final Context context) {
        this.context = context;
        random = new Random(); // to draw between different words

        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        if (preferences.contains("firstDayInteraction")) {
            firstDayInteraction = preferences.getInt("firstDayInteraction", 0);
        } else {
            firstDayInteraction = daysSinceEpoch();
            preferences.edit().putInt("firstDayInteraction", firstDayInteraction).apply();
        }

        newWordsStudied = preferences.getInt("newWordsStudied", 0);

        wordsEachDay = preferences.getInt("wordsEachDay", 6);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                // TODO: revise this cause is not working
                // TODO: add the stop learning words
                if (s == "wordsEachDay") {
                    wordsEachDay = sharedPreferences.getInt("wordsEachDay", 0);
                }
            }
        });
    }

    public int learnedToday() {
        return newWordsStudied;
    }

    public int getRemainingWords() {
        return Math.max(wordsEachDay - learnedToday(), 0);
    }

    private void setStudiedWords(int value) {
        preferences.edit().putInt("newWordsStudied", value).apply();
    }

    private void increaseStudiedWords() {
        newWordsStudied++;
        setStudiedWords(newWordsStudied);
    }

    private Word toRevise() {
        Cursor toReviseWordCursor = context.getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI,
                null,
                PushDbContract.Vocabulary.COLUMN_BURIED + " = ? AND " +
                        PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " > ? AND " +
                        PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " < ?",
                new String[]{ String.valueOf(PushDbContract.Vocabulary.NOT_BURIED),
                        String.valueOf(PushDbContract.Vocabulary.TO_PRESENT),
                        String.valueOf(PushDbContract.Vocabulary.LEARNED)
                },
                "RANDOM() LIMIT 1");
        Word toReviseWord = null;
        if (toReviseWordCursor.getCount() > 0) {
            toReviseWordCursor.moveToNext();
            toReviseWord = Word.from_cursor(toReviseWordCursor);
            toReviseWordCursor.close();
        }
        return toReviseWord;
    }

    public void rescheduleWord(Word word, int difficulty) {
        // is being revised
        if (word.getStage() == Word.TO_PRESENT) {
            Statics.addLearnedWord(context);
            increaseStudiedWords();
//            Toast.makeText(context, "New words: " + getRemainingWords(), Toast.LENGTH_SHORT).show();
        }
        word.response(difficulty);
        switch (difficulty) {
            case Word.EASY:
                word.setStage(Word.LEARNED);
                break;
            case Word.NORMAL:
                word.moveToNextStage();
                if (word.getStage() < Word.LEARNED) word.moveToNextStage();
                break;
            case Word.HARD:
                word.moveToNextStage();
        }
        if (word.getStage() >= Word.LEARNED) {
            Statics.addReviewedWord(context, difficulty);
            int n = word.getStage() - Word.LEARNED + 1;
            double r = 1.4355;
            double b = difficulty == Word.EASY? 2.0 : difficulty == Word.NORMAL? 1.0 : 0.1;
            int newInterval = (int)Math.ceil(b * Math.pow(r, n-1));
            Log.w("rescheduleWord", n + "/interval: " + newInterval);
            int scheduledTo =  daysSinceEpoch() + newInterval;
            word.setScheduledTo(scheduledTo);
        }
        word.persist(context);
    }

    private Cursor toReviewCursor(boolean randomFirst, int when) {
        String sortOrder = null;
        if (randomFirst) {
            sortOrder = "RANDOM() LIMIT 1";
        }
        return context.getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI,
                null,
                PushDbContract.Vocabulary.COLUMN_BURIED + " = ? AND " +
                        PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " >= ? AND " +
                        PushDbContract.Vocabulary.COLUMN_SCHEDULE_FOR + " <= ?",
                new String[]{ String.valueOf(PushDbContract.Vocabulary.NOT_BURIED),
                        String.valueOf(PushDbContract.Vocabulary.LEARNED),
                        String.valueOf(daysSinceEpoch() + when)
                },
                sortOrder
                );
    }

    public int toReviewQuantity(int when) {
        return toReviewCursor(false, when).getCount();
    }

    public Word toReview() {
        return toReview(0);
    }

    public Word toReview(int when) {
        Cursor toReviseWordCursor = toReviewCursor(true, when);
        Word toReviseWord = null;
        if (toReviseWordCursor.getCount() > 0) {
            toReviseWordCursor.moveToNext();
            toReviseWord = Word.from_cursor(toReviseWordCursor);
            toReviseWordCursor.close();
        }
        return toReviseWord;
    }

    private Word getNewWord() {
        Cursor randomWordCursor = context.getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI,
                null,
                PushDbContract.Vocabulary.COLUMN_BURIED + " = ? AND " + PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " = ?",
                new String[]{ String.valueOf(PushDbContract.Vocabulary.NOT_BURIED)
                , String.valueOf(PushDbContract.Vocabulary.TO_PRESENT) },
                "RANDOM() LIMIT 1");
        Word randomWord = null;
        if (randomWordCursor.getCount() > 0) {
            randomWordCursor.moveToNext();
            randomWord = Word.from_cursor(randomWordCursor);
            randomWordCursor.close();
        }
        return randomWord;
    }

    public Word nextStudyWord() {
        checkForNewDay();
        // NOTICE: id doesn't remember last served word, so better ask where did you left off before calling this function
        ArrayList<Word> possibleWords = new ArrayList<>();

        if (getRemainingWords() > 0) {
            Word newWord = getNewWord();
            if (newWord != null) possibleWords.add(newWord);
        }
        Word reviseWord = toRevise();
        if (reviseWord != null) possibleWords.add(reviseWord);
        Word reviewWord = toReview();
        if (reviewWord != null) possibleWords.add(reviewWord);

        return possibleWords.size() > 0? possibleWords.get(random.nextInt(possibleWords.size())) : null;
    }

    private void checkForNewDay() {
        if (firstDayInteraction < daysSinceEpoch()) {
            setStudiedWords(0);
            int newDay = daysSinceEpoch();
            preferences.edit().putInt("firstDayInteraction", newDay).apply();
            firstDayInteraction = newDay;
//            Toast.makeText(context, "NEW DAY PASSED", Toast.LENGTH_SHORT).show();
        }
    }
}
