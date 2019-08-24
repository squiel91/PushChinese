package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.onboarding.OnboardingNavigator;

import net.steamcrafted.materialiconlib.MaterialIconView;

public class ExamplesNavigation extends LinearLayout {
    public interface OnPageChangeRequest {
        void changeToPage(int index);
    }

    Context context;
    int pageNumber;
    int totalPageNumber;
    MaterialIconView buttonPrevious;
    MaterialIconView buttonNext;
    LinearLayout progressBottom;
    LinearLayout progressTop;
    View[] bulletList;
    OnboardingNavigator.OnPageChangeRequest onPageChangeRequest = null;

    public void setOnPageChangeRequest(OnboardingNavigator.OnPageChangeRequest onPageChangeRequest) {
        this.onPageChangeRequest = onPageChangeRequest;
    }

    private void previousButtonState(boolean active) {
        if (active) {
            buttonPrevious.setColorResource(R.color.primary_color);
        } else {
            buttonPrevious.setColorResource(R.color.grayish);
        }
    }

    private void nextButtonState(boolean active) {
        if (active) {
            buttonNext.setColorResource(R.color.primary_color);
        } else {
            buttonNext.setColorResource(R.color.grayish);
        }
    }

    public void nextPage(boolean withCallback) {
        if (pageNumber < totalPageNumber - 1) {
            View previousBullet = bulletList[pageNumber];
            YoYo.with(Techniques.TakingOff).duration(400).playOn(previousBullet);
            pageNumber ++;
            View bullet = bulletList[pageNumber];
            bullet.setVisibility(VISIBLE);
            YoYo.with(Techniques.Landing).duration(400).playOn(bullet);
            if (withCallback && onPageChangeRequest != null) {
                onPageChangeRequest.changeToPage(pageNumber);
            }
        }
        nextButtonState(pageNumber < totalPageNumber - 1);
        previousButtonState(pageNumber > 0);

    }

    public void previousPage(boolean withCallback) {
        if (pageNumber > 0) {
            View previousBullet = bulletList[pageNumber];
            YoYo.with(Techniques.TakingOff).duration(400).playOn(previousBullet);
            pageNumber --;
            View bullet = bulletList[pageNumber];
            YoYo.with(Techniques.Landing).duration(400).playOn(bullet);
            if (withCallback && onPageChangeRequest != null) {
                onPageChangeRequest.changeToPage(pageNumber);
            }
        }
        previousButtonState(pageNumber > 0);
        nextButtonState(pageNumber < totalPageNumber - 1);

    }

    public ExamplesNavigation(Context context) {
        super(context);
        init(context);
    }

    public ExamplesNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExamplesNavigation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflator = LayoutInflater.from(context);
        inflator.inflate(R.layout.examples_navigator, this, true);
        buttonPrevious = findViewById(R.id.button_previous);
        buttonPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).playOn(view);
                previousPage(true);
            }
        });
        buttonNext = findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).playOn(view);
                nextPage(true);
            }
        });

        progressBottom = findViewById(R.id.progress_bottom);
        progressTop = findViewById(R.id.progress_top);
    }

    public void setPageNumber(int number) {
        totalPageNumber = number;
        bulletList = new View[number];

        for (int i = 0; i < totalPageNumber; i++) {
            View placeholder = new View(context);
            progressBottom.addView(placeholder);
            LayoutParams params = new LayoutParams(
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_SP, 8),
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_SP, 8)
            );
            params.setMargins(
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_SP, 8),
                    0,
                    Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_SP, 8),
                    0
            );
//            placeholder.setBackgroundColor(getResources().getColor(R.color.grayish));
            placeholder.setBackground(getResources().getDrawable(R.drawable.onboarding_navigation_placeholder));
            placeholder.setLayoutParams(params);

            View bullet =  new View(context);
            progressTop.addView(bullet);
            bullet.setLayoutParams(params);
            bullet.setBackground(getResources().getDrawable(R.drawable.onboarding_navigation_bullet));
            bullet.setVisibility(INVISIBLE);
            bulletList[i] = bullet;
        }
        pageNumber = 0;
        bulletList[0].setVisibility(VISIBLE);
        previousButtonState(false);
        nextButtonState(number > 1);
    }
}
