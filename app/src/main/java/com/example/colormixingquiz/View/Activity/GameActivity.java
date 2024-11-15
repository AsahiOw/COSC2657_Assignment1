package com.example.colormixingquiz.View.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.colormixingquiz.Controller.GameController;
import com.example.colormixingquiz.Model.Data.Question;
import com.example.colormixingquiz.R;
import com.example.colormixingquiz.View.Fragment.GamePreferences;

import java.util.List;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    private ImageButton backButton;
    private Button finishButton;
    private TextView questionText;
    private Button[] answerButtons;
    private GameController gameController;

    // Audio components
    private SoundPool soundPool;
    private int correctSoundId;
    private int incorrectSoundId;
    private MediaPlayer backgroundMusic;
    private float sfxVolume;
    private float musicVolume;
    private boolean soundsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // Initialize game controller with context
            gameController = new GameController(getFilesDir().getAbsolutePath(), this);

            // Get game mode from intent
            boolean isNewGame = getIntent().getBooleanExtra("isNewGame", true);
            boolean onlyUnanswered = getIntent().getBooleanExtra("onlyUnanswered", false);

            initializeViews();
            initializeAudio();
            setupClickListeners();

            if (isNewGame) {
                try {
                    gameController.startNewGame(onlyUnanswered);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error starting new game: " + e.getMessage());
                    showErrorAndFinish(getString(R.string.error_no_questions));
                    return;
                }
            } else {
                // Resume previous game
                GamePreferences.GameProgress progress = GamePreferences.getGameProgress(this);
                try {
                    gameController.resumeGame(progress);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error resuming game: " + e.getMessage());
                    showErrorAndFinish(getString(R.string.error_resume_game));
                    return;
                }
            }

            loadQuestion();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            showErrorAndFinish(getString(R.string.error_general));
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        finishButton = findViewById(R.id.finishButton);
        questionText = findViewById(R.id.questionText);

        answerButtons = new Button[4];
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
    }

    private void initializeAudio() {
        // Initialize SoundPool with proper attributes
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attributes)
                .build();

        // Load sound effects
        correctSoundId = soundPool.load(this, R.raw.correct, 1);
        incorrectSoundId = soundPool.load(this, R.raw.incorrect, 1);

        // Set up background music
        backgroundMusic = MediaPlayer.create(this, R.raw.game_music);

        // Load volumes from preferences
        sfxVolume = GamePreferences.getSfxVolume(this);
        musicVolume = GamePreferences.getMusicVolume(this);

        // Set up background music properties
        backgroundMusic.setVolume(musicVolume, musicVolume);
        backgroundMusic.setLooping(true);

        // Set up sound loading callback
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundsLoaded = true;
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> showExitConfirmDialog());
        finishButton.setOnClickListener(v -> finishGame());

        for (Button button : answerButtons) {
            button.setOnClickListener(v -> handleAnswer((Button) v));
        }
    }

    private void loadQuestion() {
        try {
            Question currentQuestion = gameController.getCurrentQuestion();
            if (currentQuestion == null) {
                finishGame();
                return;
            }

            // Set question text
            String questionFormat = getString(R.string.question_format);
            questionText.setText(String.format(questionFormat,
                    currentQuestion.getFirstColor(),
                    currentQuestion.getSecondColor()));

            // Get and shuffle answers
            List<String> answers = gameController.getRandomizedAnswers();
            if (answers.size() != 4) {
                Log.e(TAG, "Invalid number of answers: " + answers.size());
                showErrorAndFinish(getString(R.string.error_invalid_answers));
                return;
            }

            // Set up answer buttons
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText(answers.get(i));
                answerButtons[i].setBackgroundColor(getResources().getColor(R.color.button_normal, null));
                answerButtons[i].setEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading question: " + e.getMessage());
            showErrorAndFinish(getString(R.string.error_loading_question));
        }
    }

    private void handleAnswer(Button selectedButton) {
        // Disable all buttons immediately
        for (Button button : answerButtons) {
            button.setEnabled(false);
        }

        String selectedAnswer = selectedButton.getText().toString();
        boolean isCorrect = gameController.checkAnswer(selectedAnswer);
        Question currentQuestion = gameController.getCurrentQuestion();
        String correctAnswer = currentQuestion.getCorrectMixColor();

        // Play sound
        if (soundsLoaded) {
            int soundId = isCorrect ? correctSoundId : incorrectSoundId;
            soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f);
        }

        if (isCorrect) {
            // If correct, just highlight the selected button in green
            selectedButton.setBackgroundColor(getResources().getColor(R.color.correct_answer, null));
        } else {
            // If wrong, highlight selected button in red and find & highlight correct answer in green
            selectedButton.setBackgroundColor(getResources().getColor(R.color.wrong_answer, null));
            // Find and highlight the correct answer button
            for (Button button : answerButtons) {
                if (button.getText().toString().equals(correctAnswer)) {
                    button.setBackgroundColor(getResources().getColor(R.color.correct_answer, null));
                    break;
                }
            }
        }

        // Move to next question after delay
        new Handler().postDelayed(() -> {
            gameController.nextQuestion();
            if (gameController.isGameFinished()) {
                finishGame();
            } else {
                loadQuestion();
            }
        }, 1500); // Delay 1.5s
    }


    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_confirmation)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void finishGame() {
        gameController.endGame();
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("correctAnswers", gameController.getCorrectAnswers());
        intent.putExtra("totalQuestions", gameController.getTotalQuestions());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
        gameController.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
            if (backgroundMusic != null) {
                backgroundMusic.release();
                backgroundMusic = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        } finally {
            super.onDestroy();
        }
    }
}