package com.example.cspapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SharedGames extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddGame;
    private RecyclerView rvSharedGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_games);

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabAddGame = findViewById(R.id.fabAddGame);
        rvSharedGames = findViewById(R.id.rvSharedGames);

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up FAB click listener
        fabAddGame.setOnClickListener(v -> {
            Intent intent = new Intent(SharedGames.this, CreateGame.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        // Set the selected item to Shared Games
        bottomNavigationView.setSelectedItemId(R.id.menuSharedGames);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menuSharedGames) {
                // Already on this screen
                return true;
            } else if (itemId == R.id.menuCreateGame) {
                // Navigate to Create Game
                Intent intent = new Intent(SharedGames.this, CreateGame.class);
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
        bottomNavigationView.setSelectedItemId(R.id.menuSharedGames);
    }
}