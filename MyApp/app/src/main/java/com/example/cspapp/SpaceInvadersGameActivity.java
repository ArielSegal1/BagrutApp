package com.example.cspapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SpaceInvadersGameActivity extends AppCompatActivity {

    private SpaceInvadersGameView gameView;
    private Button btnLeft, btnRight, btnFire;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the content view with the layout that has our buttons
        setContentView(R.layout.activity_space_invaders_game);

        // Get the container for our game view
        FrameLayout gameContainer = findViewById(R.id.gameContainer);

        // Get speed from intent
        int gameSpeed = getIntent().getIntExtra("GAME_SPEED", 10);

        // Create and add the game view with speed
        gameView = new SpaceInvadersGameView(this, gameSpeed);
        gameContainer.addView(gameView);

        // Set background image if provided
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        if (imageUrl != null) {
            gameView.setBackgroundImageUrl(imageUrl);
        }

        // Setup background music if available
        String musicUrl = getIntent().getStringExtra("MUSIC_URL");
        if (musicUrl != null && !musicUrl.isEmpty()) {
            setupBackgroundMusic(musicUrl);
        }

        // Set up control buttons
        setupControlButtons();
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

    private void setupControlButtons() {
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnFire = findViewById(R.id.btnFire);

        // Set up left movement button
        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    gameView.setMovingLeft(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gameView.setMovingLeft(false);
                    break;
            }
            return true;
        });

        // Set up right movement button
        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    gameView.setMovingRight(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gameView.setMovingRight(false);
                    break;
            }
            return true;
        });

        // Set up fire button
        btnFire.setOnClickListener(v -> gameView.firePlayerBullet());
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