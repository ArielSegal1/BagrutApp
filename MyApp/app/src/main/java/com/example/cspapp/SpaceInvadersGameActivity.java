package com.example.cspapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SpaceInvadersGameActivity extends AppCompatActivity {

    private SpaceInvadersGameView gameView;
    private Button btnLeft, btnRight, btnFire;

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

        // Set up control buttons
        setupControlButtons();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}