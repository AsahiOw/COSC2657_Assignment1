package com.example.colormixingquiz.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.colormixingquiz.Controller.GameController;
import com.example.colormixingquiz.R;
import com.example.colormixingquiz.View.Fragment.GamePreferences;

public class ResultsActivity extends AppCompatActivity {
    private TextView scoreText;
    private TextView totalQuestionsText;
    private TextView percentageText;
    private Button retryButton;
    private Button homeButton;

    private int correctAnswers;
    private int totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Get results from intent
        correctAnswers = getIntent().getIntExtra("correctAnswers", 0);
        totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        initializeViews();
        displayResults();
        setupClickListeners();

        // Clear the saved game progress
        GamePreferences.clearGameProgress(this);
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        totalQuestionsText = findViewById(R.id.totalQuestionsText);
        percentageText = findViewById(R.id.percentageText);
        retryButton = findViewById(R.id.retryButton);
        homeButton = findViewById(R.id.homeButton);
    }

    private void displayResults() {
        // Display score fraction
        scoreText.setText(String.format("%d/%d", correctAnswers, totalQuestions));

        // Display total questions text
        String questionsFormat = getString(R.string.total_questions_format);
        totalQuestionsText.setText(String.format(questionsFormat, totalQuestions));

        // Calculate and display percentage
        float percentage = (float) correctAnswers / totalQuestions * 100;
        String percentageFormat = getString(R.string.percentage_format);
        percentageText.setText(String.format(percentageFormat, percentage));

        // Set congratulatory message based on score
        TextView congratsText = findViewById(R.id.congratsText);
        if (percentage >= 80) {
            congratsText.setText(R.string.excellent_score);
        } else if (percentage >= 60) {
            congratsText.setText(R.string.good_score);
        } else {
            congratsText.setText(R.string.keep_practicing);
        }
    }

    private void setupClickListeners() {
        retryButton.setOnClickListener(v -> showGameModeDialog());
        homeButton.setOnClickListener(v -> {
            // Navigate back to MainActivity, clearing the activity stack
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showGameModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_mode, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button allQuestionsButton = dialogView.findViewById(R.id.allQuestionsButton);
        Button unansweredButton = dialogView.findViewById(R.id.unansweredButton);

        // Enable/disable unanswered questions button
        boolean hasUnanswered = new GameController().hasUnansweredQuestions();
        unansweredButton.setEnabled(hasUnanswered);
        unansweredButton.setAlpha(hasUnanswered ? 1.0f : 0.5f);

        allQuestionsButton.setOnClickListener(v -> {
            dialog.dismiss();
            startNewGame(false);
        });

        unansweredButton.setOnClickListener(v -> {
            if (hasUnanswered) {
                dialog.dismiss();
                startNewGame(true);
            }
        });

        dialog.show();
    }

    private void startNewGame(boolean onlyUnanswered) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("isNewGame", true);
        intent.putExtra("onlyUnanswered", onlyUnanswered);
        startActivity(intent);
        finish();
    }
}