package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.android.tian_tian.R;

import static com.example.android.tian_tian.utilities.Helper.dpiToPixels;

public class DoubleGraph extends LinearLayout {

    Context context;
    public DoubleGraph(Context context) {
        super(context);
        init(context);
    }

    public DoubleGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DoubleGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DoubleGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        this.setOrientation(VERTICAL);
    }

    private TextView getFormatedTitle(String title) {
        TextView titleTextView = new TextView(context);
        titleTextView.setText(title);
        titleTextView.setLayoutParams(new LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        titleTextView.setTextColor( getResources().getColor(R.color.grayish));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setAllCaps(true);
        return titleTextView;
    }

    public void setTitle(@Nullable String leftTitleLiteral, @Nullable String rightTitleLiteral) {
        LinearLayout titleContainer = new LinearLayout(context);
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);
        titleContainer.setPadding(0, dpiToPixels(context, 8), 0, dpiToPixels(context, 16));
        this.addView(titleContainer);
        titleContainer.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        titleContainer.addView(getFormatedTitle(leftTitleLiteral));
        TextView rightTitleTextView = getFormatedTitle(rightTitleLiteral);
        rightTitleTextView.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        titleContainer.addView(rightTitleTextView);

    }

    public void setData(Integer[] leftData, String[] legend, Integer[] rightData) {
        int maximum = 1;
        int maximumLeft = Math.max(maximum, Helper.max(leftData));
        int maximumRight = Math.max(maximum, Helper.max(rightData));
        float eachLeft = ((float) 1.0 / maximumLeft);
        float eachRight = ((float) 1.0 / maximumRight);

        for (int i = 0; i < legend.length; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            this.addView(row);
            row.setPadding(0,0,0, dpiToPixels(context, 12));
            row.setGravity(Gravity.CENTER_VERTICAL);

            if (leftData != null) {
                TextView leftDataTextView = new TextView(context);
                leftDataTextView.setText("" + leftData[i]);
                row.addView(leftDataTextView);
                leftDataTextView.setLayoutParams(new LayoutParams(dpiToPixels(context, 24), ViewGroup.LayoutParams.WRAP_CONTENT));
                leftDataTextView.setPadding(0,0, dpiToPixels(context, 4), 0);
                leftDataTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

                LinearLayout barContainer = new LinearLayout(context);
                barContainer.setBackground(getResources().getDrawable(R.drawable.rounded_dash_empty));
                row.addView(barContainer);
                barContainer.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
                View barView = new View(context);
                barContainer.addView(barView);
                barView.setLayoutParams(new LayoutParams(
                        0,
                        dpiToPixels(context, 4),
                        (float) eachLeft * leftData[i]
                ));
                barView.setBackground(getResources().getDrawable(R.drawable.rounded_dash_filled));
                View barEmptyView = new View(context);
                barContainer.addView(barEmptyView);
                barEmptyView.setLayoutParams(new LayoutParams(
                        0,
                        dpiToPixels(context, 4),
                        1 - (float) eachLeft * leftData[i]
                ));
            }

            TextView legendTextView = new TextView(context);
            legendTextView.setText(legend[i]);
            legendTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            legendTextView.setTextColor(getResources().getColor(R.color.grayish));
            row.addView(legendTextView);
            legendTextView.setLayoutParams(new LayoutParams(
                    dpiToPixels(context, 60),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            if (rightData != null) {
                LinearLayout barContainer = new LinearLayout(context);
                barContainer.setBackground(getResources().getDrawable(R.drawable.rounded_dash_empty));
                View barEmptyView = new View(context);
                barContainer.addView(barEmptyView);
                barEmptyView.setLayoutParams(new LayoutParams(
                        0,
                        dpiToPixels(context, 4),
                        1 - (float) eachRight * rightData[i]
                ));
                row.addView(barContainer);
                barContainer.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
                View barView = new View(context);
                barContainer.addView(barView);
                barView.setLayoutParams(new LayoutParams(
                        0,
                        dpiToPixels(context, 4),
                        (float) eachRight * rightData[i]
                ));
                barView.setBackground(getResources().getDrawable(R.drawable.rounded_dash_filled));

                TextView leftDataTextView = new TextView(context);
                leftDataTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                leftDataTextView.setText("" + rightData[i]);
                row.addView(leftDataTextView);
                leftDataTextView.setLayoutParams(new LayoutParams(dpiToPixels(context, 24), ViewGroup.LayoutParams.WRAP_CONTENT));
                leftDataTextView.setPadding(dpiToPixels(context, 4),0, 0, 0);
            }
        }
    }
}
