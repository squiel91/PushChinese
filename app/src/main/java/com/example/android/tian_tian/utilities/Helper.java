package com.example.android.tian_tian.utilities;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.android.tian_tian.R;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Date;

public class Helper {

    static int timeMachine = 0;

    static public String[] listFromStringCursor(Cursor cursor, String columnName, String separator) {
        String line =  cursor.getString(cursor.getColumnIndexOrThrow(columnName));
        String[] list;
        if ((line != null) && (!line.trim().isEmpty())) {
            list = line.split(separator);
        } else {
            list = new String[0];
        }
        return list;
    }

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

    public static int dpiToPixels(Context context, float value) {
        return getPixelDimensions(context, TypedValue.COMPLEX_UNIT_DIP, value);
    }

    public static int getPixelDimensions(Context context, int unit, float value) {
        return (int) TypedValue.applyDimension(
                unit,
                value,
                context.getResources().getDisplayMetrics()
        );
    }

    static public String join(String sep, Object[] array) {
        if (array != null){
            StringBuilder partial = new StringBuilder();
            for(int i = 0; i < array.length; i++){
                if (i > 0) partial.append(sep);
                partial.append(array[i]);
            }
            return partial.toString();
        } else return "";
    }


    static public String daysSinceEpochToLiteral(int dSE) {
        int today = daysSinceEpoch();

        if (dSE == today + 1) {
            return "TOM.";
        }

        if (dSE == today) {
            return "TODAY";
        }
        if (dSE == today - 1) {
            return "YEST.";
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
            layoutParams.height = dpiToPixels(context, 4);
            if (i < total - 1) {
                layoutParams.rightMargin = dpiToPixels(context,3);
            }
        }
        return container;
    }

    static public String getCharacter(String string, int position) {
        char[] characters = string.toCharArray();
        return String.valueOf(characters[position]);
    }

    static public String toPinyin(String asciiPinyin) {
        Map<String, String> pinyinToneMarks = new HashMap<String, String>();
        pinyinToneMarks.put("a", "āáǎà"); pinyinToneMarks.put("e", "ēéěè");
        pinyinToneMarks.put("i", "īíǐì"); pinyinToneMarks.put("o",  "ōóǒò");
        pinyinToneMarks.put("u", "ūúǔù"); pinyinToneMarks.put("ü", "ǖǘǚǜ");
        pinyinToneMarks.put("A",  "ĀÁǍÀ"); pinyinToneMarks.put("E", "ĒÉĚÈ");
        pinyinToneMarks.put("I", "ĪÍǏÌ"); pinyinToneMarks.put("O", "ŌÓǑÒ");
        pinyinToneMarks.put("U", "ŪÚǓÙ"); pinyinToneMarks.put("Ü",  "ǕǗǙǛ");

        Pattern pattern = Pattern.compile("([aeiouüvÜ]{1,3})(n?g?r?)([012345])");
        Matcher matcher = pattern.matcher(asciiPinyin);
        StringBuilder s = new StringBuilder();
        int start = 0;
        while (matcher.find(start)) {
            s.append(asciiPinyin, start, matcher.start(1));
            int tone = Integer.parseInt(matcher.group(3)) % 5;
            String r = matcher.group(1).replace("v", "ü").replace("V", "Ü");
            // for multple vowels, use first one if it is a/e/o, otherwise use second one
            int pos = r.length() > 1 && "aeoAEO".contains(getCharacter(r,0).toString())? 1 : 0;
            if (tone != 0) {
                s.append(r, 0, pos).append(getCharacter(pinyinToneMarks.get(getCharacter(r, pos)),tone - 1)).append(r, pos + 1, r.length());
            } else {
                s.append(r);
            }
            s.append(matcher.group(2));
            start = matcher.end(3);
        }
        if (start != asciiPinyin.length()) {
            s.append(asciiPinyin, start, asciiPinyin.length());
        }
        return s.toString();
    }

    public static void setImage(final Context context, final String drawable_name, final ImageView imageView) {
          imageView.setImageResource(context.getResources().getIdentifier(drawable_name, "drawable", "com.example.android.tian_tian"));
    }

    public static int max(Integer[] list) {
        int max = 0;
        if (list != null) {
            for (int i : list) {
                max = Math.max(i, max);
            }
        }
        return max;
    }

    public static String[] shuffleStringArray(String[] array){
        Random rgen = new Random();  // Random number generator

        for (int i = 0; i < array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            String temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        return array;
    }
}
