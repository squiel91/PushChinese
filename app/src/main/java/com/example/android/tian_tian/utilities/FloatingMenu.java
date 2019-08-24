package com.example.android.tian_tian.utilities;

import android.animation.Animator;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.entities.Word;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FloatingMenu extends LinearLayout {

    public interface OnDifficultySelectedListener {
        void onDifficultySelectedImmediate(int difficulty);
        void onDifficultySelectedDiffered(int difficulty);
    }

    public interface OnExpandedListener {
        void onExpanded();
    }

    public interface OnCollapsedListener {
        void onCollapsed();
    }

    public static enum STATE {
        NEW_WORD,
        EXPAND,
        RATE
    }

    boolean expanded = true;
    boolean onStage = false;
    STATE state;
    Context context;

    final boolean CLOSE_WHEN_OPTION_SELECTED = true;

    FloatingActionButton hardButton;
    FloatingActionButton normalButton;
    FloatingActionButton easyButton;
    FloatingActionButton mainButton;

    // Listeners
    OnDifficultySelectedListener onDifficultySelectedListener = null;
    OnExpandedListener onExpandedListener = null;
    OnCollapsedListener onCollapsedListener = null;

    public FloatingMenu(Context context) {
        super(context);
        this.context = context;
        initialize();
    }

    public FloatingMenu(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initialize();
    }

    public void initialize() {
        LayoutInflater inflator = LayoutInflater.from(context);

        this.setVisibility(View.GONE);

        inflator.inflate(R.layout.floating_menu, this, true);
        hardButton = findViewById(R.id.hard_button);
        normalButton = findViewById(R.id.normal_button);
        easyButton = findViewById(R.id.easy_button);
        mainButton = findViewById(R.id.main_action_button);

        easyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                optionSelected(Word.EASY, view);
            }
        });

        normalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                optionSelected(Word.NORMAL, view);
            }
        });

        hardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                optionSelected(Word.HARD, view);
            }
        });

        mainButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle(true);
            }
        });

        collapse(false);
        setState(STATE.NEW_WORD);
    }

    private void optionSelected(final int difficulty, View view) {
        if (onDifficultySelectedListener != null) onDifficultySelectedListener.onDifficultySelectedImmediate(difficulty);
        YoYo.with(Techniques.Pulse).duration(500).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                if (CLOSE_WHEN_OPTION_SELECTED) collapse(true);
                if (onDifficultySelectedListener != null) onDifficultySelectedListener.onDifficultySelectedDiffered(difficulty);
            }
        }).playOn(view);

    }

    public void setState(STATE newState) {
        if (this.state != newState) {
            this.state = newState;
            switch (newState) {
                case NEW_WORD:
                    mainButton.setImageResource(R.drawable.icon_learn_new);
                    break;
                case EXPAND:
                    mainButton.setImageResource(R.drawable.icon_revele);
                    break;
                case RATE:
                    mainButton.setImageResource(R.drawable.icon_rate);
            }
        }
    }

    public void setOnDifficultySelectedListener(OnDifficultySelectedListener onDifficultySelectedListener) {
        this.onDifficultySelectedListener = onDifficultySelectedListener;
    }

    public void setOnExpandedListener(OnExpandedListener onExpanededListener) {
        this.onExpandedListener = onExpanededListener;
    }

    public void setOnCollapsedListener(OnCollapsedListener setOnCollapsedListener) {
        this.onCollapsedListener = setOnCollapsedListener;
    }

    public void toggle(boolean animated) {
        YoYo.with(Techniques.Pulse).duration(100).playOn(mainButton);
        if (expanded) {
            collapse(animated);
        } else {
            expand(animated);
        }
    }

    public void expand() {
        expand(true);
    }

    public void expand(boolean animated) {
        ((View) hardButton).setVisibility(View.VISIBLE);
        ((View) normalButton).setVisibility(View.VISIBLE);
        ((View) easyButton).setVisibility(View.VISIBLE);
        if (animated) {
            YoYo.with(Techniques.BounceInUp).duration(400).playOn(hardButton);
            YoYo.with(Techniques.BounceInUp).duration(300).playOn(normalButton);
            YoYo.with(Techniques.BounceInUp).duration(200).playOn(easyButton);
        }

        expanded = true;
        if (onExpandedListener != null) onExpandedListener.onExpanded();
    }

    public void collapse() {
        collapse(true);
    }

    public void collapse(boolean animated) {

        if (animated) {
            YoYo.with(Techniques.SlideOutDown).duration(400).onEnd(new YoYo.AnimatorCallback() {
                @Override
                public void call(Animator animator) {
                    ((View) hardButton).setVisibility(View.GONE);
                    ((View) normalButton).setVisibility(View.GONE);
                    ((View) easyButton).setVisibility(View.GONE);
                }
            }).playOn(hardButton);
            YoYo.with(Techniques.SlideOutDown).duration(300).playOn(normalButton);
            YoYo.with(Techniques.SlideOutDown).duration(200).playOn(easyButton);
            YoYo.with(Techniques.Pulse).duration(200).delay(150).playOn(mainButton);

        } else {
            ((View) hardButton).setVisibility(View.GONE);
            ((View) normalButton).setVisibility(View.GONE);
            ((View) easyButton).setVisibility(View.GONE);
        }

        expanded = false;
        if (onCollapsedListener != null) onCollapsedListener.onCollapsed();
    }

    public void hide() {
        if (onStage) {
            YoYo.with(Techniques.TakingOff).duration(300).onEnd(new YoYo.AnimatorCallback() {
                @Override
                public void call(Animator animator) {
                    FloatingMenu.this.setVisibility(View.GONE);
                }
            }).playOn(this);
            onStage = false;
        }
    }

    public void show() {
        if (!onStage) {
            FloatingMenu.this.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Landing).duration(300).playOn(this);
            onStage = true;
        }
    }

    public void callAttention() {
        if (onStage) {
            YoYo.with(Techniques.Wobble)
                    .duration(200)
                    .repeat(1)
                    .playOn(this);
            Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

}
