package com.example.android.push_chinese.expandable_section;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.android.push_chinese.R;

public class ExpandableSection extends LinearLayout {

    public interface OnCollapseListener {
        void onCollapse(View view);
    }

    public void setOnCollapseListener(OnCollapseListener onCollapseListener) {
        this.onCollapseListener = onCollapseListener;
    }

    boolean isImage = false;
    private static final int ANIMATION_TIME = 100;
    LinearLayout headerView;
    ImageView toggle_icon = null;
    public View contentView;
    public ConstraintLayout section;
    int targetHeight;
    OnCollapseListener onCollapseListener = null;

    boolean expanded = true;

    private static TextView createContentTextView(Context context, String content, int textSize) {
        TextView contentTextView = new TextView(context);
        contentTextView.setText(content);
        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        return contentTextView;
    }

    public ExpandableSection(Context context, String title, String content, boolean initialyExpanded, int textSize) {
        this(context, title, createContentTextView(context, content, textSize), initialyExpanded);
    }

    public ExpandableSection(Context context) {
        super(context);
        isImage = true;
        LayoutInflater inflator = LayoutInflater.from(context);
        section =  (ConstraintLayout) inflator.inflate(R.layout.collapsable_image, null);

        contentView = section.findViewById(R.id.CollapsableImage);
        headerView = section.findViewById(R.id.CollapsableHeader);

        headerView.addView(getTitle(context, "IMAGE"));

        toggle_icon = getToggleIcon(context);
        headerView.addView(toggle_icon);
        headerView.setGravity(Gravity.CENTER_VERTICAL);

        headerView.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        context.getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4,
                        context.getResources().getDisplayMetrics()));
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle(true);
            }
        });
    }

    private TextView getTitle(Context context, String title) {
        TextView titleTextView = new TextView(context);
        titleTextView.setPadding(0,0,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8,
                        context.getResources().getDisplayMetrics()),0);
        titleTextView.setTextColor( getResources().getColor(R.color.grayish));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setAllCaps(true);
        titleTextView.setText(title);
        return titleTextView;
    }

    private ImageView getToggleIcon(Context context) {
        toggle_icon = new ImageView(context);
        toggle_icon.setImageResource(R.drawable.expand_collapse);
        toggle_icon.requestLayout();
        toggle_icon.setMaxHeight((int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                14, context.getResources().getDisplayMetrics()));
        toggle_icon.setMaxWidth((int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                14, context.getResources().getDisplayMetrics()));
        return toggle_icon;
    }

    public ExpandableSection(Context context, String title, View content, boolean initialyExpanded) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        headerView = new LinearLayout(context);
        headerView.setPadding(
                0,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        context.getResources().getDisplayMetrics()),
                0
                , (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4,
                        context.getResources().getDisplayMetrics()));

        headerView.addView(getTitle(context, title));
        headerView.setGravity(Gravity.CENTER_VERTICAL);

        toggle_icon = getToggleIcon(context);
        headerView.addView(toggle_icon);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle(true);
            }
        });

        this.addView(headerView);
        contentView = content;
        this.addView(contentView);

        expanded = initialyExpanded;
        if (expanded) {
            expand(false);
        } else {
            collapse(false);
        }

    }

    public void toggle(boolean animated) {
        if (expanded) {
            collapse(animated);
        } else {
            expand(animated);
        }
    }

    public void expand(boolean animated) {
        RotateAnimation rotate = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(100);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new AccelerateInterpolator());
        toggle_icon.startAnimation(rotate);
        expanded = true;
        contentView.setVisibility(View.VISIBLE);
        if (animated) {
            expand(contentView);
        }
    }

    public void collapse(boolean animated) {
        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(ANIMATION_TIME);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new AccelerateInterpolator());
        toggle_icon.startAnimation(rotate);
        expanded = false;
        if (animated) {
            collapse(contentView);
        } else {
            contentView.setVisibility(View.GONE);
        }
    }

    public void expand(final View v) {
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(wrapContentMeasureSpec, wrapContentMeasureSpec);
        if (!isImage)
            targetHeight = v.getMeasuredHeight();
        else
            targetHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    200,
                    getContext().getResources().getDisplayMetrics());

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? (isImage? targetHeight : LayoutParams.WRAP_CONTENT)
                        : (int)(targetHeight * interpolatedTime);
                v.setAlpha(interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(isImage? ANIMATION_TIME * 2 : ANIMATION_TIME);
        v.startAnimation(a);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isCollapsed() {
        return !expanded;
    }

    public void collapse(final View v) {
        Log.w("collapse", "entering");
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.setAlpha(1 - interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(isImage? ANIMATION_TIME * 2 : ANIMATION_TIME);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onCollapseListener != null) {
                    onCollapseListener.onCollapse(ExpandableSection.this);
                }
            }
        });
        v.startAnimation(a);
    }

}
