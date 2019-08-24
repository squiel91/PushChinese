package com.example.android.tian_tian.entities.auxiliaries;

public class RecordUpdateEvent {
    private boolean learnedWord = false;
    private boolean reviewedWord = false;
    private int difficuly;

    public void addLearnedWord() {
        learnedWord = true;
    }

    public void addReviewed(int difficulty) {
        reviewedWord = true;
        this.difficuly = difficulty;
    }

    public boolean learnedWord() {
        return learnedWord;
    }

    public boolean reviewedWord() {
        return reviewedWord;
    }

    public int getDifficuly() {
        return difficuly;
    }
}