package com.example.android.everytian.onboarding;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.everytian.R;
import com.example.android.everytian.fragments.PracticeFragment;
import com.example.android.everytian.fragments.StaticsFragment;
import com.example.android.everytian.fragments.VocabularyFragment;

public class OnboardingSliderAdaptor extends FragmentPagerAdapter {

    private String tabTitles[] = new String[] { "IntroOne", "IntroTwo", "IntroThree", "Final"};
    private Context context;

    Fragment practice;

    public OnboardingSliderAdaptor(FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    public Fragment getPractice() {
        return practice;
    }
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new IntroOne(
                    R.drawable.onboarding_one,
                    "MASTER HSK 1 VOCABULARY",
                    "Carefully curated content with audio, images and useful examples"
            );
        } else if (position == 1){
            return new IntroOne(
                    R.drawable.onboarding_two,
                    "LEARN MORE EFFICIENTLY",
                    "Unleash the power of Spaced Repetition to retain more words in less time"
            );
        } else if (position == 2){
            return new IntroOne(
                    R.drawable.onboarding_three,
                    "RECALL EVERY SINGLE DETAIL",
                    "With the folding card you will test the word from many different angles"
            );
        } else {
            return new IntroFinal();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return 4;
    }
}
