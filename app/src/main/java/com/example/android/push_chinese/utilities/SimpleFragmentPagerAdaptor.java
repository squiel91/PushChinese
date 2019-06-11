package com.example.android.push_chinese.utilities;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.push_chinese.fragments.PracticeFragment;
import com.example.android.push_chinese.fragments.StaticsFragment;
import com.example.android.push_chinese.fragments.VocabularyFragment;

public class SimpleFragmentPagerAdaptor extends FragmentPagerAdapter {

    private String tabTitles[] = new String[] { "Practice", "Vocabulary", "Stats"};
    private Context context;

    Fragment practice;

    public SimpleFragmentPagerAdaptor(FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    public Fragment getPractice() {
        return practice;
    }
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            practice = new PracticeFragment();
            return practice;
        } else if (position == 1){
            return new VocabularyFragment();
        } else {
            return new StaticsFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return 3;
    }
}