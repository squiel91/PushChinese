package com.example.android.push_chinese;


import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeFragment extends Fragment {

    ContentResolver resolver;
    View rootView;
    Word currentWord;
    FloatingActionsMenu revele;
    WordPracticeBoard wordPracticeBoard;
    SRScheduler scheduler;
    SharedPreferences preferences;

    LinearLayout word_container;
    public PracticeFragment() {
        // Required empty public constructor
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        rootView = inflater.inflate(R.layout.practice, container, false);
        resolver = getContext().getContentResolver();
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();

        word_container = rootView.findViewById(R.id.word_container);

        // TODO: need to hide this buttons when run out of words
        rootView.findViewById(R.id.bury_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buryWord();
                revele.collapse();
            }
        });

        rootView.findViewById(R.id.skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                practiceAnotherWord();
                revele.collapse();
            }
        });

        revele = (FloatingActionsMenu) rootView.findViewById(R.id.multiple_actions);
        revele.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                // TODO: expand all the word sections
                Log.w("MENU OPENED", "TODO: expand all the word sections");
            }

            @Override
            public void onMenuCollapsed() {

            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_easy)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.rescheduleWord(currentWord, Word.EASY);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_normal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.rescheduleWord(currentWord, Word.NORMAL);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_hard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.rescheduleWord(currentWord, Word.HARD);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        wordPracticeBoard = new WordPracticeBoard(getContext(), word_container);

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

    private void buryWord() {
        if (currentWord != null) {
            currentWord.bury(true);
            currentWord.persist(getContext());
            practiceAnotherWord();
        }
    }

    private void practiceAnotherWord() {
        currentWord = null;
        practiceWord(scheduler.nextStudyWord(), true);
    }

    public void practiceWord(Word word, boolean animated) {
        if (word != null) {
            currentWord = word;
            revele.setEnabled(true);
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit().putLong("practicingWordId",currentWord.getId()).apply();
            wordPracticeBoard.changeWord(currentWord, animated);
        } else {
            wordPracticeBoard.emptyPractice();
            revele.setEnabled(false);
            preferences.edit().remove("practicingWordId").apply();

        }
    }
}
