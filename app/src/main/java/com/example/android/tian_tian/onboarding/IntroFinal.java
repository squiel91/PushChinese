package com.example.android.tian_tian.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.android.tian_tian.R;

public class IntroFinal extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.onboarding_final, container, false);
//        rootView.findViewById(R.id.finish_onboarding).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ((NotifyChange) getContext()).finishOnboarding();
//            }
//        });
        return rootView;
    }
}
