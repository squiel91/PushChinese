package com.example.android.push_chinese;

public class WordSelectedEvent {
    private Word selectedWord;

    public WordSelectedEvent (Word selectedWord) {
        this.selectedWord = selectedWord;
    }

    public Word getSelectedWord() {
        return selectedWord;
    }
}
