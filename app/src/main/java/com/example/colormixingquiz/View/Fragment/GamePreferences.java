package com.example.colormixingquiz.View.Fragment;

import android.content.Context;
import android.content.SharedPreferences;

public class GamePreferences {
    private static final String PREFS_NAME = "ColorMixQuizPrefs";
    private static final String KEY_GAME_IN_PROGRESS = "gameInProgress";
    private static final String KEY_CURRENT_QUESTION = "currentQuestion";
    private static final String KEY_CORRECT_ANSWERS = "correctAnswers";
    private static final String KEY_SFX_VOLUME = "sfxVolume";
    private static final String KEY_MUSIC_VOLUME = "musicVolume";

    public static boolean hasGameInProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_GAME_IN_PROGRESS, false);
    }

    public static void saveGameProgress(Context context, int currentQuestion, int correctAnswers) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_GAME_IN_PROGRESS, true);
        editor.putInt(KEY_CURRENT_QUESTION, currentQuestion);
        editor.putInt(KEY_CORRECT_ANSWERS, correctAnswers);
        editor.apply();
    }

    public static void clearGameProgress(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_GAME_IN_PROGRESS, false);
        editor.remove(KEY_CURRENT_QUESTION);
        editor.remove(KEY_CORRECT_ANSWERS);
        editor.apply();
    }

    public static GameProgress getGameProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new GameProgress(
                prefs.getInt(KEY_CURRENT_QUESTION, 0),
                prefs.getInt(KEY_CORRECT_ANSWERS, 0)
        );
    }

    public static void saveVolumes(Context context, float sfxVolume, float musicVolume) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
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

        GameProgress(int currentQuestion, int correctAnswers) {
            this.currentQuestion = currentQuestion;
            this.correctAnswers = correctAnswers;
        }
    }
}