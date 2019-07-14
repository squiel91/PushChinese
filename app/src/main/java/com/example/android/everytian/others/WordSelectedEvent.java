package com.example.android.everytian.others;

import com.example.android.everytian.entities.Word;

public class WordSelectedEvent {
    private Word selectedWord;

    public WordSelectedEvent (Word selectedWord) {
        this.selectedWord = selectedWord;
    }

    public Word getSelectedWord() {
        return selectedWord;
    }
}
