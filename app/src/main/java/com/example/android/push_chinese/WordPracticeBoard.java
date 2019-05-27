package com.example.android.push_chinese;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.android.push_chinese.expandable_section.ExpandableSection;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class WordPracticeBoard {
    Word word = null;
    ViewGroup practiceBoard;
    MediaPlayer mediaPlayer;
    boolean audioReady;
    boolean playImmediately;


    Context context;
    ArrayList<ExpandableSection> wordSections;
    int toCollapse;

    WordPracticeBoard(Context context, ViewGroup practiceBoard) {
        this.context = context;
        practiceBoard.removeAllViews();
        this.practiceBoard = practiceBoard;
    }

    int getPixelDimensions(int unit, float value) {
        return (int) TypedValue.applyDimension(
                unit,
                value,
                context.getResources().getDisplayMetrics()
        );
    }

    private  LinearLayout createList(Context context, String[] itemsList, int textSize, int padding) {
        LinearLayout listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < itemsList.length; i++) {
            TextView textViewItem = new TextView(context);
            textViewItem.setText("▪ " + itemsList[i]);
            textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            if (i < itemsList.length - 1) {
                textViewItem.setPadding(0, 0, 0, getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, padding));
            }
            listContainer.addView(textViewItem);
        }
        return listContainer;
    }

    public  ArrayList<ExpandableSection> expandWord(final Word word, final boolean animated) {

        ArrayList<ExpandableSection> sections = new ArrayList<>();

        ExpandableSection imageSection = null;
        if (word.hasImage()) {
            imageSection = new ExpandableSection(context);
            Picasso.get().load(String.format("https://pushchinese.000webhostapp.com/resources/image/%d.jpg", word.getId()))
                    .into((ImageView) imageSection.contentView);
            practiceBoard.addView(imageSection.section);
            sections.add(imageSection);
        }

        LinearLayout innerWord = new LinearLayout(context);
        innerWord.setOrientation(LinearLayout.VERTICAL);
        innerWord.setPadding(
                getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 16),
                0,
                getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 16),
                0);

        practiceBoard.addView(innerWord);
        ExpandableSection headWordSection = new ExpandableSection(context, "Head Word", word.getHeadWord(), false, 40);
        sections.add(headWordSection);
        innerWord.addView(headWordSection);

        ExpandableSection audioSection = null;
        if (word.hasAudio()) {
            LinearLayout audioContainer = new LinearLayout(context);
            TextView pronunciationTextView = new TextView(context);
            pronunciationTextView.setText(word.getPronunciation());
            pronunciationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            pronunciationTextView.setPadding(0,0,getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 8),0);
            audioContainer.addView(pronunciationTextView);
            ImageView audioIcon = new ImageView(context);
            audioIcon.setMaxHeight(getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 14));
            audioIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            audioIcon.setImageResource(R.drawable.play_audio);
            audioContainer.addView(audioIcon);
            audioContainer.setGravity(Gravity.CENTER_VERTICAL);

            audioIcon.getLayoutParams().height = getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 16);
            audioIcon.getLayoutParams().width = getPixelDimensions(TypedValue.COMPLEX_UNIT_DIP, 16);
            audioIcon.requestLayout();

            audioReady = false;
            playImmediately = false;
            String url = String.format("https://pushchinese.000webhostapp.com/resources/audio/%d.flac", word.getId());
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(context, "Sorry! No audio", Toast.LENGTH_SHORT).show();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    audioReady = true;
                    if (playImmediately) {
                        player.start();
                        playImmediately = false;
                    }
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Toast.makeText(context, "Sorry! Audio not found", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            audioContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (audioReady) {
                        mediaPlayer.start();
                    } else {
                        playImmediately = true;
                    }
                }
            });

            audioSection = new ExpandableSection(context, "Pronunciation",  audioContainer, false);
            innerWord.addView(audioSection);
            sections.add(audioSection);
        } else {
            audioSection = new ExpandableSection(context, "Pronunciation", word.getPronunciation(), false, 20);
            innerWord.addView(audioSection);
            sections.add(audioSection);
        }

        ExpandableSection translationsSection = null;
        if ((word.getTranslations() != null) && (word.getTranslations().length > 0)) {
            LinearLayout translationsListView = createList(context, word.getTranslations(), 20, 8);
            translationsSection = new ExpandableSection(context, "Translations", translationsListView, false);
            innerWord.addView(translationsSection);
            sections.add(translationsSection);
        }

        ExpandableSection measureWordsSection = null;
        if ((word.getMeasures() != null) && (word.getMeasures().length > 0)) {
            measureWordsSection = new ExpandableSection(context, "Measure words", word.getMeasuresString(), false, 20);
            innerWord.addView(measureWordsSection);
            sections.add(measureWordsSection);
        }

        ExpandableSection examplesSection = null;
        if ((word.getExampleIds() != null) && (word.getExampleIds().length > 0)) {
            LinearLayout examplesListView = createList(context, word.getExamples(context, true), 20, 8);
            examplesSection = new ExpandableSection(context, "Examples", examplesListView, false);
            innerWord.addView(examplesSection);
            sections.add(examplesSection);
        }

        switch (word.getStage()) {
            case Word.TO_PRESENT:
                if (imageSection != null) imageSection.expand(animated);
                headWordSection.expand(animated);
                if (audioSection != null) audioSection.expand(animated);
                if (translationsSection != null) translationsSection.expand(animated);
                break;
            case Word.SHOW_HEAD_WORD:
                headWordSection.expand(animated);
                break;
            case Word.SHOW_PRONUNCIATION:
                audioSection.expand(animated);
                break;
            case Word.SHOW_TRANSLATION:
                translationsSection.expand(animated);
                break;
            case Word.SHOW_IMAGE:
                imageSection.expand(animated);
                break;
            default:
                if (imageSection != null) imageSection.expand(animated);
                headWordSection.expand(animated);
                if (audioSection != null) audioSection.expand(animated);
                if (translationsSection != null) translationsSection.expand(animated);
        }

        if (measureWordsSection != null) measureWordsSection.expand(animated);
        if (examplesSection != null) examplesSection.expand(animated);

        return sections;
    }

    public  void collapseWord(final Word newWord) {
        toCollapse = 0;
        for (ExpandableSection section : wordSections) {
            if (section.isExpanded()) {
                section.setOnCollapseListener(new ExpandableSection.OnCollapseListener() {

                    @Override
                    public void onCollapse(View view) {
                        hasCollapsed(newWord);
                    }
                });
                section.collapse(true);
                toCollapse++;
            }
        }
    }

    void hasCollapsed(Word newWord) {
        toCollapse--;
        if (toCollapse == 0) {
            word = null;
            changeWord(newWord);
        }
    }

    void changeWord(Word word) {
        changeWord(word, true);
    }

    public void emptyPractice() {
        practiceBoard.removeAllViews();
        LayoutInflater inflator = LayoutInflater.from(context);
        inflator.inflate(R.layout.empty_practice, practiceBoard, true);
    }

    public void changeWord(Word word, boolean animated) {
        if ((!animated) || (this.word == null)) {
            practiceBoard.removeAllViews();
            wordSections = expandWord(word, animated);
            this.word = word;
        } else {
            Log.w("changeWord", "null");
            collapseWord(word);
        }
    }
}
