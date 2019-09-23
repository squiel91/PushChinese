package com.example.android.tian_tian.fragments;


import android.content.Intent;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.activities.AddNewWord;
import com.example.android.tian_tian.data.DictionaryEntries;
import com.example.android.tian_tian.utilities.Helper;
import com.example.android.tian_tian.utilities.SwitchButton;
import com.example.android.tian_tian.utilities.WordAdapter;
import com.example.android.tian_tian.others.WordSelectedEvent;
import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.data.PushDbHelper;
import com.example.android.tian_tian.entities.Word;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.example.android.tian_tian.utilities.Helper.withoutNumbersAndSpaces;

/**
 * A simple {@link Fragment} subclass.
 */
public class VocabularyFragment extends Fragment {

    public interface KeboardHandler {
        void openKeyboard(EditText view);
        void closeKeyboard();
    }

    public interface Listener {
        public void onWordSelected(Word word);
    }

    private void showEmptyView(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void showNoResults(boolean show) {
        if (show) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
        }

    }

    SwitchButton toLearnFilterItem;
    SwitchButton learningFilterItem;
    SwitchButton reviewingFilterItem;
    View emptyView;
    boolean search = false;
    ListView listView;
    MediaPlayer mediaPlayer;
    FloatingActionButton scrollToTopButton;
    boolean scrollToTopButtonHide = true;

    boolean closingSearch = false;
    WordAdapter itemsAdapter;


    String[] projection = null;

    EditText searchInput;
    View noResults;
    FloatingActionButton addNewWordButton;

    float lastTouchDownX;

    public VocabularyFragment() {
        // Required empty public constructor
    }

    private void showScrollToTopButton(boolean show) {
        int transitionTime = 400;
        if (show) {
            if (scrollToTopButtonHide) {
                scrollToTopButtonHide = false;
                ((View) scrollToTopButton).setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp).duration(transitionTime).playOn(scrollToTopButton);
            }
        } else {
            if (!scrollToTopButtonHide) {
                scrollToTopButtonHide = true;
                YoYo.with(Techniques.SlideOutDown).duration(transitionTime).playOn(scrollToTopButton);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View rootView = inflater.inflate(R.layout.word_list, container, false);
        noResults = rootView.findViewById(R.id.no_results);
        listView = (ListView) rootView.findViewById(R.id.word_list_container);


        emptyView = rootView.findViewById(R.id.empty_screen);

        addNewWordButton = rootView.findViewById(R.id.add_word);
        addNewWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddNewWord.class));
            }
        });

        Cursor cursor = getCursor();
        showEmptyView(cursor.getCount() == 0);

        itemsAdapter = new WordAdapter(getContext(), cursor);

        itemsAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            public Cursor runQuery(CharSequence querry) {
                Cursor queryCursor;
                PushDbHelper pushDbHelper = new PushDbHelper(getContext());
                if (querry != null) {
                    Cursor userDeckCursor = getContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, projection,
                             "INSTR(" + PushDbContract.Vocabulary.COLUMN_HEAD_WORD + ", ?) > 0 OR INSTR(" +
                                     PushDbContract.Vocabulary.COLUMN_TRANSLATION + ", ?) > 0 OR INSTR(" +
                                     PushDbContract.Vocabulary.COLUMN_PRONUNCIATION_SEARCHABLE + ", ?) > 0"
                            , new String[] {
                                    querry.toString(),
                                    querry.toString(),
                                    withoutNumbersAndSpaces(querry.toString())
                            }, PushDbContract.Vocabulary.COLUMN_ID + " DESC");

                    final DictionaryEntries dictionaryEntries = new DictionaryEntries(getContext());
                    Cursor dictionaryCursor = dictionaryEntries.getCursor(querry.toString());
                    Cursor[] cursorList = new Cursor[2];
                    cursorList[0] = userDeckCursor;
                    cursorList[1] = dictionaryCursor;

                    queryCursor = new MergeCursor(cursorList);
                    // querry the other database and merge cursors
                } else {
                    boolean atLeastOneActive = false;
                    String selectString = "";
                    if (toLearnFilterItem.getActive()) {
                        selectString = "(" + PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " == " + Word.TO_PRESENT + ")";
                        atLeastOneActive = true;
                    }
                    if (learningFilterItem.getActive()) {
                        if (atLeastOneActive) {
                            selectString += " OR ";
                        }
                        selectString += "(" + PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " > " + Word.TO_PRESENT +
                                " AND " + PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " < " + Word.LEARNED + ")";
                        atLeastOneActive = true;
                    }
                    if (reviewingFilterItem.getActive()) {
                        if (atLeastOneActive) {
                            selectString += " OR ";
                        }
                        selectString += "(" + PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " >= " + Word.LEARNED + ")";
                        atLeastOneActive = true;
                    }
                    queryCursor = getContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, projection,
                                selectString, null, PushDbContract.Vocabulary.COLUMN_ID + " DESC");
                }
                final int cursorQty = queryCursor.getCount();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (noResults != null) {
                            showNoResults(cursorQty == 0);
                        } else {
                            Log.w("noResults", "Not found");
                        }
                    }
                });
                return queryCursor;
            }
        });
        final ConstraintLayout headerView = (ConstraintLayout) inflater.inflate(R.layout.vocabulary_list_header, null);

        final TextView toLearnFilterItemView = (TextView) headerView.findViewById(R.id.to_learn_filter_item);
        toLearnFilterItem = new SwitchButton(getContext(), toLearnFilterItemView, false);
        toLearnFilterItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toLearnFilterItem.toggle();
                itemsAdapter.getFilter().filter(null);
            }
        });

        final TextView learningFilterItemView = (TextView) headerView.findViewById(R.id.learning_filter_item);
        learningFilterItem = new SwitchButton(getContext(), learningFilterItemView, false);
        learningFilterItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                learningFilterItem.toggle();
                itemsAdapter.getFilter().filter(null);
            }
        });

        final TextView reviewingFilterItemView = (TextView) headerView.findViewById(R.id.reviewing_filter_item);
        reviewingFilterItem = new SwitchButton(getContext(), reviewingFilterItemView, false);
        reviewingFilterItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewingFilterItem.toggle();
                itemsAdapter.getFilter().filter(null);
            }
        });

        scrollToTopButton = (FloatingActionButton) rootView.findViewById(R.id.scroll_to_top);
        scrollToTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listView.getFirstVisiblePosition() > 10) {
                    listView.setSelection(9);
                };
                listView.smoothScrollToPosition(0);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > 5) showScrollToTopButton(true);
                else showScrollToTopButton(false);
            }
        });

        final View cancelSearchIcon = headerView.findViewById(R.id.cancel_search_icon);
        cancelSearchIcon.setVisibility(View.GONE);
        searchInput = headerView.findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                itemsAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        final View searchIcon = headerView.findViewById(R.id.search_icon);
        final View searchLabel = headerView.findViewById(R.id.search_label);
        final View filterButton = headerView.findViewById(R.id.filter_vocabulary);
        final View filterLabels = headerView.findViewById(R.id.filter_labels);

        headerView.findViewById(R.id.search_vocabulary).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // save the X,Y coordinates
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownX = motionEvent.getX();
                }

                // let the touch event pass on to whoever needs it
                return false;
            }
        });
        headerView.findViewById(R.id.search_vocabulary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!search) {
                    if (lastTouchDownX < (view.getWidth() / 2)) {
                        searchInput.setVisibility(View.VISIBLE);
                    } else {
                        filterLabels.setVisibility(View.VISIBLE);
                    }
                    filterButton.setVisibility(View.GONE);
                    searchIcon.setVisibility(View.GONE);
                    searchLabel.setVisibility(View.GONE);
                    cancelSearchIcon.setVisibility(View.VISIBLE);
                    searchInput.setFocusableInTouchMode(true);
                    searchInput.requestFocus();
                    ((KeboardHandler) getContext()).openKeyboard(searchInput);
                    search = true;
                } else {
                    toLearnFilterItem.setUnselected();
                    learningFilterItem.setUnselected();
                    reviewingFilterItem.setUnselected();

                    searchInput.setText("");
                    itemsAdapter.getFilter().filter("");
                    cancelSearchIcon.setVisibility(View.GONE);
                    filterLabels.setVisibility(View.GONE);
                    searchInput.setVisibility(View.GONE);
                    searchLabel.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.VISIBLE);
                    filterButton.setVisibility(View.VISIBLE);
                    ((KeboardHandler) getContext()).closeKeyboard();
                    search = false;
                }
            }
        });

        listView.addHeaderView(headerView);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View clickedItem, int position, long l) {
//                Cursor audio_cursor = getCursor(l);
//                audio_cursor.moveToFirst();
//                Word clickedWord = Word.from_cursor(audio_cursor);
//
//                EventBus.getDefault().post(new WordSelectedEvent(clickedWord));
//                ((Listener) getActivity()).onWordSelected(clickedWord);
//            }
//        });

        listView.setAdapter(itemsAdapter);
        return rootView;
    }



    private Cursor getCursor(long id) {
        PushDbHelper pushDbHelper = new PushDbHelper(getContext());
        Cursor cursor = getContext().getContentResolver().query(
                PushDbContract.Vocabulary.CONTENT_URI,
                projection,
                PushDbContract.Vocabulary.COLUMN_ID + "= ? ",
                new String[] { String.valueOf(id) }, null);
        return cursor;

    }

    private Cursor getCursor() {
        PushDbHelper pushDbHelper = new PushDbHelper(getContext());
        Cursor cursor = getContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, projection,
                null, null,
                PushDbContract.Vocabulary.COLUMN_ID + " DESC");
        return cursor;
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Subscribe
    public void wordsEdited(Float dummy) {
        wordsChanged(0);
    }

    @Subscribe
    public void wordsChanged(Integer dummy) {
        itemsAdapter.getCursor().close();
        Cursor cursor = getCursor();
        itemsAdapter.swapCursor(cursor);
        showEmptyView(cursor.getCount() == 0);
    }


}