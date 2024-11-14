package com.example.colormixingquiz.View.Activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.colormixingquiz.R;
import com.example.colormixingquiz.Controller.GameController;
import com.example.colormixingquiz.View.Fragment.GamePreferences;

public class OptionsActivity extends AppCompatActivity {
    private ImageButton backButton;
    private SeekBar sfxSlider;
    private SeekBar musicSlider;
    private Button resetButton;
    private MediaPlayer testSoundPlayer;
    private GameController gameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        gameController = new GameController();

        initializeViews();
        setupClickListeners();
        loadCurrentSettings();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        sfxSlider = findViewById(R.id.sfxSlider);
        musicSlider = findViewById(R.id.musicSlider);
        resetButton = findViewById(R.id.resetButton);

        // Set up sliders
        sfxSlider.setMax(100);
        musicSlider.setMax(100);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        resetButton.setOnClickListener(v -> showResetConfirmDialog());

        sfxSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                GamePreferences.saveVolumes(OptionsActivity.this, volume,
                        GamePreferences.getMusicVolume(OptionsActivity.this));

                // Play test sound
                playTestSound();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        musicSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                GamePreferences.saveVolumes(OptionsActivity.this,
                        GamePreferences.getSfxVolume(OptionsActivity.this), volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadCurrentSettings() {
        float sfxVolume = GamePreferences.getSfxVolume(this);
        float musicVolume = GamePreferences.getMusicVolume(this);

        sfxSlider.setProgress((int)(sfxVolume * 100));
        musicSlider.setProgress((int)(musicVolume * 100));
    }

    private void showResetConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirmation_title)
                .setMessage(R.string.reset_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Reset all game data
                    gameController.resetAllAnswers();
                    GamePreferences.clearGameProgress(this);
                    showResetSuccessDialog();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showResetSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_success_title)
                .setMessage(R.string.reset_success_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void playTestSound() {
        if (testSoundPlayer != null) {
            testSoundPlayer.release();
        }

        testSoundPlayer = MediaPlayer.create(this, R.raw.button_click);
        if (testSoundPlayer != null) {
            float volume = GamePreferences.getSfxVolume(this);
            testSoundPlayer.setVolume(volume, volume);
            testSoundPlayer.setOnCompletionListener(MediaPlayer::release);
            testSoundPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testSoundPlayer != null) {
            testSoundPlayer.release();
            testSoundPlayer = null;
        }
    }
}