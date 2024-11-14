package com.example.colormixingquiz.View.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
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
    private MediaPlayer correctSound;
    private MediaPlayer incorrectSound;
    private MediaPlayer backgroundMusic;
    private float sfxVolume;
    private float musicVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            // Initialize game controller
            gameController = new GameController();

            // Get game mode from intent
            boolean isNewGame = getIntent().getBooleanExtra("isNewGame", true);
            boolean onlyUnanswered = getIntent().getBooleanExtra("onlyUnanswered", false);

            initializeViews();
            setupClickListeners();
            initializeAudio();

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

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> showExitConfirmDialog());
        finishButton.setOnClickListener(v -> finishGame());

        for (Button button : answerButtons) {
            button.setOnClickListener(v -> handleAnswer((Button) v));
        }
    }

    private void initializeAudio() {
        correctSound = MediaPlayer.create(this, R.raw.correct);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect);
        backgroundMusic = MediaPlayer.create(this, R.raw.game_music);

        sfxVolume = GamePreferences.getSfxVolume(this);
        musicVolume = GamePreferences.getMusicVolume(this);

        correctSound.setVolume(sfxVolume, sfxVolume);
        incorrectSound.setVolume(sfxVolume, sfxVolume);
        backgroundMusic.setVolume(musicVolume, musicVolume);

        backgroundMusic.setLooping(true);
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
        // Disable all buttons to prevent multiple answers
        for (Button button : answerButtons) {
            button.setEnabled(false);
        }

        String selectedAnswer = selectedButton.getText().toString();
        boolean isCorrect = gameController.checkAnswer(selectedAnswer);

        // Change button color based on answer
        int color = isCorrect ?
                getResources().getColor(R.color.correct_answer, null) :
                getResources().getColor(R.color.wrong_answer, null);
        selectedButton.setBackgroundColor(color);

        // Play appropriate sound
        if (isCorrect) {
            correctSound.start();
        } else {
            incorrectSound.start();
        }

        // Save progress
        GamePreferences.saveGameProgress(this,
                gameController.getCurrentQuestionIndex(),
                gameController.getCorrectAnswers());

        // Wait before loading next question
        new Handler().postDelayed(() -> {
            gameController.nextQuestion();
            loadQuestion();
        }, 1000);
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_confirmation)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    GamePreferences.saveGameProgress(this,
                            gameController.getCurrentQuestionIndex(),
                            gameController.getCorrectAnswers());
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void finishGame() {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("correctAnswers", gameController.getCorrectAnswers());
        intent.putExtra("totalQuestions", gameController.getTotalQuestions());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (correctSound != null) {
                correctSound.release();
            }
            if (incorrectSound != null) {
                incorrectSound.release();
            }
            if (backgroundMusic != null) {
                backgroundMusic.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        } finally {
            super.onDestroy();
        }
    }
}