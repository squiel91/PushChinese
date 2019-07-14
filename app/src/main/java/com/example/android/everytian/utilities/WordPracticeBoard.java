package com.example.android.everytian.utilities;

import android.animation.Animator;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.everytian.R;
import com.example.android.everytian.entities.Word;
import com.example.android.everytian.expandable_section.ExpandableSection;

import java.util.ArrayList;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WordPracticeBoard {
    Word word = null;
    public ViewGroup card;
    MediaPlayer mediaPlayer;
    ExpandableSection.OnLockedClick onLockedClick = null;


    Context context;
    ArrayList<ExpandableSection> wordSections;
    int toCollapse;

    public void enterScreen(final OnAnimationFinishes callback) {
        show();
        YoYo.with(Techniques.RotateInDownRight).duration(300).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                if (callback != null) callback.onAnimationFinishes();
            }
        }).playOn(card);
    }

    public interface OnAnimationFinishes {
        void onAnimationFinishes();
    }

    public void hide() {
        card.setVisibility(View.GONE);
    }

    public void show() {
        card.setVisibility(View.VISIBLE);

    }

    public LinearLayout getContainer() {
        LinearLayout container = (LinearLayout) card.getChildAt(0);
        card.removeAllViews();
        return container;
    }

    public void setContainer(LinearLayout container) {
        card.removeAllViews();
        card.addView(container);
        container.invalidate();
        container.requestLayout();
        card.invalidate();
        card.requestLayout();
    }

    public void exitScreen(final OnAnimationFinishes callback) {
        YoYo.with(Techniques.RotateOutUpLeft).duration(300).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                if (callback != null) callback.onAnimationFinishes();
            }
        }).playOn(card);
    }

    public WordPracticeBoard(Context context, ViewGroup practiceBoard) {
        this.context = context;
        practiceBoard.removeAllViews();
        this.card = practiceBoard;
    }

    public void unlockSections() {
        for (ExpandableSection wordSection : wordSections) {
            wordSection.setLock(false);
        }
    }

    public void setOnLockedClick(ExpandableSection.OnLockedClick onLockedClick) {
        this.onLockedClick = onLockedClick;
    }

    private  LinearLayout createList(Context context, String[] itemsList, int textSize, int padding) {

        LinearLayout listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < itemsList.length; i++) {
            LinearLayout lineContainer = new LinearLayout(context);
            lineContainer.setOrientation(LinearLayout.HORIZONTAL);
            if (i < itemsList.length - 1) {
                lineContainer.setPadding(0, 0, 0, Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, padding));
            }

            TextView textViewBullet = new TextView(context);
            textViewBullet.setText("•");
            textViewBullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            textViewBullet.setPadding(
                    0,
                    0,
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 8),
                    0
            );

            TextView textViewItem = new TextView(context);
            textViewItem.setText(itemsList[i]);
            textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            lineContainer.addView(textViewBullet);
            lineContainer.addView(textViewItem);
            listContainer.addView(lineContainer);
        }
        return listContainer;
    }

    public  ArrayList<ExpandableSection> expandWord(final Word word, final boolean animated) {

        ArrayList<ExpandableSection> sections = new ArrayList<>();
        LinearLayout innerWord = new LinearLayout(context);

        innerWord.setOrientation(LinearLayout.VERTICAL);
        card.addView(innerWord);

        ExpandableSection imageSection = null;
        if (word.hasImage()) {
            imageSection = new ExpandableSection(context, true);
            Helper.setImage(context, String.format("image_%d", word.getId()), (ImageView) imageSection.contentView);
//            Picasso.get().load(String.format("https://pushchinese.000webhostapp.com/resources/image/image_%d.jpg", word.getId()))
//                    .into((ImageView) imageSection.contentView);
            innerWord.addView(imageSection);
            sections.add(imageSection);
        }

        ExpandableSection audioSection = null;
        if (word.hasAudio()) {
            LinearLayout audioContainer = new LinearLayout(context);
            TextView pronunciationTextView = new TextView(context);
            pronunciationTextView.setText(Helper.toPinyin(word.getPronunciation()));
            pronunciationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            audioContainer.addView(pronunciationTextView);
            ImageView audioIcon = new ImageView(context);
            audioIcon.setMaxHeight(Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 24));
            audioIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            audioIcon.setImageResource(R.drawable.icon_voice);
            audioContainer.addView(audioIcon);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 24),
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 24)
            );
            params.setMargins(Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 12),
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 18), 0, 0);
            audioIcon.setLayoutParams(params);
            audioIcon.requestLayout();
            final ImageView audioIconFinal = audioIcon;

            mediaPlayer = MediaPlayer.create(context, context.getResources().getIdentifier(
                    String.format("audio_%d", word.getId()),
                    "raw",
                    "com.example.android.everytian"));

            audioContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YoYo.with(Techniques.Pulse)
                            .duration(500)
                            .repeat(2)
                            .playOn(audioIconFinal);
                    mediaPlayer.start();
                }
            });

            audioSection = new ExpandableSection(context, "Pronunciation",  audioContainer, false);
            innerWord.addView(audioSection);
            sections.add(audioSection);
        } else {
            audioSection = new ExpandableSection(context, "Pronunciation", Helper.toPinyin(word.getPronunciation()), false, 40);
            innerWord.addView(audioSection);
            sections.add(audioSection);
        }

        ExpandableSection headWordSection = new ExpandableSection(context, "Characters", word.getHeadWord(), false, 40);
        sections.add(headWordSection);
        innerWord.addView(headWordSection);

        ExpandableSection translationsSection = null;
        if ((word.getTranslations() != null) && (word.getTranslations().length > 0)) {
            LinearLayout translationsListView = createList(context, word.getTranslations(), 20, 8);
            translationsSection = new ExpandableSection(context, "Translations", translationsListView, false);
            innerWord.addView(translationsSection);
            sections.add(translationsSection);
        }

        ExpandableSection measureWordsSection = null;
        if ((word.getMeasures() != null) && (word.getMeasures().length > 0)) {
            measureWordsSection = new ExpandableSection(context, "Measure words", Helper.join(", ", word.getMeasures()), false, 20);
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

        View footer = new View(context);
        innerWord.addView(footer);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) footer.getLayoutParams();
        layoutParams.height = Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 40);
        layoutParams.width = MATCH_PARENT;
        footer.setLayoutParams(layoutParams);

        if (word.getStage() == Word.TO_PRESENT) {
            if (imageSection != null) imageSection.expand(animated);
            headWordSection.expand(animated);
            if (audioSection != null) audioSection.expand(animated);
            if (translationsSection != null) translationsSection.expand(animated);
        } else {
            int recall_test;
            if (word.getStage() < Word.LEARNED) recall_test = word.getStage();
            else {
                recall_test = Word.SHOW_HEAD_WORD;
                ArrayList<Integer> possibleWords = word.getPossibleStages();
                recall_test =  possibleWords.get((new Random()).nextInt(possibleWords.size()));
            }
            switch (recall_test) {
                case Word.SHOW_HEAD_WORD:
                    headWordSection.expand(animated);
                    audioSection.setLock(true);
                    translationsSection.setLock(true);
                    break;
                case Word.SHOW_PRONUNCIATION:
                    headWordSection.setLock(true);
                    audioSection.expand(animated);
                    translationsSection.expand(animated);
                    break;
                case Word.SHOW_TRANSLATION:
                    headWordSection.setLock(true);
                    audioSection.setLock(true);
                    translationsSection.expand(animated);
                    break;
                case Word.SHOW_IMAGE:
                    headWordSection.setLock(true);
                    audioSection.setLock(true);
                    translationsSection.setLock(true);
                    imageSection.expand(animated);
                    break;
                default:
                    if (imageSection != null) imageSection.expand(animated);
                    headWordSection.setLock(true);
                    if (audioSection != null) audioSection.expand(animated);
                    if (translationsSection != null) translationsSection.expand(animated);
            }
        }

        if (measureWordsSection != null) measureWordsSection.expand(animated);
        if (examplesSection != null) examplesSection.expand(animated);

        for (ExpandableSection section : sections) {
            if (section.isLocked()) section.setOnLockedClick(onLockedClick);
        }

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
        card.removeAllViews();
        LayoutInflater inflator = LayoutInflater.from(context);
        inflator.inflate(R.layout.empty_practice, card, true);
    }

    public void changeWord(Word word, boolean animated) {
        if ((!animated) || (this.word == null)) {
            card.removeAllViews();
            wordSections = expandWord(word, animated);
            this.word = word;
        } else {
            Log.w("changeWord", "null");
            collapseWord(word);
        }
    }
}
