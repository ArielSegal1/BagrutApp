package com.example.cspapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGame extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Button btnChooseGame, btnGameSettings, btnCreateGame;
    private RecyclerView rvCreatedGames;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedGame = "Snake Game";
    private int gameSpeed = 10; // Default speed
    private String gameName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_game);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnChooseGame = findViewById(R.id.btnChooseGame);
        btnGameSettings = findViewById(R.id.btnGameSettings);
        rvCreatedGames = findViewById(R.id.rvCreatedGames);

        // Add Create Game button
        btnCreateGame = findViewById(R.id.btnCreateGame);
        if (btnCreateGame == null) {
            // If button doesn't exist yet, add it programmatically
            btnCreateGame = new Button(this);
            btnCreateGame.setId(View.generateViewId());
            btnCreateGame.setText("Create Game");
            btnCreateGame.setBackgroundTintList(btnChooseGame.getBackgroundTintList());
            btnCreateGame.setTextSize(18);
            btnCreateGame.setAllCaps(false);
            btnCreateGame.setPadding(12, 12, 12, 12);

            // Add it to the layout
            ((android.view.ViewGroup) btnGameSettings.getParent()).addView(btnCreateGame,
                    new android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up RecyclerView
        rvCreatedGames.setLayoutManager(new LinearLayoutManager(this));
        loadUserGames();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        btnChooseGame.setOnClickListener(v -> showGameSelectionDialog());
        btnGameSettings.setOnClickListener(v -> showGameSettingsDialog());
        btnCreateGame.setOnClickListener(v -> showCreateGameDialog());
    }

    private void showGameSelectionDialog() {
        final String[] gameOptions = {"Snake Game"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Game Type")
                .setItems(gameOptions, (dialog, which) -> {
                    selectedGame = gameOptions[which];
                    btnChooseGame.setText(selectedGame);
                });
        builder.create().show();
    }

    private void showGameSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_game_settings, null);

        SeekBar speedSeekBar = view.findViewById(R.id.seekBarSpeed);
        TextView speedValueText = view.findViewById(R.id.tvSpeedValue);

        // Set initial values
        speedSeekBar.setProgress(gameSpeed - 5); // Map 5-15 to 0-10 on seekbar
        speedValueText.setText(String.valueOf(gameSpeed));

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Map 0-10 to 5-15 for actual game speed
                gameSpeed = progress + 5;
                speedValueText.setText(String.valueOf(gameSpeed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view)
                .setTitle("Game Settings")
                .setPositiveButton("Save", (dialog, which) -> {
                    Toast.makeText(CreateGame.this,
                            "Speed set to: " + gameSpeed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showCreateGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_game, null);

        EditText nameInput = view.findViewById(R.id.etGameName);

        builder.setView(view)
                .setTitle("Create Game")
                .setPositiveButton("Create", (dialog, which) -> {
                    gameName = nameInput.getText().toString().trim();
                    if (gameName.isEmpty()) {
                        Toast.makeText(CreateGame.this,
                                "Please enter a game name", Toast.LENGTH_SHORT).show();
                    } else {
                        saveGameToFirestore();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void saveGameToFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to create games",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Create game data
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("name", gameName);
        gameData.put("type", selectedGame);
        gameData.put("speed", gameSpeed);
        gameData.put("createdAt", System.currentTimeMillis());

        // Add to user's games collection
        db.collection("users").document(userId)
                .collection("games")
                .add(gameData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateGame.this,
                            "Game created successfully!", Toast.LENGTH_SHORT).show();
                    loadUserGames(); // Refresh the games list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateGame.this,
                            "Error creating game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserGames() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("games")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameItem> gameList = new ArrayList<>();

                    queryDocumentSnapshots.forEach(document -> {
                        String id = document.getId();
                        String name = document.getString("name");
                        String type = document.getString("type");
                        int speed = document.getLong("speed").intValue();

                        gameList.add(new GameItem(id, name, type, speed));
                    });

                    // Set up adapter
                    CreatedGamesAdapter adapter = new CreatedGamesAdapter(gameList,
                            gameId -> launchSelectedGame(gameId));
                    rvCreatedGames.setAdapter(adapter);
                });
    }

    private void launchSelectedGame(GameItem game) {
        // For now, we only have Snake Game
        Intent intent = new Intent(CreateGame.this, SnakeGameActivity.class);
        intent.putExtra("GAME_SPEED", game.getSpeed());
        startActivity(intent);
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
        // Refresh games list
        loadUserGames();
    }
}