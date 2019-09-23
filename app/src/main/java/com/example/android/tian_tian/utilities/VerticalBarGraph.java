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

public class VerticalBarGraph extends LinearLayout {

    Context context;
    public VerticalBarGraph(Context context) {
        super(context);
        init(context);
    }

    public VerticalBarGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalBarGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public VerticalBarGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        this.setOrientation(HORIZONTAL);
    }

    public void setData(Integer[] data, String[] legend) {
        this.removeAllViews();

        int maximum = 1;
        maximum = Math.max(maximum, Helper.max(data));
        float each = ((float) 1.0 / maximum);

        for (int i = 0; i < legend.length; i++) {
            LinearLayout column = new LinearLayout(context);
            column.setOrientation(VERTICAL);

            column.setLayoutParams(new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (float) 1.0 / legend.length
            ));
            column.setGravity(Gravity.CENTER_HORIZONTAL);
            this.addView(column);

            TextView dataTextView = new TextView(context);
            dataTextView.setText("" + data[i]);
            column.addView(dataTextView);
//            dataTextView.setLayoutParams(new LayoutParams(dpiToPixels(context, 24), ViewGroup.LayoutParams.WRAP_CONTENT));
            dataTextView.setPadding(0,0, 0, dpiToPixels(context, 8));
            dataTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

            LinearLayout barContainer = new LinearLayout(context);
            column.addView(barContainer);
            barContainer.setLayoutParams(new LayoutParams(
                    dpiToPixels(context, 4),
                    dpiToPixels(context, 120)));
            barContainer.setOrientation(VERTICAL);
            barContainer.setBackground(getResources().getDrawable(R.drawable.rounded_dash_empty));
//
//
            View barEmptyView = new View(context);
            barContainer.addView(barEmptyView);
            barEmptyView.setLayoutParams(new LayoutParams(
                    dpiToPixels(context, 4),
                    0,
                    1 - (float) each * data[i]
            ));

            View barView = new View(context);
            barContainer.addView(barView);
            barView.setLayoutParams(new LayoutParams(
                    dpiToPixels(context, 8),
                    0,
                    (float) each * data[i]
            ));
            barView.setBackground(getResources().getDrawable(R.drawable.rounded_dash_filled));

            TextView legendTextView = new TextView(context);
            legendTextView.setText(legend[i]);
            legendTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            legendTextView.setTextColor(getResources().getColor(R.color.grayish));
            legendTextView.setPadding(0,dpiToPixels(context, 8), 0, 0);
            column.addView(legendTextView);
        }
    }
}
