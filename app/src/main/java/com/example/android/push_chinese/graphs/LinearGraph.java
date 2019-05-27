package com.example.android.push_chinese.graphs;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.example.android.push_chinese.R;
import com.example.android.push_chinese.Word;

public class LinearGraph extends LinearLayout {
    private Context context;
    private String title;
    private Integer nominator;
    private Integer denominator;
    private TextView titleTextView;
    private TextView nominatorTextView;
    private TextView denominatorTextView;


    int getPixelDimensions(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getContext().getResources().getDisplayMetrics()
        );
    }


    public LinearGraph(Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.graph_indicator, this, true);
        titleTextView = this.findViewById(R.id.indicator_title);
        nominatorTextView = this.findViewById(R.id.indicator_nominator);
        nominatorTextView.setText("toper");
        denominatorTextView = this.findViewById(R.id.indicator_denominator);

    }
    public LinearGraph(Context context, String title, Integer nominator, Integer denominator) {
        this(context);
        setTitle(title);
        setNominator(nominator);
        setDenominator(denominator);
    }

    public void setTitle(String title) {
        this.title = title;
        titleTextView.setText(title);

    }

    public void setNominator(Integer nominator) {
        this.nominator = nominator;
        nominatorTextView.setText(nominator.toString());
    }

    public void setDenominator(Integer denominator) {
        this.denominator = denominator;
        denominatorTextView.setText("/ " + denominator);
    }



}
