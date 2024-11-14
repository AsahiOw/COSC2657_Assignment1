package com.example.colormixingquiz.View.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.colormixingquiz.Model.Data.Question;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GamePreferences {
    private static final String TAG = "GamePreferences";
    private static final String PREFS_NAME = "ColorMixQuizPrefs";
    private static final String KEY_GAME_IN_PROGRESS = "gameInProgress";
    private static final String KEY_CURRENT_QUESTION = "currentQuestion";
    private static final String KEY_CORRECT_ANSWERS = "correctAnswers";
    private static final String KEY_SFX_VOLUME = "sfxVolume";
    private static final String KEY_MUSIC_VOLUME = "musicVolume";
    private static final String KEY_ANSWERED_QUESTIONS = "answeredQuestions";

    public static void saveGameState(Context context, List<Question> questions,
                                     int currentQuestion, int correctAnswers) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE).edit();

            // Save basic game state
            editor.putBoolean(KEY_GAME_IN_PROGRESS, true);
            editor.putInt(KEY_CURRENT_QUESTION, currentQuestion);
            editor.putInt(KEY_CORRECT_ANSWERS, correctAnswers);

            // Save answered questions state
            JSONArray answeredQuestions = new JSONArray();
            for (Question q : questions) {
                if (q.isAnswered()) {
                    answeredQuestions.put(q.getId());
                }
            }
            editor.putString(KEY_ANSWERED_QUESTIONS, answeredQuestions.toString());

            editor.apply();
            Log.d(TAG, "Game state saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving game state: " + e.getMessage());
        }
    }

    public static void restoreQuestionState(Context context, List<Question> questions) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String answeredQuestionsJson = prefs.getString(KEY_ANSWERED_QUESTIONS, "[]");

        try {
            JSONArray answeredQuestions = new JSONArray(answeredQuestionsJson);
            for (int i = 0; i < answeredQuestions.length(); i++) {
                int answeredId = answeredQuestions.getInt(i);
                for (Question q : questions) {
                    if (q.getId() == answeredId) {
                        q.setAnswered(true);
                        break;
                    }
                }
            }
            Log.d(TAG, "Question states restored successfully");
        } catch (JSONException e) {
            Log.e(TAG, "Error restoring question states: " + e.getMessage());
        }
    }

    public static boolean hasGameInProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_GAME_IN_PROGRESS, false);
    }

    public static GameProgress getGameProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new GameProgress(
                prefs.getInt(KEY_CURRENT_QUESTION, 0),
                prefs.getInt(KEY_CORRECT_ANSWERS, 0)
        );
    }

    public static void clearGameProgress(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_GAME_IN_PROGRESS, false);
        editor.remove(KEY_CURRENT_QUESTION);
        editor.remove(KEY_CORRECT_ANSWERS);
        // Don't clear KEY_ANSWERED_QUESTIONS as we want to maintain the overall progress
        editor.apply();
        Log.d(TAG, "Game progress cleared");
    }

    public static void saveVolumes(Context context, float sfxVolume, float musicVolume) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putFloat(KEY_SFX_VOLUME, sfxVolume);
        editor.putFloat(KEY_MUSIC_VOLUME, musicVolume);
        editor.apply();
    }

    public static float getSfxVolume(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_SFX_VOLUME, 1.0f);
    }

    public static float getMusicVolume(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_MUSIC_VOLUME, 1.0f);
    }

    public static class GameProgress {
        public final int currentQuestion;
        public final int correctAnswers;

        public GameProgress(int currentQuestion, int correctAnswers) {
            this.currentQuestion = currentQuestion;
            this.correctAnswers = correctAnswers;
        }
    }
}