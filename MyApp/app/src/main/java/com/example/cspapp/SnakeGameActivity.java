package com.example.cspapp;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeGameActivity extends AppCompatActivity {

    private SnakeGameView gameView;

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

        // Create and set the game view with speed and image URL
        gameView = new SnakeGameView(this, gameSpeed, imageUrl);
        setContentView(gameView);
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