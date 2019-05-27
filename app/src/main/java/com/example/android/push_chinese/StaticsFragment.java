package com.example.android.push_chinese;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.example.android.push_chinese.graphs.Indicator;

import java.text.DecimalFormat;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Number of new words remaining today
        // Remaining number of words to review today

        // easy normal hard ratio?


        // today
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();

        View rootView = inflater.inflate(R.layout.statics, container, false);

//        Word toper = scheduler.toReview(200);
//        if (toper != null) {
//            ((TextView)rootView.findViewById(R.id.overview)).setText(
//                    toper.getStage() +"/" + toper.getScheduledTo() + "/" + scheduler.howManyDaysSinceBC() +" - Remaining new words: " + scheduler.getRemainingWords() +
//                            "\nTo review today: " + scheduler.toReviewQuantity(0) +
//                            "\nTo review tomorrow: " + scheduler.toReviewQuantity(1) +
//                            "\nTo review the day after tomorrow: " + scheduler.toReviewQuantity(2));
//        } else {
//            ((TextView)rootView.findViewById(R.id.overview)).setText(
//                    "To review today: " + scheduler.toReviewQuantity(0) +
//                            "\nTo review tomorrow: " + scheduler.toReviewQuantity(1) +
//                            "\nTo review the day after tomorrow: " + scheduler.toReviewQuantity(2));
//        }

        LineChartView chart = (LineChartView)rootView.findViewById(R.id.linechart);
        LineSet dataset = new LineSet(new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}, new float[] {
                1,
                4,
                2,
                3,
                3,
                2,
                2,
                1,
                4,
                2,
                3,
                2,
                4,
                2
        });
        chart.addData(dataset);
        chart.setYAxis(false);
        chart.setXAxis(false);
//        chart.setLabelsFormat(DecimalFormat.INTEGER_FIELD);

        // DOT
        // Color
        dataset.setDotsColor(getResources().getColor(R.color.white));
        // Radius
        dataset.setDotsRadius(getPixelDimensions(4));
        // Strokes
        dataset.setDotsStrokeThickness(getPixelDimensions(2));
        dataset.setDotsStrokeColor(getResources().getColor(R.color.primary_color));

        // Line
        // Type
//        dataset.endAt(5);
//        dataset2.beginAt(4).setDashed(new float[] {getPixelDimensions(16), getPixelDimensions(4)});
        dataset.setSmooth(true);
        //Thickness
        dataset.setThickness(getPixelDimensions(3));
        // Color
        dataset.setColor(getResources().getColor(R.color.primary_color));

        // Fill
//        dataset.setFill(color)
//        dataset.setGradientFill(color[], float[])

        chart.setYLabels(AxisRenderer.LabelPosition.NONE);
        chart.setXLabels(AxisRenderer.LabelPosition.NONE);
        chart.show();

//66

        return rootView;

    }
}
