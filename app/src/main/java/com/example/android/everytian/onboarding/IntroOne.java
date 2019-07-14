package com.example.android.everytian.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.everytian.R;

public class IntroOne extends Fragment {

    View rootView;
    int imageId;
    String title;
    String description;

    IntroOne(int imageId, String title, String description) {
        this.imageId = imageId;
        this.title = title;
        this.description = description;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.onboarding_one, container, false);
        ((ImageView) rootView.findViewById(R.id.main_image)).setImageResource(imageId);
        ((TextView) rootView.findViewById(R.id.title)).setText(title);
        ((TextView) rootView.findViewById(R.id.description)).setText(description);

        return rootView;

    }

    void set(int imageId, String title, String description) {

    }
}
