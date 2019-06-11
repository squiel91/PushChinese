package com.example.android.push_chinese.utilities;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.db.chart.util.Tools;
import com.example.android.push_chinese.R;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Date;

public class Helper {

    static int timeMachine = 2;

    static public String withoutNumbersAndSpaces(String input) {
        return input.replaceAll("[0-9 ]", "").toLowerCase();
    }

    static public int daysSinceEpoch() {
        return daysSinceEpoch(new Date()) + timeMachine;
    }

    static public int daysSinceEpoch(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(DateTimeUtils.toInstant(date), ZoneId.systemDefault());
        LocalDate localDate = localDateTime.toLocalDate();
        return (int) localDate.toEpochDay();
    }

    static public String daysSinceEpochToLiteral(int dSE) {
        int today = daysSinceEpoch();
        if (dSE == today) {
            return "TODAY";
        }
        LocalDate localDate = LocalDate.ofEpochDay(dSE);
        return (localDate.getMonth().getValue())+ "/" + (localDate.getDayOfMonth());
    }

    public static LinearLayout getDashedLines(Context context, int consumed, int total) {
        if (total == 0) {
            total = 1;
            consumed = 0;
        }
        LinearLayout container = new LinearLayout(context);
        for (int i = 0; i < total; i++) {
            View dashedLine = new View(context);
            if (i < consumed) {
                dashedLine.setBackground(context.getResources().getDrawable(R.drawable.rounded_dash_filled));
            } else {
                dashedLine.setBackground(context.getResources().getDrawable(R.drawable.rounded_dash_empty));
            }

            container.addView(dashedLine);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) dashedLine.getLayoutParams();
            layoutParams.weight = 1;
            layoutParams.width = 0;
            layoutParams.height = (int) Tools.fromDpToPx(4);
            if (i < total - 1) {
                layoutParams.rightMargin = (int) Tools.fromDpToPx(3);
            }
        }
        return container;
    }

}
