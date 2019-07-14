package com.example.android.everytian.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.everytian.R;
import com.example.android.everytian.activities.AddNewWord;
import com.example.android.everytian.activities.OpenSource;
import com.example.android.everytian.activities.SettingsActivity;
import com.example.android.everytian.utilities.WordAdapter;
import com.example.android.everytian.others.WordSelectedEvent;
import com.example.android.everytian.data.PushDbContract;
import com.example.android.everytian.data.PushDbHelper;
import com.example.android.everytian.entities.Word;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;

import static com.example.android.everytian.utilities.Helper.withoutNumbersAndSpaces;

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

    boolean search = false;
    MediaPlayer mediaPlayer;
    FloatingActionButton scrollToTopButton;
    boolean scrollToTopButtonHide = true;

    boolean closingSearch = false;
    WordAdapter itemsAdapter;


    String[] projection = null;

    EditText searchInput;
    View noResults;
    FloatingActionButton addNewWordButton;

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
        View rootView = inflater.inflate(R.layout.word_list, container, false);
        noResults = rootView.findViewById(R.id.no_results);

        addNewWordButton = rootView.findViewById(R.id.add_word);
        addNewWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddNewWord.class));
            }
        });

        itemsAdapter = new WordAdapter(getContext(), getCursor());
        itemsAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            public Cursor runQuery(CharSequence querry) {
                Cursor queryCursor;
                Log.d("runQuerry", "runQuery constraint:"+querry);
                PushDbHelper pushDbHelper = new PushDbHelper(getContext());
                if (querry != null) {
                    queryCursor = getContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, projection,
                             "INSTR(" + PushDbContract.Vocabulary.COLUMN_HEAD_WORD + ", ?) > 0 OR INSTR(" +
                                     PushDbContract.Vocabulary.COLUMN_TRANSLATION + ", ?) > 0 OR INSTR(" +
                                     PushDbContract.Vocabulary.COLUMN_PRONUNCIATION_SEARCHABLE + ", ?) > 0"
                            , new String[] {
                                    querry.toString(),
                                    querry.toString(),
                                    withoutNumbersAndSpaces(querry.toString())
                            }, null);
                } else {
                    queryCursor = getCursor();
                }
                final int cursorQty = queryCursor.getCount();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (noResults != null) {
                            if (cursorQty > 0) {
                                noResults.setVisibility(View.GONE);
                            } else {
                                noResults.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.w("noResults", "Not found");
                        }
                    }
                });
                return queryCursor;
            }
        });
        final ListView listView = (ListView) rootView.findViewById(R.id.word_list_container);
        final ConstraintLayout headerView = (ConstraintLayout) inflater.inflate(R.layout.vocabulary_list_header, null);

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
                if (charSequence != null && !charSequence.toString().trim().isEmpty()) {
//                    Toast.makeText(getContext(), charSequence + "|Hola", Toast.LENGTH_SHORT).show();
                    itemsAdapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        final View searchIcon = headerView.findViewById(R.id.search_icon);
        final View searchLabel = headerView.findViewById(R.id.search_label);

        headerView.findViewById(R.id.search_vocabulary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!search) {
                    searchIcon.setVisibility(View.GONE);
                    searchLabel.setVisibility(View.GONE);
                    searchInput.setVisibility(View.VISIBLE);
                    cancelSearchIcon.setVisibility(View.VISIBLE);
                    searchInput.setFocusableInTouchMode(true);
                    searchInput.requestFocus();
                    ((KeboardHandler) getContext()).openKeyboard(searchInput);
                    search = true;
                } else {
                    searchInput.setText("");
                    itemsAdapter.getFilter().filter(null);
                    cancelSearchIcon.setVisibility(View.GONE);
                    searchInput.setVisibility(View.GONE);
                    searchLabel.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.VISIBLE);
                    ((KeboardHandler) getContext()).closeKeyboard();
                    search = false;
                }
            }
        });

        listView.addHeaderView(headerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View clickedItem, int position, long l) {
                Cursor audio_cursor = getCursor(l);
                audio_cursor.moveToFirst();
                Word clickedWord = Word.from_cursor(audio_cursor);

                EventBus.getDefault().post(new WordSelectedEvent(clickedWord));
                ((Listener) getActivity()).onWordSelected(clickedWord);
            }
        });

        listView.setAdapter(itemsAdapter);

        return rootView;
    }

    private Cursor getCursor(long id) {
        PushDbHelper pushDbHelper = new PushDbHelper(getContext());


        return getContext().getContentResolver().query(
                PushDbContract.Vocabulary.CONTENT_URI,
                projection,
                PushDbContract.Vocabulary.COLUMN_ID + "= ? ",
                new String[] { String.valueOf(id) },
                null);
    }

    private Cursor getCursor() {
        PushDbHelper pushDbHelper = new PushDbHelper(getContext());
        return getContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI, projection, null, null, null);
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
}