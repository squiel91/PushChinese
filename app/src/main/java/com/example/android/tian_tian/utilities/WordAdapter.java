package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.database.Cursor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;

import com.example.android.tian_tian.R;
import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.entities.Word;
import com.example.android.tian_tian.fragments.VocabularyFragment;
import com.example.android.tian_tian.others.WordSelectedEvent;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class WordAdapter extends CursorAdapter {

    public WordAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    private View deckView(Context context, boolean isUserWord, View item) {
        CardView baseCard = item.findViewById(R.id.word_card);
        LinearLayout innerView = item.findViewById(R.id.inner_view);
        if (isUserWord) {
            baseCard.setElevation(Helper.dpiToPixels(context, 2));
            baseCard.setBackgroundColor(context.getResources().getColor(R.color.white));
            innerView.setBackground(null);
        } else {
            baseCard.setElevation(0);
            baseCard.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            innerView.setBackground(context.getResources().getDrawable(R.drawable.dotted));
        }
        return item;
    }

    @Override
    public void bindView(View item, final Context context, Cursor cursor) {
        final Word word = Word.from_cursor(cursor);
        deckView(context, cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_LEVEL) >= 0, item);
        ((TextView)item.findViewById(R.id.head_word_text_view)).setText(word.getHeadWord());
        ((TextView)item.findViewById(R.id.translation_text_view)).setText(Helper.join(", ", word.getTranslations()));
        TextView pronunciationTextView = (TextView) item.findViewById(R.id.pronunciation_text_view);
        if (word.hasPronunciation()) {
//            pronunciationTextView.setText("" + word.getId());
            pronunciationTextView.setText(Helper.toPinyin(word.getPronunciation()));
            pronunciationTextView.setVisibility(View.VISIBLE);

        } else {
            pronunciationTextView.setVisibility(View.GONE);
        };

        boolean hasImage = word.hasImage();
        ImageView descriptiveImageView = (ImageView) item.findViewById(R.id.descriptive_image_view);
        if (hasImage) {
            Picasso.get().load(new File(word.getImageURI())).into(descriptiveImageView);
            descriptiveImageView.setVisibility(View.VISIBLE);
        } else {
            descriptiveImageView.setVisibility(View.GONE);
        };

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new WordSelectedEvent(word));
                ((VocabularyFragment.Listener) context).onWordSelected(word);
            }
        });
    }
}
