package com.example.android.tian_tian.fragments;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.tian_tian.BusEvents.StatsUpdate;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.entities.Statics;
import com.example.android.tian_tian.entities.Word;
import com.example.android.tian_tian.entities.auxiliaries.Record;
import com.example.android.tian_tian.utilities.DoubleGraph;
import com.example.android.tian_tian.utilities.Helper;
import com.example.android.tian_tian.utilities.SRScheduler;
import com.example.android.tian_tian.utilities.VerticalBarGraph;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import static com.example.android.tian_tian.utilities.Helper.withoutNumbersAndSpaces;

public class StaticsFragment extends Fragment {
    SRScheduler scheduler;
    View rootView;

    @Subscribe
    public void learningWordsChanged(StatsUpdate dummy) {
        init();
    }

    public StaticsFragment() {
        // Required empty public constructor
    }

    private View init() {
        final int quantity = 7;
        final ArrayList<Record> records = Statics.getLastRecords(getContext(), quantity);
        Record todaysRecord = records.get(0);
        int wordsToLearn = scheduler.wordsEachDay;
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

        int learned = getTotalLearnedNumber();
        int total_voc = getTotalNumberWords();
        ((TextView) rootView.findViewById(R.id.learned_words)).setText("" + learned);
        ((TextView) rootView.findViewById(R.id.total_words)).setText("" + total_voc);
        float learnedRatio = (float) learned/Math.max(total_voc, 1);
        ((LinearLayout) rootView.findViewById(R.id.learned_inner_bar)).setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        learnedRatio
                )
        );
        ((LinearLayout) rootView.findViewById(R.id.learned_outer_bar)).setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1 - learnedRatio
                )
        );


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

        String[] toReviewLiterals = new String[7];
        Integer[] toReviewValues = new Integer[7];

        for (int i = 1; i <= 7; i++) {
            String label = Helper.daysSinceEpochToLiteral(today + i);
            toReviewLiterals[i - 1] = label;

            int dayAccumulated = scheduler.toReviewQuantity(i);
            toReviewValues[i - 1] = dayAccumulated - accumulated;
            accumulated = dayAccumulated;
        }

        VerticalBarGraph verticalBarGraph = rootView.findViewById(R.id.future_to_review);
        verticalBarGraph.setData(toReviewValues, toReviewLiterals);

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

        return init();
    }

    private int getTotalNumberWords() {
        Cursor cursor = getContext().getContentResolver().query(
                PushDbContract.Vocabulary.CONTENT_URI,
                new String[] {
                        PushDbContract.Vocabulary.COLUMN_ID
                }, null, null, null);
        return cursor.getCount();
    }

    private int getTotalLearnedNumber() {
        Cursor cursor = getContext().getContentResolver().query(
                PushDbContract.Vocabulary.CONTENT_URI,
                new String[] {
                        PushDbContract.Vocabulary.COLUMN_ID
                }, PushDbContract.Vocabulary.COLUMN_LEARNING_STAGE + " > ?",
                new String[] {
                        "" + Word.LEARNED
                }, null);
        return cursor.getCount();
    }
}
