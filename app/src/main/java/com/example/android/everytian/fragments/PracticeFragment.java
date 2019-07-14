package com.example.android.everytian.fragments;


import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.everytian.R;
import com.example.android.everytian.expandable_section.ExpandableSection;
import com.example.android.everytian.utilities.FloatingMenu;
import com.example.android.everytian.utilities.SRScheduler;
import com.example.android.everytian.utilities.WordPracticeBoard;
import com.example.android.everytian.others.WordSelectedEvent;
import com.example.android.everytian.entities.Word;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeFragment extends Fragment {

    boolean isFirstOne = true; // To sole the bug that the first transition is wrong
    ContentResolver resolver;
    View rootView;
    Word currentWord;
    FloatingMenu floatingMenu;
    SRScheduler scheduler;
    SharedPreferences preferences;

    View skipButton;
    View burryButton;

    WordPracticeBoard wordCardFront = null;
    WordPracticeBoard wordCardBack = null;

    @Subscribe
    public void onWordSelectedEvent(WordSelectedEvent event) {
        Log.w("onWordSelectedEvent", event.getSelectedWord().getHeadWord());
        practiceWord(event.getSelectedWord(), false, true);
    };

    public void openContextualMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v, Gravity.TOP);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener)this);
        popup.inflate(R.menu.practice_contextual_menu);
        popup.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        rootView = inflater.inflate(R.layout.practice, container, false);
        resolver = getContext().getContentResolver();
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();

        wordCardFront = new WordPracticeBoard(getContext(), (CardView) rootView.findViewById(R.id.word_card_front));
        wordCardBack = new WordPracticeBoard(getContext(), (CardView) rootView.findViewById(R.id.word_card_back));

        // TODO: need to hide this buttons when run out of words
        burryButton = rootView.findViewById(R.id.bury_button);
        burryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buryWord();
                floatingMenu.collapse();
            }
        });

        skipButton = rootView.findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                practiceAnotherWord();
                floatingMenu.collapse();
            }
        });

        floatingMenu = (FloatingMenu) rootView.findViewById(R.id.floatingMenu);

        floatingMenu.setOnExpandedListener(new FloatingMenu.OnExpandedListener() {
            @Override
            public void onExpanded() {
                if (isFirstOne) wordCardFront.unlockSections();
                else wordCardBack.unlockSections();

                floatingMenu.setState(FloatingMenu.STATE.RATE);
            }
        });

        floatingMenu.setOnDifficultySelectedListener(new FloatingMenu.OnDifficultySelectedListener() {
            @Override
            public void onDifficultySelected(int difficulty) {
                scheduler.rescheduleWord(currentWord, difficulty);
                practiceAnotherWord();
            }
        });

        wordCardBack.setOnLockedClick(new ExpandableSection.OnLockedClick() {
            @Override
            public void OnLockedClick() {
                floatingMenu.callAttention();
            }
        });

        wordCardFront.setOnLockedClick(new ExpandableSection.OnLockedClick() {
            @Override
            public void OnLockedClick() {
                floatingMenu.callAttention();
            }
        });

        Word practicingWord;
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (preferences.contains("practicingWordId")) {
            Long practicingWordId = preferences.getLong("practicingWordId", 0);
            practicingWord = Word.from_id(getContext(), practicingWordId);
        } else {
            practicingWord = scheduler.nextStudyWord();
        }
        practiceWord(practicingWord, false, false);
        // TODO: pulling to check for new words when finished practicing
        return rootView;
    }

    private void buryWord() {
        if (currentWord != null) {
            currentWord.bury(true);
            currentWord.persist(getContext());
            practiceAnotherWord();
        }
    }

    private void practiceAnotherWord() {
        currentWord = null;
        practiceWord(scheduler.nextStudyWord(), true, false);
    }

    boolean noCardLeft = false;

    public void practiceWord(Word word, boolean animated, boolean stackIn) {
        if (word != null) {
            skipButton.setVisibility(View.VISIBLE);
            burryButton.setVisibility(View.VISIBLE);
            currentWord = word;
            floatingMenu.show();
            floatingMenu.collapse(false);
            if (currentWord.getStage() == Word.TO_PRESENT) floatingMenu.setState(FloatingMenu.STATE.NEW_WORD);
            else floatingMenu.setState(FloatingMenu.STATE.EXPAND);
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit().putLong("practicingWordId",currentWord.getId()).apply();
            if (stackIn) {
                if (!noCardLeft && isFirstOne) {
                    LinearLayout container = wordCardFront.getContainer();
                    wordCardBack.setContainer(container);
                    wordCardBack.show();
                }
                isFirstOne = true;
                wordCardFront.changeWord(currentWord, false);
                wordCardFront.enterScreen(new WordPracticeBoard.OnAnimationFinishes() {
                    @Override
                    public void onAnimationFinishes() {
                        wordCardBack.hide();
                    }
                });
            } else {
                if (animated) {
                    if (isFirstOne) {
                        wordCardBack.show();
                        wordCardBack.changeWord(currentWord, false);
                        wordCardFront.exitScreen(null);
                    } else {
                        wordCardBack.show();
                        LinearLayout container = wordCardBack.getContainer();
                        wordCardFront.setContainer(container);
                        wordCardBack.changeWord(currentWord, false);
                        wordCardFront.exitScreen(null);
                    }
                    isFirstOne = false;
                } else {
                    isFirstOne = true;
                    wordCardFront.show();
                    wordCardBack.changeWord(currentWord, false);
                    wordCardFront.changeWord(currentWord, false);
                    wordCardBack.hide();
                }
            }
            noCardLeft = false;
        } else {
            noCardLeft = true;
            skipButton.setVisibility(View.GONE);
            burryButton.setVisibility(View.GONE);
            if (isFirstOne) wordCardFront.exitScreen(null);
            else wordCardBack.exitScreen(null);
            floatingMenu.hide();
            preferences.edit().remove("practicingWordId").apply();
            isFirstOne = true;

        }
    }
}
