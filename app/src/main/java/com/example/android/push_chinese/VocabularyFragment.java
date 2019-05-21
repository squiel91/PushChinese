package com.example.android.push_chinese;


import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.push_chinese.data.PushDbContract;
import com.example.android.push_chinese.data.PushDbHelper;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class VocabularyFragment extends Fragment {

    interface Listener {
        public void onWordSelected(Word word);
    }

    MediaPlayer mediaPlayer;

    String[] projection = null;

    public VocabularyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.word_list, container, false);

        WordAdapter itemsAdapter = new WordAdapter(getContext(), getCursor());
        ListView listView = (ListView) rootView.findViewById(R.id.word_list_container);

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