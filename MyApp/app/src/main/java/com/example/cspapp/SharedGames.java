package com.example.cspapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.List;

public class SharedGames extends AppCompatActivity implements SharedGamesAdapter.OnSharedGameClickListener {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddGame;
    private RecyclerView rvSharedGames;
    private FirebaseFirestore db;
    private List<SharedGameItem> sharedGames = new ArrayList<>();
    private SharedGamesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_games);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shared Games");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabAddGame = findViewById(R.id.fabAddGame);
        rvSharedGames = findViewById(R.id.rvSharedGames);

        // Set up RecyclerView
        adapter = new SharedGamesAdapter(sharedGames, this);
        rvSharedGames.setLayoutManager(new LinearLayoutManager(this));
        rvSharedGames.setAdapter(adapter);

        // Load shared games
        loadSharedGames();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up FAB click listener
        fabAddGame.setOnClickListener(v -> {
            Intent intent = new Intent(SharedGames.this, CreateGame.class);
            startActivity(intent);
        });
    }

    private void loadSharedGames() {
        db.collection("sharedGames")
                .orderBy("sharedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sharedGames.clear();
                    for (DocumentSnapshot docRef : queryDocumentSnapshots.getDocuments()) {
                        String id = docRef.getId();
                        String name = docRef.getString("name");
                        String type = docRef.getString("type");
                        int speed = docRef.getLong("speed").intValue();
                        String creatorId = docRef.getString("creatorId");
                        String creatorName = docRef.getString("creatorName");
                        long sharedAt = docRef.getLong("sharedAt");
                        String imageUrl = docRef.getString("imageUrl");
                        String musicUrl = docRef.getString("musicUrl"); // Add this line

                        SharedGameItem game = new SharedGameItem(
                                id, name, type, speed, creatorId, creatorName,
                                sharedAt, imageUrl, musicUrl); // Pass musicUrl
                        sharedGames.add(game);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SharedGames.this, "Error loading shared games: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
    public void onPlayClick(SharedGameItem game) {
        Intent intent;

        if (game.getType().equals("Snake Game")) {
            intent = new Intent(SharedGames.this, SnakeGameActivity.class);
        } else if (game.getType().equals("Space Invaders")) {
            intent = new Intent(SharedGames.this, SpaceInvadersGameActivity.class);
        } else {
            // Default to Snake Game
            intent = new Intent(SharedGames.this, SnakeGameActivity.class);
        }

        // Pass the image URL to the game activity
        if (game.hasImage()) {
            intent.putExtra("IMAGE_URL", game.getImageUrl());
        }

        if (game.hasMusic()) {
            intent.putExtra("MUSIC_URL", game.getMusicUrl());
        }

        intent.putExtra("GAME_SPEED", game.getSpeed());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            // Show logout confirmation dialog
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Options")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Perform logout
                    FirebaseAuth.getInstance().signOut();

                    // Navigate back to MainMenu
                    Intent intent = new Intent(this, MainMenu.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Make sure the correct navigation item is selected
        bottomNavigationView.setSelectedItemId(R.id.menuSharedGames);
        // Refresh games list
        loadSharedGames();
    }
}