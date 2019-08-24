package com.example.android.tian_tian.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.android.tian_tian.R;
import com.example.android.tian_tian.activities.MainActivity;
import com.example.android.tian_tian.utilities.Helper;

public class Onboarding extends AppCompatActivity implements NotifyChange {

    private Integer studyWords = null;
    private Boolean traditional = null;

    @Override
    public void numberOfStudyWords(int quantity) {
        studyWords = quantity;
    }

    @Override
    public void studyTraditional(boolean traditional) {
        this.traditional = traditional;
    }

    OnboardingNavigator navigator;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.onboarding);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        OnboardingSliderAdaptor adapter = new OnboardingSliderAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        navigator = findViewById(R.id.navigation);
        navigator.setPageNumber(4);
        navigator.setOnPageChangeRequest(new OnboardingNavigator.OnPageChangeRequest() {
            @Override
            public void changeToPage(int index) {
                viewPager.setCurrentItem(index, true);
            }
        });

        navigator.setOnFinishOnboarding(new OnboardingNavigator.OnFinishOnboarding() {
            @Override
            public void onFinishOnboarding() {
                redirect();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                navigator.changeToPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void redirect() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("initializedDay", Helper.daysSinceEpoch());

        editor.apply();

        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(mainIntent);
    }
}