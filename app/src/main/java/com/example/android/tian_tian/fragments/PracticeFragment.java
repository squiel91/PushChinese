package com.example.android.tian_tian.fragments;


import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.android.tian_tian.BusEvents.StatsUpdate;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.activities.AddNewWord;
import com.example.android.tian_tian.expandable_section.ExpandableSection;
import com.example.android.tian_tian.utilities.FloatingMenu;
import com.example.android.tian_tian.utilities.Helper;
import com.example.android.tian_tian.utilities.SRScheduler;
import com.example.android.tian_tian.utilities.WordPracticeBoard;
import com.example.android.tian_tian.others.WordSelectedEvent;
import com.example.android.tian_tian.entities.Word;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeFragment extends Fragment {

    public interface OnWordChanged {
        public void wordChanged();
    }

    boolean isFirstOne = true; // To sole the bug that the first transition is wrong
    ContentResolver resolver;
    View rootView;
    Word currentWord;
    ScrollView practiceScrollView;
    FloatingMenu floatingMenu;
    SRScheduler scheduler;
    SharedPreferences preferences;

    View noActionContainer;
    View optionsPanel;
    View editButton;
    View skipButton;
    View buryButton;

    WordPracticeBoard wordCardFront = null;
    WordPracticeBoard wordCardBack = null;

    @Subscribe
    public void onWordSelectedEvent(WordSelectedEvent event) {
        Log.w("onWordSelectedEvent", event.getSelectedWord().getHeadWord());
        practiceWord(event.getSelectedWord(), false);
    };

    public void openContextualMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v, Gravity.TOP);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener)this);
        popup.inflate(R.menu.practice_contextual_menu);
        popup.show();
    }

    private View init(View rootView) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        resolver = getContext().getContentResolver();
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();

        noActionContainer = rootView.findViewById(R.id.no_action_container);
                wordCardFront = new WordPracticeBoard(getContext(), (CardView) rootView.findViewById(R.id.word_card_front), getActivity().getSupportFragmentManager());
        wordCardBack = new WordPracticeBoard(getContext(), (CardView) rootView.findViewById(R.id.word_card_back), getActivity().getSupportFragmentManager());

        optionsPanel = rootView.findViewById(R.id.word_options_panel);
        editButton = rootView.findViewById(R.id.edit_button);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddNewWord.class);
                intent.putExtra("Word", currentWord);
                startActivity(intent);
            }
        });

        buryButton = rootView.findViewById(R.id.bury_button);
        buryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete this word?")
                        .setMessage(currentWord.getHeadWord()+ " will disappear from your vocabulary and you won't see it again")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteWord();
                                floatingMenu.collapse();
                            }})
                        .setNegativeButton("Cancel", null).show()
                        .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor( getResources().getColor(R.color.color_red));
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
        practiceScrollView = (ScrollView) rootView.findViewById(R.id.practiceScrollView);

        floatingMenu.setOnExpandedListener(new FloatingMenu.OnExpandedListener() {
            @Override
            public void onExpanded() {
                if (isFirstOne) wordCardFront.unlockSections();
                else wordCardBack.unlockSections();

                floatingMenu.setState(FloatingMenu.STATE.RATE);
            }
        });

        floatingMenu.setOnSkipedListener(new FloatingMenu.OnSkipedListener() {
            @Override
            public void onSkiped() {
//                  This would be more neat but is not being called
//                practiceScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//                    @Override
//                    public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//                        if (i == 0) {
//                            practiceAnotherWord();
//                        }
//                    }
//                });
                practiceScrollView.smoothScrollTo(0,0);
                CountDownTimer timer = new CountDownTimer(500, 100) {
                        public void onTick(long millisUntilFinished) {}

                        public void onFinish() { practiceAnotherWord(); }
                    }.start();
                practiceScrollView.setOnScrollChangeListener(null);
            }
        });

        floatingMenu.setOnDifficultySelectedListener(new FloatingMenu.OnDifficultySelectedListener() {
            @Override
            public void onDifficultySelectedDiffered(int difficulty) {
                scheduler.rescheduleWord(currentWord, difficulty);
                practiceAnotherWord();
            }

            @Override
            public void onDifficultySelectedImmediate(int difficulty) {
                practiceScrollView.smoothScrollTo(0,0);
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
        practiceWord(practicingWord, false);
        // TODO: pulling to check for new words when finished practicing
        return rootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.practice, container, false);
        return init(rootView);
    }

    private void deleteWord() {
        if (currentWord != null) {
            currentWord.delete(getContext());
            File audioFile = new File(currentWord.getAudioURI());
            if (audioFile.exists()) audioFile.delete();
            File imageFile = new File(currentWord.getImageURI());
            if (imageFile.exists()) imageFile.delete();
            EventBus.getDefault().post(new Integer(0));
            EventBus.getDefault().post(new StatsUpdate());
            practiceAnotherWord();
        }
    }

    private void practiceAnotherWord() {
            practiceWord(scheduler.nextStudyWord(), true);
    }

    public void practiceWord(Word word, boolean animated) {
        if (word != null) {
            noActionContainer.setVisibility(View.GONE);
            ((OnWordChanged)getActivity()).wordChanged(); // to clear the board

            optionsPanel.setVisibility(View.VISIBLE);
            floatingMenu.show();
            floatingMenu.collapse(false);
            boolean isNotTimeToPractice = false;
            if (word.getStage() == Word.TO_PRESENT) floatingMenu.setState(FloatingMenu.STATE.NEW_WORD);
            else {
                if (word.getScheduledTo() <= Helper.daysSinceEpoch()) floatingMenu.setState(FloatingMenu.STATE.EXPAND);
                else  {
                    floatingMenu.setState(FloatingMenu.STATE.SKIP);
                    isNotTimeToPractice = true;
                }
            }
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit().putLong("practicingWordId",word.getId()).apply();

            currentWord = word;
            if (animated) {
                wordCardBack.show();
                if (isFirstOne) {
                    wordCardBack.changeWord(currentWord, false, isNotTimeToPractice);
                    wordCardFront.exitScreen(null);
                } else {
                    wordCardFront.show();
                    LinearLayout container = wordCardBack.getContainer();
                    wordCardFront.setContainer(container);
                    wordCardBack.changeWord(currentWord, false, isNotTimeToPractice);
                    wordCardFront.exitScreen(null);
                }
                isFirstOne = false;
            } else {
                isFirstOne = true;
                wordCardFront.show();
                wordCardFront.changeWord(currentWord, false, isNotTimeToPractice);
                wordCardBack.hide();
            }
        } else {
            noActionContainer.setVisibility(View.VISIBLE);
            currentWord = null;
            optionsPanel.setVisibility(View.INVISIBLE);
            if (isFirstOne) wordCardFront.exitScreen(null);
            else wordCardBack.exitScreen(null);
            floatingMenu.hide();
            preferences.edit().remove("practicingWordId").apply();
            isFirstOne = true;
            setNewDayPoolingTimer();
        }
    }

    @Subscribe
    public void wordsEdited(Float dummy) {
        Log.w("REFRESH", "deataching");
        Picasso.get().invalidate(new File(currentWord.getImageURI()));
        init(rootView);
    }

    private void newDayPooling() {
        if (scheduler.checkForNewDay()) {
            if (newDayPoolingTimer != null)
                newDayPoolingTimer.cancel();
            practiceAnotherWord();
        }
    }

    private CountDownTimer newDayPoolingTimer = null;

    private CountDownTimer setNewDayPoolingTimer() {
        newDayPoolingTimer = new CountDownTimer(1 * 30 * 24  * 60 * 60 * 1000, 10 * 1000) {
            public void onTick(long millisUntilFinished) {
                newDayPooling();
            }

            public void onFinish() {}
        }.start();
        return newDayPoolingTimer;

    }
}
