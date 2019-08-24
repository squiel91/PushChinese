package com.example.android.tian_tian.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.tian_tian.BusEvents.LearnQuantityUpdated;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.entities.Statics;
import com.example.android.tian_tian.entities.auxiliaries.Record;
import com.example.android.tian_tian.utilities.DoubleGraph;
import com.example.android.tian_tian.utilities.Helper;
import com.example.android.tian_tian.utilities.SRScheduler;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

public class StaticsFragment extends Fragment {
    SRScheduler scheduler;
    View rootView;

    @Subscribe
    public void learningWordsChanged(LearnQuantityUpdated quantity) {
        init(quantity.getQuantity());
    }

    public StaticsFragment() {
        // Required empty public constructor
    }

    private View init(Integer wordsToLearn) {
        final int quantity = 7;
        final ArrayList<Record> records = Statics.getLastRecords(getContext(), quantity);
        Record todaysRecord = records.get(0);

        if (wordsToLearn == null) {
            wordsToLearn = scheduler.wordsEachDay;
        }
        Log.w("wordsToLearn", "" + wordsToLearn);

        ((TextView) rootView.findViewById(R.id.todays_new_words_so_far)).setText("" + scheduler.learnedToday());
        ((TextView) rootView.findViewById(R.id.todays_new_words_total)).setText("" + wordsToLearn);
        ((TextView) rootView.findViewById(R.id.reviewd_so_far)).setText("" + todaysRecord.getReviewed());
        ((TextView) rootView.findViewById(R.id.to_review_total)).setText("" + (scheduler.toReviewQuantity(SRScheduler.TODAY) + todaysRecord.getReviewed()));

        LinearLayout newWordsDashed = rootView.findViewById(R.id.newWordsDashed);
        LinearLayout dashedLines = Helper.getDashedLines(getContext(), scheduler.learnedToday(), wordsToLearn);

        LinearLayout toReviewDashed = rootView.findViewById(R.id.toReviewDashed);
        LinearLayout toReviewDashedLines = Helper.getDashedLines(getContext(), todaysRecord.getReviewed(),
                scheduler.toReviewQuantity(SRScheduler.TODAY) + todaysRecord.getReviewed());

        newWordsDashed.removeAllViews();
        newWordsDashed.addView(dashedLines);
        dashedLines.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        dashedLines.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;

        toReviewDashed.removeAllViews();
        toReviewDashed.addView(toReviewDashedLines);
        toReviewDashed.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        toReviewDashed.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;

        Integer[] reviewedQuantity = new Integer[records.size()];
        String[] literals = new String[records.size()];
        Integer[] learnedQuantity = new Integer[records.size()];

        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            literals[i] = record.getDateLiteral();
            reviewedQuantity[i] = record.getReviewed();
            learnedQuantity[i] = record.getLearned();
        }

        int today = Helper.daysSinceEpoch();
        int accumulated = scheduler.toReviewQuantity(0);

        String[] toReviewLiterals = new String[10];
        Integer[] toReviewValues = new Integer[10];

        for (int i = 1; i <= 10; i++) {
            String label = Helper.daysSinceEpochToLiteral(today + i);
            toReviewLiterals[9 - (i - 1)] = label;

            int dayAccumulated = scheduler.toReviewQuantity(i);
            toReviewValues[9 - (i - 1)] = dayAccumulated - accumulated;
            accumulated = dayAccumulated;
        }

        DoubleGraph bgp = rootView.findViewById(R.id.double_graph_projection);
        bgp.removeAllViews();
        bgp.setTitle("Next to Review", "");
        bgp.setData(toReviewValues, toReviewLiterals, null);

        DoubleGraph bg = rootView.findViewById(R.id.double_graph);
        bg.removeAllViews();
        bg.setTitle("Learned", "Reviewed");
        bg.setData(learnedQuantity, literals, reviewedQuantity);
        return rootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        scheduler = ((SRScheduler.SRSchedulerInterface) getContext()).getSRScheduler();
        rootView = inflater.inflate(R.layout.statics, container, false);

        return init(null);
    }
}
