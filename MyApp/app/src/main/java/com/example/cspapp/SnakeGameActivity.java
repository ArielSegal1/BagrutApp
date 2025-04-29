package com.example.cspapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeGameActivity extends AppCompatActivity {

    private SnakeGameView gameView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get game details from intent
        int gameSpeed = getIntent().getIntExtra("GAME_SPEED", 10);
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        String musicUrl = getIntent().getStringExtra("MUSIC_URL");

        // Create and set the game view with speed and image URL
        gameView = new SnakeGameView(this, gameSpeed, imageUrl);
        setContentView(gameView);

        // Setup background music if available
        if (musicUrl != null && !musicUrl.isEmpty()) {
            setupBackgroundMusic(musicUrl);
        }
    }

    private void setupBackgroundMusic(String musicUrl) {
        try {
            // Initialize the media player
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setLooping(true); // Loop the music
            mediaPlayer.prepare();

            // Start playing when ready
            mediaPlayer.setOnPreparedListener(mp -> mp.start());

        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, just continue without music
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();

        // Resume music if it was playing
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();

        // Pause music when game is paused
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release media player resources
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}