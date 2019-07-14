package com.example.android.everytian.utilities;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.android.everytian.R;

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

    public void setTitle(@Nullable String leftTitleLiteral, @Nullable String rightTitleLiteral) {
        LinearLayout titleContainer = new LinearLayout(context);
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(titleContainer);
        titleContainer.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView rightTitleTextView = new TextView(context);
        rightTitleTextView.setText(leftTitleLiteral);
        rightTitleTextView.setLayoutParams(new LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        rightTitleTextView.setAllCaps(true);
        titleContainer.addView(rightTitleTextView);

        TextView leftTitleTextView = new TextView(context);
        leftTitleTextView.setText(rightTitleLiteral);
        leftTitleTextView.setLayoutParams(new LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        leftTitleTextView.setAllCaps(true);
        leftTitleTextView.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        titleContainer.addView(leftTitleTextView);

    }

    public void setData(Integer[] leftData, String[] legend, Integer[] rightData) {
        for (int i = 0; i < legend.length; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            this.addView(row);

            if (leftData != null) {
                TextView leftDataTextView = new TextView(context);
                leftDataTextView.setText("" + leftData[i]);
                row.addView(leftDataTextView);
                leftDataTextView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                LinearLayout barContainer = new LinearLayout(context);
                row.addView(barContainer);
                barContainer.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
                View barView = new View(context);
                barContainer.addView(barView);
                barView.setLayoutParams(new LayoutParams(
                        leftData[i] * 30,
                        100
                ));
                barView.setBackgroundColor(getResources().getColor(R.color.primary_color));
            }

            TextView legendTextView = new TextView(context);
            legendTextView.setText(legend[i]);
            row.addView(legendTextView);

            if (rightData != null) {
                TextView rightDataTextView = new TextView(context);
                rightDataTextView.setText("" + rightData[i]);
                row.addView(rightDataTextView);
                rightDataTextView.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
                rightDataTextView.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
            }
        }
    }
}
