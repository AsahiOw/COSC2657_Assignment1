package com.example.colormixingquiz.Model.Repository;

import android.content.Context;
import android.util.Log;
import com.example.colormixingquiz.Model.Data.Question;
import com.example.colormixingquiz.Model.Data.FileHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionRepository {
    private static final String TAG = "QuestionRepository";
    private static final String QUESTIONS_FILE = "questions.json";

    private List<Question> questions;
    private static QuestionRepository instance;
    private final Context context;
    private final FileHelper fileHelper;

    private QuestionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.fileHelper = new FileHelper(context, QUESTIONS_FILE);
        this.questions = new ArrayList<>();
        loadQuestions();
    }

    public static QuestionRepository getInstance(Context context) {
        if (instance == null) {
            instance = new QuestionRepository(context);
        }
        return instance;
    }

    private void loadQuestions() {
        try {
            // First try to read from internal storage
            String jsonContent = fileHelper.readJsonFromInternal();
            parseQuestionsFromJson(jsonContent);
            Log.d(TAG, "Successfully loaded " + questions.size() + " questions from internal storage");
        } catch (IOException e) {
            Log.d(TAG, "No existing questions in internal storage, loading from assets");
            try {
                // If internal storage fails, read from assets and save to internal
                String jsonContent = fileHelper.readJsonFromAssets();
                fileHelper.writeJsonToInternal(jsonContent);
                parseQuestionsFromJson(jsonContent);
                Log.d(TAG, "Successfully loaded " + questions.size() + " questions from assets");
            } catch (IOException | JSONException e2) {
                Log.e(TAG, "Error loading questions from assets: " + e2.getMessage());
                loadDefaultQuestions();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing questions JSON: " + e.getMessage());
            loadDefaultQuestions();
        }
    }

    private void parseQuestionsFromJson(String jsonContent) throws JSONException {
        questions.clear();
        JSONObject jsonObject = new JSONObject(jsonContent);
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
    }

    private void loadDefaultQuestions() {
        Log.d(TAG, "Loading default questions");
        questions.clear();
        questions.add(new Question(1, "Red", "Blue", "Purple"));
        questions.add(new Question(2, "Yellow", "Blue", "Green"));
        questions.add(new Question(3, "Red", "Yellow", "Orange"));
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public List<Question> getUnansweredQuestions() {
        return questions.stream()
                .filter(q -> !q.isAnswered())
                .collect(Collectors.toList());
    }

    public void resetAllAnswers() {
        questions.forEach(q -> q.setAnswered(false));
    }
}