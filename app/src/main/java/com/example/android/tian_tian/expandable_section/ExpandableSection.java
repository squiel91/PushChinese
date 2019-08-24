package com.example.android.tian_tian.expandable_section;

import android.content.Context;
import android.graphics.Typeface;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.utilities.Helper;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

public class ExpandableSection extends LinearLayout {

    public interface OnCollapseListener {
        void onCollapse(View view);
    }

    public void setOnCollapseListener(OnCollapseListener onCollapseListener) {
        this.onCollapseListener = onCollapseListener;
    }

    public static class OnLockedClick {
        public void OnLockedClick() {}
    }

    boolean isImage = false;
    private static final int ANIMATION_TIME = 150;
    LinearLayout headerView;
    ImageView toggle_icon = null;
    MaterialIconView lock_icon = null;
    public View contentView;
    int targetHeight;
    OnCollapseListener onCollapseListener = null;
    boolean locked = false;

    OnLockedClick onLockedClick = null;

    boolean expanded = true;

    public void setOnLockedClick(OnLockedClick onLockedClick) {
        this.onLockedClick = onLockedClick;
    }

    private static TextView createContentTextView(Context context, String content, int textSize) {
        TextView contentTextView = new TextView(context);
        contentTextView.setTextIsSelectable(true);
        contentTextView.setText(content);
        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        return contentTextView;
    }

    public ExpandableSection(Context context, String title, String content, boolean initialyExpanded, int textSize) {
        this(context, title, createContentTextView(context, content, textSize), initialyExpanded);
    }

    public void setLock(boolean locked) {
        if (locked) {
            lock_icon = new MaterialIconView(getContext());
            lock_icon.setIcon(MaterialDrawableBuilder.IconValue.LOCK);
//            lock_icon.setIcon(MaterialDrawableBuilder.IconValue.STAR_HALF); // RATE
//            lock_icon.setIcon(MaterialDrawableBuilder.IconValue.PLAYLIST_PLUS); // LEARN
//            lock_icon.setIcon(MaterialDrawableBuilder.IconValue.KEY); // EXPAND
            lock_icon.setColorResource(R.color.grayish);
            lock_icon.setScaleType(ImageView.ScaleType.CENTER);

            headerView.addView(lock_icon);
            LayoutParams params = new LayoutParams(
                    (int)TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            14, getContext().getResources().getDisplayMetrics()),
                    (int)TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            14, getContext().getResources().getDisplayMetrics())
            );
            params.leftMargin = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8, getContext().getResources().getDisplayMetrics());
            lock_icon.setLayoutParams(params);
            lock_icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            if (this.locked) {
                YoYo.with(Techniques.RollOut)
                        .duration(300)
                        .playOn(lock_icon);
                expand(true);
            }
        }
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public ExpandableSection(Context context, boolean initialyExpanded) {
        // is Image
        super(context);
        isImage = true;
        LayoutInflater inflator = LayoutInflater.from(context);
        inflator.inflate(R.layout.collapsable_image, this, true);

        contentView = this.findViewById(R.id.CollapsableImage);
        headerView = this.findViewById(R.id.CollapsableHeader);

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

        expanded = initialyExpanded;

        if (expanded) {
            expand(false);
        } else {
            collapse(false);
        }
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
        toggle_icon.setMaxHeight(Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP,  14));
        toggle_icon.setMaxWidth(Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 14));
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

        this.setPadding(
                Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 16),
                0,
                Helper.getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, 16),
                0);

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
            if  (locked) {
                if (onLockedClick != null) {
                    onLockedClick.OnLockedClick();
                }
            } else {
                expand(animated);
            }
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
        } else {
            headerView.setAlpha((float)1/3);
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
            headerView.setAlpha(1);
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
                headerView.setAlpha(1-((interpolatedTime*2)/3));
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(ANIMATION_TIME);
        a.setInterpolator(new AccelerateInterpolator());
        v.startAnimation(a);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void collapse(final View v) {
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
                    headerView.setAlpha((float)1/3 + ((interpolatedTime*2)/3));
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(ANIMATION_TIME);
        a.setInterpolator(new AccelerateInterpolator());
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
