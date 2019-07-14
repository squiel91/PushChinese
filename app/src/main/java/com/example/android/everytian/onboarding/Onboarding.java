package com.example.android.everytian.onboarding;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.everytian.R;
import com.example.android.everytian.activities.MainActivity;
import com.example.android.everytian.entities.Sentence;
import com.example.android.everytian.entities.Word;
import com.example.android.everytian.utilities.Helper;
import com.example.android.everytian.utilities.SimpleFragmentPagerAdaptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Onboarding extends AppCompatActivity implements NotifyChange {

    private Integer studyWords = null;
    private Boolean traditional = null;

    @Override
    public void numberOfStudyWords(int quantity) {
        studyWords = quantity;
    }

    @Override
    public void studyTraditional(boolean traditional) {
        this.traditional = traditional;
    }

    boolean finishedLoading = false;
    boolean finishedOnboarding = false;

    public static final String LOG_TAG = Onboarding.class.getSimpleName();

    ProgressBar loadingBar;
    TextView loadingDescription;
    TextView loadingProgress;
    Button startLearningButton;
    OnboardingNavigator navigator;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.onboarding);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        OnboardingSliderAdaptor adapter = new OnboardingSliderAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        navigator = findViewById(R.id.navigation);
        navigator.setPageNumber(4);
        navigator.setOnPageChangeRequest(new OnboardingNavigator.OnPageChangeRequest() {
            @Override
            public void changeToPage(int index) {
                viewPager.setCurrentItem(index, true);
            }
        });

        navigator.setOnFinishOnboarding(new OnboardingNavigator.OnFinishOnboarding() {
            @Override
            public void onFinishOnboarding() {
                final View onboardingContainer = findViewById(R.id.onboarding_container);
                YoYo.with(Techniques.TakingOff).duration(400).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        onboardingContainer.setVisibility(View.GONE);
                    }
                }).playOn(onboardingContainer);
                finishedOnboarding = true;
                canContinue();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                navigator.changeToPage(position);
                Log.w("POSITION", "" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        loadingDescription = this.findViewById(R.id.loading_description);
        loadingBar = this.findViewById(R.id.loading_bar);

        loadingDescription.setText("Loading Sentences (1/2)");
        SentencesAsyncTask sentenceTask = new SentencesAsyncTask();
        sentenceTask.execute();
    }

    private void redirect() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("onboarded",true);
        editor.putInt("initializedDay", Helper.daysSinceEpoch());

        editor.apply();

        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(mainIntent);
    }

    private abstract class GeneralAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String jsonString = null;
            try {
                jsonString = readFromStream(getApplicationContext().getResources().openRawResource(getResourceId()));
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not load json file", e);
            }

            if (jsonString != null) {
                ArrayList<Sentence> sentencesList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray = new JSONArray(jsonString);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Problem parsing the sentences JSON results", e);
                }

                // If there are results in the array
                Integer progress = 0;
                Integer totalObjects = jsonArray.length();
                for (int index = 0; index < totalObjects; index++) {
                    try {
                        processJsonObject(jsonArray.getJSONObject(index));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        progress++;
                        publishProgress(progress, totalObjects);
                    }
                }
            }
            return null;
        }

        protected abstract int getResourceId();

        protected abstract void processJsonObject(JSONObject jsonObject);

        @Override
        protected void onProgressUpdate(Integer ...values) {
            loadingBar.setProgress((int)((values[0] / (float)values[1]) * 100));
        }

        protected String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }
    }

    private class SentencesAsyncTask extends GeneralAsyncTask {

        protected int getResourceId() {
            return R.raw.sentences;
        }

        protected void processJsonObject(JSONObject jsonObject) {
            try {
                Sentence.from_json(jsonObject).persist(getApplicationContext());
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Could not parse Word from JSON", e);
            }
        }

        @Override
        protected void onPostExecute(Void voids) {
            loadingDescription.setText("Loading Vocabulary (2/2)");
            VocabularyAsyncTask vocabularyTask = new VocabularyAsyncTask();
            vocabularyTask.execute();

        }
    }

    private class VocabularyAsyncTask extends GeneralAsyncTask {

        protected int getResourceId() {
            return R.raw.vocabulary;
        }

        protected void processJsonObject(JSONObject jsonObject) {
            try {
                Word.from_json(jsonObject).store(getApplicationContext());
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Could not parse Word from JSON", e);
            }
        }

        @Override
        protected void onPostExecute(Void voids) {
            loadingBar.setIndeterminate(true);
            loadingDescription.setText("Last adjustments");
            finishedLoading = true;
            canContinue();
        }
    }

    private void canContinue() {
        if (finishedLoading && finishedOnboarding) redirect();
    }
}