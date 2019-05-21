package com.example.android.push_chinese;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.push_chinese.expandable_section.ExpandableSection;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import static com.example.android.push_chinese.data.PushDbContract.Vocabulary;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    ContentResolver resolver;
    View rootView;
    Word currentWord;
    FloatingActionsMenu revele;
    WordPracticeBoard wordPracticeBoard;
    SRScheduler scheduler;

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
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.skip_word:
                practiceAnotherWord();
                revele.collapse();
                return true;
            case R.id.butty_word:
                buryWord();
                revele.collapse();
                return true;
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        rootView = inflater.inflate(R.layout.practice, container, false);
        resolver = getContext().getContentResolver();
        scheduler = new SRScheduler(getContext());

        word_container = rootView.findViewById(R.id.word_container);

        rootView.findViewById(R.id.contextualMenuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openContextualMenu(view);
            }
        });

        revele = (FloatingActionsMenu) rootView.findViewById(R.id.multiple_actions);
        revele.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {

            }

            @Override
            public void onMenuCollapsed() {

            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_easy)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.reschreduleWord(currentWord, SRScheduler.EASY);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_normal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.reschreduleWord(currentWord, SRScheduler.NORMAL);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        ((FloatingActionButton) rootView.findViewById(R.id.practice_hard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.reschreduleWord(currentWord, SRScheduler.HARD);
                practiceAnotherWord();
                revele.collapse();
            }
        });

        wordPracticeBoard = new WordPracticeBoard(getContext(), word_container);

        Word practicingWord;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
            ContentValues contentValues = new ContentValues();
            contentValues.put(Vocabulary.COLUMN_BURIED, Vocabulary.BURIED);
            resolver.update(Uri.parse(Vocabulary.CONTENT_URI + "/" + String.valueOf(currentWord.getId())),
                    contentValues,
                    null,
                    null);
            practiceAnotherWord();
        }
    }

    private void blankState() {
        currentWord = null;
        word_container.removeAllViews();
    }

    private void practiceAnotherWord() {
        currentWord = null;
        practiceWord(scheduler.nextStudyWord(), true);
    }

    public void practiceWord(Word word, boolean animated) {
        if (word != null) {
            currentWord = word;
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit().putLong("practicingWordId",currentWord.getId()).apply();
            wordPracticeBoard.changeWord(currentWord, animated);
        } else {
            // TODO: should change to empty view
            Toast.makeText(getContext(), "No more words to learn", Toast.LENGTH_SHORT).show();
            blankState();
        }
    }
}
