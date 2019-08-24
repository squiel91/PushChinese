package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.database.Cursor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.example.android.tian_tian.R;
import com.example.android.tian_tian.entities.Word;
import com.squareup.picasso.Picasso;

import java.io.File;

public class WordAdapter extends CursorAdapter {

    public WordAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View item, Context context, Cursor cursor) {
        Word word = Word.from_cursor(cursor);
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
    }
}
