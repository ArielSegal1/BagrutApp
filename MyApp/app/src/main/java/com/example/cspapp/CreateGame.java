package com.example.cspapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CreateGame extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Button btnChooseGame;
    private RecyclerView rvCreatedGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_game);

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnChooseGame = findViewById(R.id.btnChooseGame);
        rvCreatedGames = findViewById(R.id.rvCreatedGames);

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up button click listener
        btnChooseGame.setOnClickListener(v -> {
            startActivity(new Intent(CreateGame.this, SnakeGameActivity.class));
        });
    }

    private void setupBottomNavigation() {
        // Set the selected item to Create Game
        bottomNavigationView.setSelectedItemId(R.id.menuCreateGame);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menuCreateGame) {
                // Already on this screen
                return true;
            } else if (itemId == R.id.menuSharedGames) {
                // Navigate to Shared Games
                Intent intent = new Intent(CreateGame.this, SharedGames.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Smooth transition
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure the correct navigation item is selected
        bottomNavigationView.setSelectedItemId(R.id.menuCreateGame);
    }
}