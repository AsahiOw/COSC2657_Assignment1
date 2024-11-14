package com.example.colormixingquiz.View.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.colormixingquiz.R;
import com.example.colormixingquiz.Controller.GameController;

public class HistoryActivity extends AppCompatActivity {
    private ImageButton backButton;
    private TextView statsText;
    private TextView percentageText;
    private ProgressBar progressBar;
    private GameController gameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        gameController = new GameController();

        initializeViews();
        setupClickListeners();
        displayStatistics();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        statsText = findViewById(R.id.statsText);
        percentageText = findViewById(R.id.percentageText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void displayStatistics() {
        GameController.GameStatistics statistics = gameController.getGameStatistics();

        // Update the statistics text
        String statsFormat = getString(R.string.stats_format);
        statsText.setText(String.format(statsFormat,
                statistics.getAnsweredQuestions(),
                statistics.getTotalQuestions()));

        // Update the percentage text
        float percentage = statistics.getCompletionPercentage();
        String percentFormat = getString(R.string.percentage_format);
        percentageText.setText(String.format(percentFormat, percentage));

        // Update progress bar
        progressBar.setMax(statistics.getTotalQuestions());
        progressBar.setProgress(statistics.getAnsweredQuestions());

        // Display appropriate message based on progress
        TextView messageText = findViewById(R.id.messageText);
        if (percentage == 100) {
            messageText.setText(R.string.history_complete);
        } else if (percentage >= 50) {
            messageText.setText(R.string.history_good_progress);
        } else {
            messageText.setText(R.string.history_keep_going);
        }
    }
}