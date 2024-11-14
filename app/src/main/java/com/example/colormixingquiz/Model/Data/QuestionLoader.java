package com.example.colormixingquiz.Model.Data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {
    private static final String TAG = "QuestionLoader";
    private static final String QUESTIONS_FILE = "questions.json";
    private final Context context;

    public QuestionLoader(Context context) {
        this.context = context;
    }

    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        try {
            String jsonString = loadJsonFromAssets();
            questions = parseQuestionsFromJson(jsonString);
            Log.d(TAG, "Successfully loaded " + questions.size() + " questions");
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading questions: " + e.getMessage());
            questions = getDefaultQuestions();
        }
        return questions;
    }

    private String loadJsonFromAssets() throws IOException {
        StringBuilder jsonString = new StringBuilder();
        try (InputStream is = context.getAssets().open(QUESTIONS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON file from assets: " + e.getMessage());
            throw e;
        }
        return jsonString.toString();
    }

    private List<Question> parseQuestionsFromJson(String jsonString) throws JSONException {
        List<Question> questions = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObj = questionsArray.getJSONObject(i);
                questions.add(new Question(
                        questionObj.getInt("id"),
                        questionObj.getString("firstColor"),
                        questionObj.getString("secondColor"),
                        questionObj.getString("correctMixColor")
                ));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            throw e;
        }
        return questions;
    }

    private List<Question> getDefaultQuestions() {
        List<Question> defaultQuestions = new ArrayList<>();
        defaultQuestions.add(new Question(1, "Red", "Blue", "Purple"));
        defaultQuestions.add(new Question(2, "Yellow", "Blue", "Green"));
        defaultQuestions.add(new Question(3, "Red", "Yellow", "Orange"));
        Log.d(TAG, "Loaded default questions");
        return defaultQuestions;
    }
}