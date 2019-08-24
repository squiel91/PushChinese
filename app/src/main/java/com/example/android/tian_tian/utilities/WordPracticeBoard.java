package com.example.android.tian_tian.utilities;

import android.animation.Animator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.divyanshu.draw.widget.Line;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.entities.Word;
import com.example.android.tian_tian.expandable_section.ExpandableSection;
import com.example.android.tian_tian.onboarding.OnboardingNavigator;
import com.example.android.tian_tian.onboarding.OnboardingSliderAdaptor;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.example.android.tian_tian.data.PushProvider.LOG_TAG;

public class WordPracticeBoard {
    Word word = null;
    public ViewGroup card;
    MediaPlayer mediaPlayer;
    FragmentManager supportFragmentManager;
    ExpandableSection.OnLockedClick onLockedClick = null;

    Integer examplePreviousIndex = 0;
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

        YoYo.with(Techniques.FadeIn).duration(0).playOn(card);

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
                hide();
            }
        }).playOn(card);
    }

    public WordPracticeBoard(Context context, ViewGroup practiceBoard, FragmentManager supportFragmentManager) {
        this.context = context;
        this.supportFragmentManager = supportFragmentManager;
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
            textViewItem.setTextIsSelectable(true);
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
            ImageView descriptiveImageView = (ImageView) imageSection.contentView;
            Picasso.get().load(new File(word.getImageURI())).into(descriptiveImageView);
            innerWord.addView(imageSection);
            sections.add(imageSection);
        }

        ExpandableSection headWordSection = new ExpandableSection(context, "Characters", word.getHeadWord(), false, 40);
        sections.add(headWordSection);
        innerWord.addView(headWordSection);

        ExpandableSection audioSection = null;
        if (word.hasAudio()) {
            LinearLayout audioContainer = new LinearLayout(context);
            audioContainer.setGravity(Gravity.CENTER_VERTICAL);
            TextView pronunciationTextView = new TextView(context);
            pronunciationTextView.setTextIsSelectable(true);
            pronunciationTextView.setText(Helper.toPinyin(word.getPronunciation()));
            pronunciationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            audioContainer.addView(pronunciationTextView);

            final MaterialIconView audioIcon = new MaterialIconView(context);
            audioIcon.setIcon(MaterialDrawableBuilder.IconValue.MUSIC_NOTE);
            audioIcon.setToActionbarSize();
            audioIcon.setSizeDp(30);
            audioIcon.setPadding(Helper.dpiToPixels(context, 12),
                    0, 0, 0);
            audioIcon.setColorResource(R.color.primary_color);
            audioContainer.addView(audioIcon);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 24),
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 24)
            );

            String myUri = word.getAudioURI();
            try{

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(myUri);
                mediaPlayer.prepare();
            }catch(IOException e){
                e.printStackTrace();
            }

            audioContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YoYo.with(Techniques.Pulse)
                            .duration(500)
                            .repeat(2)
                            .playOn(audioIcon);
                    mediaPlayer.start();
                }
            });

            audioSection = new ExpandableSection(context, "Pronunciation",  audioContainer, false);
            innerWord.addView(audioSection);
            sections.add(audioSection);
        } else {
            audioSection = new ExpandableSection(context, "Pronunciation", Helper.toPinyin(word.getPronunciation()), false, 30);
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
            measureWordsSection = new ExpandableSection(context, "Measure words", Helper.join(", ", word.getMeasures()), false, 20);
            innerWord.addView(measureWordsSection);
            sections.add(measureWordsSection);
        }

        ExpandableSection examplesSection = null;
        if ((word.getExamples() != null) && (word.getExamples().length > 0)) {
            final String[] shuffledExamples = Helper.shuffleStringArray(word.getExamples(true));

            LinearLayout examplesContent = new LinearLayout(context);
            examplesContent.setOrientation(LinearLayout.VERTICAL);

            final TextSwitcher textSwitcher = new TextSwitcher(context);

            textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    final TextView textViewItem = new TextView(context);
                    textViewItem.setTextIsSelectable(true);
                    textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    return textViewItem;
                }
            });

            textSwitcher.setText(shuffledExamples[0]);

            ExamplesNavigation exampleNavigator = new ExamplesNavigation(context);
            exampleNavigator.setPageNumber(word.getExamples().length);

            exampleNavigator.setOnPageChangeRequest(new OnboardingNavigator.OnPageChangeRequest() {
                @Override
                public void changeToPage(int index) {
                    if (index > examplePreviousIndex) {
                        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right));
                        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
                    } else {
                        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
                        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right));
                    }
                    examplePreviousIndex = index;
                    textSwitcher.setText(shuffledExamples[index]);
                }
            });

            examplesContent.addView(textSwitcher);
            examplesContent.addView(exampleNavigator);

//            LinearLayout examplesListView = createList(context, word.getExamples(true), 20, 8);
            examplesSection = new ExpandableSection(context, "Examples", examplesContent, false);
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
            collapseWord(word);
        }
    }
}
