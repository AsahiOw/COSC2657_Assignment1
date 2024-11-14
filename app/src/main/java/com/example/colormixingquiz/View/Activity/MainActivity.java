package com.example.colormixingquiz.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.colormixingquiz.Controller.GameController;
import com.example.colormixingquiz.R;
import com.example.colormixingquiz.View.Fragment.GamePreferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private Button playButton;
    private Button optionsButton;
    private FloatingActionButton historyButton;
    private GameController gameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize game controller with debug logging
        try {
            gameController = new GameController(getFilesDir().getAbsolutePath(), this);
            Log.d("MainActivity", "GameController initialized successfully");

            // Test questions are loaded
            if (gameController.hasUnansweredQuestions()) {
                Log.d("MainActivity", "Questions loaded successfully");
            } else {
                Log.w("MainActivity", "No questions loaded");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing GameController: " + e.getMessage());
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        playButton = findViewById(R.id.playButton);
        optionsButton = findViewById(R.id.optionsButton);
        historyButton = findViewById(R.id.historyButton);
    }

    private void setupClickListeners() {
        playButton.setOnClickListener(v -> showGameStartDialog());
        optionsButton.setOnClickListener(v -> startOptionsActivity());
        historyButton.setOnClickListener(v -> startHistoryActivity());
    }

    private void showGameStartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_start, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button newGameButton = dialogView.findViewById(R.id.newGameButton);
        Button continueButton = dialogView.findViewById(R.id.continueButton);

        // Check if there's a game in progress
        boolean hasGameInProgress = GamePreferences.hasGameInProgress(this);
        continueButton.setEnabled(hasGameInProgress);
        continueButton.setAlpha(hasGameInProgress ? 1.0f : 0.5f);

        newGameButton.setOnClickListener(v -> {
            dialog.dismiss();
            showGameModeDialog();
        });

        continueButton.setOnClickListener(v -> {
            if (hasGameInProgress) {
                dialog.dismiss();
                startGameActivity(false);
            }
        });

        dialog.show();
    }

    private void showGameModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_mode, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button allQuestionsButton = dialogView.findViewById(R.id.allQuestionsButton);
        Button unansweredButton = dialogView.findViewById(R.id.unansweredButton);

        // Check if there are unanswered questions
        boolean hasUnansweredQuestions = gameController.hasUnansweredQuestions();
        unansweredButton.setEnabled(hasUnansweredQuestions);
        unansweredButton.setAlpha(hasUnansweredQuestions ? 1.0f : 0.5f);

        allQuestionsButton.setOnClickListener(v -> {
            dialog.dismiss();
            startGameActivity(true);
        });

        unansweredButton.setOnClickListener(v -> {
            if (hasUnansweredQuestions) {
                dialog.dismiss();
                startGameActivity(true, true);
            }
        });

        dialog.show();
    }

    private void startGameActivity(boolean isNewGame) {
        startGameActivity(isNewGame, false);
    }

    private void startGameActivity(boolean isNewGame, boolean onlyUnanswered) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("isNewGame", isNewGame);
        intent.putExtra("onlyUnanswered", onlyUnanswered);
        startActivity(intent);
    }

    private void startOptionsActivity() {
        Intent intent = new Intent(this, OptionsActivity.class);
        startActivity(intent);
    }

    private void startHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}