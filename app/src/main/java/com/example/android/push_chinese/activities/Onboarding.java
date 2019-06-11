package com.example.android.push_chinese.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.push_chinese.R;
import com.example.android.push_chinese.entities.Sentence;
import com.example.android.push_chinese.entities.Word;
import com.example.android.push_chinese.utilities.Helper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Onboarding  extends AppCompatActivity {

    boolean vocabulary_downloaded = false;
    int level = 0;
    /** Tag for the log messages */
    public static final String LOG_TAG = Onboarding.class.getSimpleName();
    Onboarding activity_context;
    public View progressBar;
    public ProgressBar progressBar2;
    TextView downloading;
    private static final String BASE_VOCABULARY_REQUEST_URL =
            "https://pushchinese.000webhostapp.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);
        downloading = findViewById(R.id.sentence_download);
        activity_context = this;
        progressBar = activity_context.findViewById(R.id.progressBar);
        progressBar2 = activity_context.findViewById(R.id.progressBar2);
        // Kick off an {@link AsyncTask} to perform the network request
        ((Button) findViewById(R.id.level_1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(1);
            }
        });

        ((Button) findViewById(R.id.level_2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(2);
            }
        });;

        ((Button) findViewById(R.id.level_3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(3);
            }
        });;

        ((Button) findViewById(R.id.level_4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(4);
            }
        });;

        ((Button) findViewById(R.id.level_5)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(5);
            }
        });;

        ((Button) findViewById(R.id.level_6)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForLevel(6);
            }
        });;
    }

    private void prepareForLevel(int level) {
        this.level = level;
        if(!vocabulary_downloaded) {
            VocabularyAsyncTask task = new VocabularyAsyncTask();
            task.execute(BASE_VOCABULARY_REQUEST_URL  + "level/" + level);
        } else {
            SentencesAsyncTask task = new SentencesAsyncTask();
            task.execute(BASE_VOCABULARY_REQUEST_URL  + "sentences/" + level);
        }
    }

    /**
     * Update the screen to display information from the given {@link Word}.
     */
    private void redirect() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("onboarded",true);
        editor.putInt("initializedDay", Helper.daysSinceEpoch());
        editor.putInt("level", level);

        editor.apply();

        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(mainIntent);
    }

    /**
     * Returns a formatted date and time string for when the earthquake happened.
     */
    private String getDateString(long timeInMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss z");
        return formatter.format(timeInMilliseconds);
    }

    private abstract class GeneralAsyncTask extends AsyncTask<String, Integer, ArrayList<Object>> {

        /**
         * Returns new URL object from the given string URL.
         */
        protected URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        protected String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Unexpected response from server for " + url.toString() +
                            " with status " + String.valueOf(responseCode));
                    jsonResponse = null;
                }
            } catch (IOException e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
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

        @Override
        protected ArrayList<Object> doInBackground(String... stringUrls) {
            // Create URL object
            URL url = createUrl(stringUrls[0]);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not perform Http request", e);
            }

            if (jsonResponse != null) {
                // Extract relevant fields from the JSON response and create an {@link Event} object
                ArrayList<Sentence> sentences_list = extractSentencesFromJson(jsonResponse);
                ArrayList<Object> objectList = new ArrayList<>();
                Integer progress = 0;
                for (Sentence currentSentence : sentences_list) {
                    currentSentence.persist(activity_context);
                    objectList.add(currentSentence);
                    progress++;
                    publishProgress(progress);
                }
                // Return the {@link Event} object as the result fo the {@link VocabularyAsyncTask}
                return objectList;
            } else {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            downloading.setText("Running..."+ values[0]);
            progressBar2.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //        Comes back to the main thread
        @Override
        protected void onPostExecute(ArrayList<Object> objectList) {
            if (objectList == null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(activity_context, "There was a problem fetching the data", Toast.LENGTH_LONG).show();
                return;
            } else {
                progressBar.setVisibility(View.GONE);
                redirect();
            }
        }

        private ArrayList<Sentence> extractSentencesFromJson(String sentencesJSON) {
            ArrayList<Sentence> sentencesList = new ArrayList<>();
            try {
                JSONArray baseJsonArrayResponse = new JSONArray(sentencesJSON);

                // If there are results in the array
                for (int index = 0; index < baseJsonArrayResponse.length(); index++) {
                    sentencesList.add(Sentence.from_json(baseJsonArrayResponse.getJSONObject(index)));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the sentences JSON results", e);
            }
            return sentencesList;
        }
    }

    private class VocabularyAsyncTask extends GeneralAsyncTask {

        @Override
        protected ArrayList<Object> doInBackground(String... stringUrls) {
            // Create URL object
            URL url = createUrl(stringUrls[0]);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not perform Http request", e);
            }

            if (jsonResponse != null) {
                // Extract relevant fields from the JSON response and create an {@link Event} object
                ArrayList<Word> word_list = extractWordsFromJson(jsonResponse);
                ArrayList<Object> objectList = new ArrayList<>();
                for (Word currentWord : word_list) {
                    currentWord.store(activity_context);
                    objectList.add(currentWord);
                }
                // Return the {@link Event} object as the result fo the {@link VocabularyAsyncTask}
                return objectList;
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //        Comes back to the main thread
        @Override
        protected void onPostExecute(ArrayList<Object> word_list) {
            if (word_list == null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(activity_context, "There was a problem fetching the data", Toast.LENGTH_LONG).show();
                return;
            } else {
                vocabulary_downloaded = true;
                SentencesAsyncTask task = new SentencesAsyncTask();
                task.execute(BASE_VOCABULARY_REQUEST_URL  + "sentences/" + level);
            }
        }

        private ArrayList<Word> extractWordsFromJson(String vocabularyJSON) {
            ArrayList<Word> wordList = new ArrayList<>();
            try {
                JSONArray baseJsonArrayResponse = new JSONArray(vocabularyJSON);

                // If there are results in the array
                for (int index = 0; index < baseJsonArrayResponse.length(); index++) {
                    // Create a new {@link Word} object
                    wordList.add(Word.from_json(baseJsonArrayResponse.getJSONObject(index)));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the vocabulary JSON results", e);
            }
            return wordList;
        }
    }
}