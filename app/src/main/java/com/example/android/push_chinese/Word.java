package com.example.android.push_chinese;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.push_chinese.data.PushDbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Word {
    private Long id;
    private String headWord;
    private String pronunciation;
    private String[] translations;
    private String[] measures;
    private Integer level;
    private Long[] examples;

    private int difficulty = 0;
    private boolean buried = false;
    private Boolean image;
    private Boolean audio;
    private Uri word_uri = null;

    public Word (Long id, String headWord, String pronunciation, String[] translations, String[] measures, Long[] examples,  Integer level, Boolean image, Boolean audio) {
        this.id = id;
        this.headWord = headWord;
        this.pronunciation = pronunciation;
        this.translations = translations;
        this.measures = measures;
        this.examples = examples;
        if (level != null) {
            this.level = level;
        } else {
            level = null;
        }
        this.image = (image != null) && image;
        this.audio =  (audio != null) && audio;
    }

    public static Word from_cursor(Cursor cursor) {
        String translationsString =  cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_TRANSLATION));
        String[] translations;
        if ((translationsString != null) && (!translationsString.trim().isEmpty())) {
            translations = translationsString.split(", ");
        } else {
            translations = null;
        }

        String examplesString = cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_EXAMPLES));

        String[] examplesStringList;
        if ((examplesString != null) && (!examplesString.trim().isEmpty())) {
            examplesStringList = examplesString.split(", ");
        } else {
            examplesStringList = new String[0];
        }
        Long[] examples = new Long[examplesStringList.length];
        for(int i = 0; i < examplesStringList.length; i++){
            try {
                examples[i] = Long.parseLong(examplesStringList[i]);
            } catch (Exception e) {
                Log.e(Word.class.getName(), "Cant parse sentence ID", e);
            }
        }

        String measuresString = cursor.getString(
                cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_MEASURES));
        String[] measures;
        if ((measuresString != null) && (!measuresString.trim().isEmpty())) {
            measures = measuresString.split(", ");
        } else {
            measures = new String[0];
        }

        return new Word(
                cursor.getLong(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_HEAD_WORD)),
                cursor.getString(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION)),
                translations,
                measures,
                examples,
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_LEVEL)),
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_AUDIO)) == 1,
                cursor.getInt(cursor.getColumnIndex(PushDbContract.Vocabulary.COLUMN_IMAGE))  == 1
        );
    }

    static private String getTonal(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.optString("tonal");
    }


    public static Word from_json(JSONObject json) throws JSONException {
        JSONArray jsonExamples = json.getJSONArray(PushDbContract.Vocabulary.COLUMN_EXAMPLES);
        Long[] examples;
        if (jsonExamples != null) {
            examples = new Long[jsonExamples.length()];
            for(int i = 0; i < jsonExamples.length(); i++){
                examples[i] = jsonExamples.getLong(i);
            }
        } else {
            examples = new Long[0];
        }

        JSONArray jsonTranslations = json.getJSONArray("translations");
        String[] translations;
        if (jsonExamples != null) {
            translations = new String[jsonTranslations.length()];
            for(int i = 0; i < jsonTranslations.length(); i++){
                translations[i] = jsonTranslations.getString(i);
            }
        } else {
            translations = new String[0];
        }

        JSONArray jsonMeasures = json.optJSONArray("measure");
        String[] measures;
        if (jsonMeasures != null) {
            measures = new String[jsonMeasures.length()];
            for(int i = 0; i < jsonMeasures.length(); i++){
                measures [i] = jsonMeasures.getString(i);
            }
        } else {
            measures = new String[0];
        }

        return new Word(
                json.getLong("id"),
                json.getString(PushDbContract.Vocabulary.COLUMN_HEAD_WORD),
                getTonal(json.optJSONObject(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION)),
                translations,
                measures,
                examples,
                json.optInt(PushDbContract.Vocabulary.COLUMN_LEVEL),
                json.optBoolean("audio", false),
                json.optBoolean("image", false)
        );
    }

    public static Word from_id(Context context, Long id) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse(PushDbContract.Vocabulary.CONTENT_URI + "/" + String.valueOf(id)),
                null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return Word.from_cursor(cursor);
        }  else {
            return null;
        }
    }

    public Long getId() {
        return id;
    }

    public String getHeadWord() {
        return headWord;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String arrayToString(Object[] array) {
        if (array != null){
            StringBuilder partialTranslations = new StringBuilder();
            for(int i = 0; i < array.length; i++){
                if (i > 0) {
                    partialTranslations.append(", ");
                }
                partialTranslations.append(array[i]);
            }
            return partialTranslations.toString();
        } else {
            return "";
        }
    }

    public String getTranslation() {
        return arrayToString(translations);
    }

    public String[] getTranslations() {
        return translations;
    }

    public int getLevel() {
        return level;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void bury(boolean bury) {
        buried = bury;
    }

    public boolean hasPronunciation() {
        return pronunciation != null;
    }

    public boolean hasImage() {
        return image;
    }

    public boolean hasAudio() {
        return audio;
    }

    public Long[] getExampleIds() {
        return examples;
    }

    public String[] getExamples(Context context, boolean masked) {
        Long[] exampleIds = getExampleIds();
        String[] examplesList = new String[exampleIds.length];
        for (int index = 0; index < exampleIds.length; index++) {
            Sentence sentence = Sentence.from_id(exampleIds[index], context);
            if (sentence != null) {
                if (masked) {
                    String mask = "__";
                    for (int head_word_index = 1; head_word_index < getHeadWord().length(); head_word_index++) {
                        mask += " __";
                    }
                    examplesList[index] = sentence.getRawSentence().replaceAll(this.headWord,
                            this.headWord.replaceAll(getHeadWord(), mask));
                } else {
                    examplesList[index] = sentence.getRawSentence();
                }
            }
        }
        return examplesList;
    }

    public String getExamplesString(Context context) {
        StringBuilder partialString = new StringBuilder();
        for (String sentenceText : getExamples(context, false)) {
            if (sentenceText != null) {
                partialString.append(sentenceText);
                partialString.append("\n");
            }
        }
        return partialString.toString();
    }

    public String[] getMeasures() {
        return measures;
    }

    public String getMeasuresString() {
        return arrayToString(measures);
    }

    public Uri persist(Context context) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PushDbContract.Vocabulary.COLUMN_ID, this.getId());
            contentValues.put(PushDbContract.Vocabulary.COLUMN_HEAD_WORD,this.getHeadWord());
            contentValues.put(PushDbContract.Vocabulary.COLUMN_PRONUNCIATION,this.getPronunciation());
            contentValues.put(PushDbContract.Vocabulary.COLUMN_TRANSLATION,this.getTranslation());
            contentValues.put(PushDbContract.Vocabulary.COLUMN_MEASURES,this.getMeasuresString());
            contentValues.put(PushDbContract.Vocabulary.COLUMN_EXAMPLES, arrayToString(this.getExampleIds()));
            contentValues.put(PushDbContract.Vocabulary.COLUMN_LEVEL,this.getLevel());
            if (hasAudio()) {
                contentValues.put(PushDbContract.Vocabulary.COLUMN_AUDIO, true);
            }
            if (hasImage()) {
                contentValues.put(PushDbContract.Vocabulary.COLUMN_IMAGE, true);
            }

            // description is column in items table, item.description has value for description
            word_uri =context.getContentResolver().insert(PushDbContract.Vocabulary.CONTENT_URI, contentValues);
            return word_uri;
    }

    public void delete(Context context) {
        if (word_uri != null) {
            int word_deleted = context.getContentResolver().delete(word_uri, null, null);
            if (word_deleted == 1) {
                Toast.makeText(context, "Deleted correctly", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(context, "Couldnt delete it", Toast.LENGTH_SHORT);
            }
        } else {
            Log.w("Word", "Word hasnt been saved yet!");
        }
    }
}
