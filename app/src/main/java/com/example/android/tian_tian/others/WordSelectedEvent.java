package com.example.android.tian_tian.others;

import com.example.android.tian_tian.entities.Word;

public class WordSelectedEvent {
    private Word selectedWord;

    public WordSelectedEvent (Word selectedWord) {
        this.selectedWord = selectedWord;
    }

    public Word getSelectedWord() {
        return selectedWord;
    }
}
