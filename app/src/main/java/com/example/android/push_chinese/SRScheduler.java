package com.example.android.push_chinese;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import com.example.android.push_chinese.data.PushDbContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class SRScheduler {

    private Context context;
    private SharedPreferences preferences;
    int newWordsStudied;
    public int wordsEachDay;
    Random random;
    Date firstDayInteraction;

    public interface SRSchedulerInterface {
        SRScheduler getSRScheduler();
    }

    public SRScheduler(final Context context) {
        this.context = context;
        random = new Random();
        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        if (preferences.contains("firstDayInteraction")) {
            firstDayInteraction = new Date(preferences.getLong("firstDayInteraction", 0));
        } else {
            firstDayInteraction = new Date();
            preferences.edit().putLong("firstDayInteraction", firstDayInteraction.getTime()).apply();
        }

        newWordsStudied = preferences.getInt("newWordsStudied", 0);

        wordsEachDay = preferences.getInt("wordsEachDay", 20);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                // TODO: revise this cause is not working
                // TODO: add the stop learning words
                if (s == "wordsEachDay") {
                    wordsEachDay = sharedPreferences.getInt("wordsEachDay", 0);
                    Log.w("OOOOOOOO: wordsEachDay", String.valueOf(wordsEachDay));
                }
                Log.w("TOPER","out");
            }
        });
    }

    public int getRemainingWords() {
        Log.w("OO0O: getRemainingWords", String.valueOf(Math.max(wordsEachDay - newWordsStudied, 0)));
        return Math.max(wordsEachDay - newWordsStudied, 0);
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
        if (word.getHeadWord() == "写") {
            int t=4;
            Log.w("dummy", "t");
        }
        // is being revised
        if (word.getStage() == Word.TO_PRESENT) {
            increaseStudiedWords();
//            Toast.makeText(context, "New words: " + getRemainingWords(), Toast.LENGTH_SHORT).show();
        }
        word.response(difficulty);
        if (word.moveToNextStage() >= Word.LEARNED) {
            int n = word.getStage() - Word.LEARNED + 1;
            double r = 1.4355;
            double b = difficulty == Word.EASY? 2.0 : difficulty == Word.NORMAL? 1.0 : 0.1;
            int newInterval = (int)Math.ceil(b * Math.pow(r, n-1));
            Log.w("rescheduleWord", n + "/interval: " + newInterval);
            int scheduledTo =  howManyDaysSinceBC() + newInterval;
            word.setScheduledTo(scheduledTo);
        }
        Log.w("rescheduleWord", "stage: " + word.getStage());
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
                        String.valueOf(howManyDaysSinceBC() + when)
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
        Word nextWord = null;
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

    public int howManyDaysSinceBC() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        return cal.get(Calendar.YEAR) * 366 + cal.get(Calendar.DAY_OF_YEAR);
    }

    private boolean oneDayOrMorePassed(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        int dayOfYear1 = cal1.get(Calendar.DAY_OF_YEAR);
        int dayOfYear2 = cal2.get(Calendar.DAY_OF_YEAR);

        return (year2 > year1) || (dayOfYear2 > dayOfYear1);
    }

    private void checkForNewDay() {
        Date now = new Date();
        if (oneDayOrMorePassed(firstDayInteraction, now)) {
            setStudiedWords(0);
            preferences.edit().putLong("firstDayInteraction", (new Date()).getTime()).apply();
            firstDayInteraction = now;
//            Toast.makeText(context, "NEW DAY PASSED", Toast.LENGTH_SHORT).show();
        }
    }
}
