package com.example.android.everytian.fragments;

import android.animation.PropertyValuesHolder;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.renderer.YRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.db.chart.view.StackBarChartView;
import com.example.android.everytian.R;
import com.example.android.everytian.entities.Statics;
import com.example.android.everytian.entities.auxiliaries.Record;
import com.example.android.everytian.utilities.DoubleGraph;
import com.example.android.everytian.utilities.Helper;
import com.example.android.everytian.utilities.SRScheduler;

import java.util.ArrayList;

public class StaticsFragment extends Fragment {
    SRScheduler scheduler;
    public StaticsFragment() {
        // Required empty public constructor
    }

    int getPixelDimensions(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getContext().getResources().getDisplayMetrics()
        );
    }

    private Tooltip mTip;

    private TextView mLegendOneRed;

    private TextView mLegendOneYellow;

    private TextView mLegendOneGreen;


    private StackBarChartView mChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();
        View rootView = inflater.inflate(R.layout.statics, container, false);

        final int quantity = 7;
        final ArrayList<Record> records = Statics.getLastRecords(getContext(), quantity);
        Record todaysRecord = records.get(0);

        ((TextView) rootView.findViewById(R.id.todays_new_words_so_far)).setText("" + scheduler.learnedToday());
        ((TextView) rootView.findViewById(R.id.todays_new_words_total)).setText("" + scheduler.wordsEachDay);
        ((TextView) rootView.findViewById(R.id.reviewd_so_far)).setText("" + todaysRecord.getReviewed());
        ((TextView) rootView.findViewById(R.id.to_review_total)).setText("" + (scheduler.toReviewQuantity(SRScheduler.TODAY) + todaysRecord.getReviewed()));

        LinearLayout newWordsDashed = rootView.findViewById(R.id.newWordsDashed);
        LinearLayout dashedLines = Helper.getDashedLines(getContext(), scheduler.learnedToday(), scheduler.wordsEachDay);

        LinearLayout toReviewDashed = rootView.findViewById(R.id.toReviewDashed);
        LinearLayout toReviewDashedLines = Helper.getDashedLines(getContext(), todaysRecord.getReviewed(),
                scheduler.toReviewQuantity(SRScheduler.TODAY) + todaysRecord.getReviewed());

        newWordsDashed.addView(dashedLines);
        dashedLines.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        dashedLines.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;

        toReviewDashed.addView(toReviewDashedLines);
        toReviewDashed.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        toReviewDashed.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;

        final LineChartView chart = rootView.findViewById(R.id.linechart);
        LineSet dataset = new LineSet();
        LineSet reviewChartDataset = new LineSet();
        LineSet futureReviewDataset = new LineSet();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int initializedDay = preferences.getInt("initializedDay", 0);
        int initializedDayIndex = 0;

        int maxiumValue = 0;
        int maxiumValueIndex = 0;
        int maxiumValueSet = 0;

        BarSet stackBarEasy = new BarSet();
        BarSet stackBarNormal = new BarSet();
        BarSet stackBarHard = new BarSet();

        Integer[] leftData = new Integer[records.size()];
        String[] literals = new String[records.size()];
        Integer[] rightData = new Integer[records.size()];

        for (int i = 0; i < quantity; i++) {
            Record record = records.get(i);
            literals[i] = record.getDateLiteral();
            leftData[i] = record.getReviewed();
            rightData[i] = record.getLearned();
            dataset.addPoint(record.getDateLiteral(), record.getLearned());
            reviewChartDataset.addPoint(record.getDateLiteral(), record.getReviewed());
            maxiumValue = Math.max(maxiumValue, record.getLearned());

            if (initializedDayIndex == 0 && record.getDay() >= initializedDay) {
                initializedDayIndex = i;
            }

            if (record.getLearned() >= record.getReviewed()) {
                if (record.getLearned() >= maxiumValue) {
                    maxiumValue = record.getLearned();
                    maxiumValueIndex = i;
                    maxiumValueSet = 0;
                }
            } else {
                if (record.getReviewed() >= maxiumValue) {
                    maxiumValue = record.getReviewed();
                    maxiumValueIndex = i;
                    maxiumValueSet = 1;
                }
            }

            futureReviewDataset.addPoint(record.getDateLiteral(), record.getReviewed());

            stackBarEasy.addBar(record.getDateLiteral(), record.getEasy());
            stackBarNormal.addBar(record.getDateLiteral(), record.getNormal());
            stackBarHard.addBar(record.getDateLiteral(), record.getHard());
        }


//        ((TextView) rootView.findViewById(R.id.to_review_debug)).setText("Today: " + scheduler.toReviewQuantity(0) +
//                            "\nTomorrow: " + scheduler.toReviewQuantity(1) +
//                            "\nDay after tomorrow: " + scheduler.toReviewQuantity(2));

        int today = Helper.daysSinceEpoch();
        int accumulated = scheduler.toReviewQuantity(0);

        for (int i = 1; i <= 3; i++) {
            String label = Helper.daysSinceEpochToLiteral(today + i);
            dataset.addPoint(label, 0);
            reviewChartDataset.addPoint(label, 0);
            int dayAccumulated = scheduler.toReviewQuantity(i);
            futureReviewDataset.addPoint(label, dayAccumulated - accumulated);
            maxiumValue = Math.max(dayAccumulated - accumulated, maxiumValue);
            accumulated = dayAccumulated;
        }

        futureReviewDataset.beginAt(quantity - 1);
        futureReviewDataset.setDashed(new float[] {getPixelDimensions(8), getPixelDimensions(4)});
        futureReviewDataset.setDotsColor(getResources().getColor(R.color.white));
        futureReviewDataset.setFill(getResources().getColor(R.color.transparent_orange));
        futureReviewDataset.setDotsRadius(getPixelDimensions(4));
        futureReviewDataset.setDotsStrokeThickness(getPixelDimensions(2));
        futureReviewDataset.setDotsStrokeColor(getResources().getColor(R.color.category_numbers));
        futureReviewDataset.setSmooth(true);
        futureReviewDataset.setThickness(getPixelDimensions(3));
        futureReviewDataset.setColor(getResources().getColor(R.color.category_numbers));



        stackBarEasy.setColor(getResources().getColor(R.color.easy_tint));
        stackBarNormal.setColor(getResources().getColor(R.color.normal_tint));
        stackBarHard.setColor(getResources().getColor(R.color.hard_tint));

        mChart = rootView.findViewById(R.id.chart);

        mChart.addData(stackBarEasy);
        mChart.addData(stackBarNormal);
        mChart.addData(stackBarHard);

        chart.addData(dataset);

        chart.addData(reviewChartDataset);
        chart.addData(futureReviewDataset);

        chart.setYAxis(false);
        chart.setXAxis(false);
//        chart.setLabelsFormat(DecimalFormat.INTEGER_FIELD);

        dataset.setDotsColor(getResources().getColor(R.color.white));
        dataset.setDotsRadius(getPixelDimensions(4));
        dataset.setDotsStrokeThickness(getPixelDimensions(2));
        dataset.setDotsStrokeColor(getResources().getColor(R.color.primary_color));
        Log.w("initializedDayIndex", "" + initializedDayIndex);
        dataset.beginAt(initializedDayIndex);
        dataset.setSmooth(true);
        dataset.setThickness(getPixelDimensions(3));
        dataset.setColor(getResources().getColor(R.color.primary_color));
        dataset.endAt(quantity);

        // Fill
        dataset.setFill(getResources().getColor(R.color.transparent_blue));
//        dataset.setGradientFill(color[], float[])

        chart.setYLabels(AxisRenderer.LabelPosition.NONE);
//        chart.setXLabels(AxisRenderer.LabelPosition.NONE);
        chart.setPadding(getPixelDimensions(8), getPixelDimensions(8), getPixelDimensions(8), getPixelDimensions(8));
        chart.setClipToPadding(false);

        mTip = new Tooltip(getContext(), R.layout.linechart_tooltip, R.id.value);
//        ((TextView) mTip.findViewById(R.id.value)).setTypeface(
//                Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"));

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        final int tooltipMaxiumValue = maxiumValue;
        final int tooltipValueSet = maxiumValueSet;
        final int tooltipValueIndex = maxiumValueIndex;


        Runnable chartAction = new Runnable() {
            @Override
            public void run() {
                mTip.prepare(chart.getEntriesArea(tooltipValueSet).get(tooltipValueIndex), tooltipMaxiumValue);
                chart.removeView(mTip);
                chart.showTooltip(mTip, true);
            }
        };

        chart
                .setAxisBorderValues(0, Math.max(1, maxiumValue * ((float) 1.45)))
                .setAxisLabelsSpacing((int) Tools.fromDpToPx(10))
                .setTooltips(mTip)
                .show(new Animation().setInterpolator(
                        new BounceInterpolator())
                            .fromAlpha(0)
                            .withEndAction(chartAction)
                );

        reviewChartDataset.setDotsColor(getResources().getColor(R.color.white));
        reviewChartDataset.setFill(getResources().getColor(R.color.transparent_orange));
        reviewChartDataset.setDotsRadius(getPixelDimensions(4));
        reviewChartDataset.setDotsStrokeThickness(getPixelDimensions(2));
        reviewChartDataset.setDotsStrokeColor(getResources().getColor(R.color.category_numbers));
        reviewChartDataset.setSmooth(true);
        reviewChartDataset.setThickness(getPixelDimensions(3));
        reviewChartDataset.setColor(getResources().getColor(R.color.category_numbers));
        reviewChartDataset.beginAt(initializedDayIndex);
        reviewChartDataset.endAt(quantity);

        // Difficulty graph

        mLegendOneRed = (TextView) rootView.findViewById(R.id.state_one);
        mLegendOneYellow = (TextView) rootView.findViewById(R.id.state_two);
        mLegendOneGreen = (TextView) rootView.findViewById(R.id.state_three);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mChart.setOnEntryClickListener(new OnEntryClickListener() {
                @Override
                public void onClick(int setIndex, int entryIndex, Rect rect) {

                    if (setIndex == 2) mLegendOneRed.animate()
                            .scaleY(1.3f)
                            .scaleX(1.3f)
                            .setDuration(100)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                    mLegendOneRed.animate().scaleY(1.0f).scaleX(1.0f).setDuration(100);
                                }
                            });
                    else if (setIndex == 1) {
                        mLegendOneYellow.animate()
                                .scaleY(1.3f)
                                .scaleX(1.3f)
                                .setDuration(100)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {

                                        mLegendOneYellow.animate()
                                                .scaleY(1.0f)
                                                .scaleX(1.0f)
                                                .setDuration(100);
                                    }
                                });
                    } else {
                        mLegendOneGreen.animate()
                                .scaleY(1.3f)
                                .scaleX(1.3f)
                                .setDuration(100)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {

                                        mLegendOneGreen.animate()
                                                .scaleY(1.0f)
                                                .scaleX(1.0f)
                                                .setDuration(100);
                                    }
                                });
                    }
                }
            });

        int[] order = new int[quantity];
        for (int i= 0; i < quantity; i++) {
            order[i] = i;
        }

//        mChart
//                .setYLabels(YRenderer.LabelPosition.NONE)
//                .setAxisLabelsSpacing((int) Tools.fromDpToPx(10))
//                .show(new Animation().inSequence(.5f, order));

        DoubleGraph bg = rootView.findViewById(R.id.double_graph);
        bg.setTitle("Reviewed", "Learned");
        bg.setData(leftData, literals, rightData);
        return rootView;

    }
}
