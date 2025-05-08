package com.example.cspapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import androidx.annotation.Nullable;
import android.media.MediaPlayer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

public class CreateGame extends AppCompatActivity implements CreatedGamesAdapter.OnGameClickListener {

    private BottomNavigationView bottomNavigationView;
    private Button btnChooseGame, btnName, btnGameSettings, btnCreateGame, btnImages, btnMusic;
    private RecyclerView rvCreatedGames;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedGame = "Snake Game";
    private int gameSpeed = 10; // Default speed
    private String gameName = "";
    private List<GameItem> userGames = new ArrayList<>();
    private CreatedGamesAdapter gamesAdapter;

    private Uri selectedMusicUri = null;
    private static final int PICK_MUSIC_REQUEST = 2;
    private ActivityResultLauncher<Intent> musicPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_game);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Game");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnChooseGame = findViewById(R.id.btnChooseGame);
        btnName = findViewById(R.id.btnName);
        btnImages = findViewById(R.id.btnImages);
        btnMusic = findViewById(R.id.btnMusic);
        btnGameSettings = findViewById(R.id.btnGameSettings);
        btnCreateGame = findViewById(R.id.btnCreateGame);
        rvCreatedGames = findViewById(R.id.rvCreatedGames);


        // Register activity result launcher
        registerActivityResultLaunchers();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up RecyclerView
        gamesAdapter = new CreatedGamesAdapter(userGames, this);
        rvCreatedGames.setLayoutManager(new LinearLayoutManager(this));
        rvCreatedGames.setAdapter(gamesAdapter);
        loadUserGames();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        btnImages.setOnClickListener(v -> openImagePicker());
        btnChooseGame.setOnClickListener(v -> showGameSelectionDialog());
        btnName.setOnClickListener(v -> showNameDialog());
        btnGameSettings.setOnClickListener(v -> showGameSettingsDialog());
        btnCreateGame.setOnClickListener(v -> showCreateGameDialog());
        btnMusic.setOnClickListener(v -> openMusicPicker());
    }


    private void registerActivityResultLaunchers() {
        musicPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getData() != null) {

                        selectedMusicUri = result.getData().getData();
                        btnMusic.setText("Music Selected");

                        // Preview the selected music
                        previewSelectedMusic(selectedMusicUri);
                    }
                }
        );
    }

    private void openMusicPicker() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        musicPickerLauncher.launch(Intent.createChooser(intent, "Select Music"));
    }

    private void showNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_game, null);

        EditText nameInput = view.findViewById(R.id.etGameName);

        // Pre-fill with existing name if available
        if (!gameName.isEmpty()) {
            nameInput.setText(gameName);
        }

        builder.setView(view)
                .setTitle("Game Name")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        gameName = name;
                        // Update button text to show selected name
                        btnName.setText("Name: " + gameName);
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showGameSelectionDialog() {
        final String[] gameOptions = {"Snake Game", "Space Invaders"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Game Type")
                .setItems(gameOptions, (dialog, which) -> {
                    selectedGame = gameOptions[which];
                    btnChooseGame.setText("Game: " + selectedGame);
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
                int actualSpeed = progress + 5; // Map 0-10 to 5-15
                speedValueText.setText(String.valueOf(actualSpeed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view)
                .setTitle("Game Settings")
                .setPositiveButton("Save", (dialog, which) -> {
                    gameSpeed = speedSeekBar.getProgress() + 5; // Map 0-10 to 5-15
                    btnGameSettings.setText("Speed: " + gameSpeed);
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showCreateGameDialog() {
        // Check if name has been set
        if (gameName.isEmpty()) {
            Toast.makeText(this, "Please set a game name first", Toast.LENGTH_SHORT).show();
            showNameDialog();
            return;
        }

        // Show confirmation dialog with game details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Game")
                .setMessage("Create game with the following settings?\n\n" +
                        "Name: " + gameName + "\n" +
                        "Type: " + selectedGame + "\n" +
                        "Speed: " + gameSpeed)
                .setPositiveButton("Create", (dialog, which) -> {
                    saveGameToFirestore();
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private Uri selectedImageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Method to open image picker when image button is clicked
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            btnImages.setText("Image Selected");
        } else if (requestCode == PICK_MUSIC_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedMusicUri = data.getData();
            btnMusic.setText("Music Selected");

            // Preview the selected music
            previewSelectedMusic(selectedMusicUri);
        }
    }

    private MediaPlayer mediaPlayer;

    private void previewSelectedMusic(Uri musicUri) {
        // Stop any currently playing music
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Create a new MediaPlayer instance
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, musicUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Stop preview after 5 seconds
            new Handler().postDelayed(() -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }, 5000);
        } catch (IOException e) {
            Toast.makeText(this, "Error playing music preview", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGameToFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to create games",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Game");
        progressDialog.setMessage("Please wait while we save your game...");
        progressDialog.show();

        final String gameId = UUID.randomUUID().toString();

        // First create game data without URLs
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("name", gameName);
        gameData.put("type", selectedGame);
        gameData.put("speed", gameSpeed);
        gameData.put("createdAt", System.currentTimeMillis());

        // Count for tracking multiple uploads
        final int[] uploadCount = {0};
        final int totalUploads = (selectedImageUri != null ? 1 : 0) + (selectedMusicUri != null ? 1 : 0);

        // If image was selected, upload it
        if (selectedImageUri != null) {
            uploadCount[0]++;
            uploadImageToStorage(userId, gameId, gameData, progressDialog, uploadCount, totalUploads);
        }

        // If music was selected, upload it
        if (selectedMusicUri != null) {
            uploadCount[0]++;
            uploadMusicToStorage(userId, gameId, gameData, progressDialog, uploadCount, totalUploads);
        }

        // If no files to upload, save game data directly
        if (totalUploads == 0) {
            saveGameDataToFirestore(userId, gameData, progressDialog);
        }
    }

    // Add method to upload image to storage
    private void uploadImageToStorage(String userId, String gameId, Map<String, Object> gameData,
                                      ProgressDialog progressDialog, int[] uploadCount, int totalUploads) {
        // Create a reference to the storage location
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("game_images")
                .child(userId)
                .child(gameId + ".jpg");

        // Upload the file
        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Add the image URL to the game data
                        gameData.put("imageUrl", uri.toString());

                        // Check if all uploads are complete
                        checkUploadsComplete(userId, gameData, progressDialog, uploadCount, totalUploads);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGame.this, "Error uploading image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadMusicToStorage(String userId, String gameId, Map<String, Object> gameData,
                                      ProgressDialog progressDialog, int[] uploadCount, int totalUploads) {
        // Create a reference to the storage location
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("game_music")
                .child(userId)
                .child(gameId + ".mp3");

        // Upload the file
        storageRef.putFile(selectedMusicUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Add the music URL to the game data
                        gameData.put("musicUrl", uri.toString());

                        // Check if all uploads are complete
                        checkUploadsComplete(userId, gameData, progressDialog, uploadCount, totalUploads);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGame.this, "Error uploading music: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Add method to check if all uploads are complete
    private void checkUploadsComplete(String userId, Map<String, Object> gameData,
                                      ProgressDialog progressDialog, int[] uploadCount, int totalUploads) {
        uploadCount[0]--;
        if (uploadCount[0] <= 0) {
            // All uploads complete, save game data
            saveGameDataToFirestore(userId, gameData, progressDialog);
        }
    }


    private void saveGameDataToFirestore(String userId, Map<String, Object> gameData,
                                         ProgressDialog progressDialog) {
        db.collection("users").document(userId)
                .collection("games")
                .add(gameData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGame.this, "Game created successfully!",
                            Toast.LENGTH_SHORT).show();
                    // Reset game name, image and music
                    gameName = "";
                    selectedImageUri = null;
                    selectedMusicUri = null;
                    btnName.setText("Name");
                    btnImages.setText("Images");
                    btnMusic.setText("Music");
                    // Refresh the games list
                    loadUserGames();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateGame.this, "Error creating game: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserGames() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("games")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userGames.clear();
                    for (DocumentSnapshot docRef : queryDocumentSnapshots.getDocuments()) {
                        String id = docRef.getId();
                        String name = docRef.getString("name");
                        String type = docRef.getString("type");
                        int speed = docRef.getLong("speed").intValue();
                        String imageUrl = docRef.getString("imageUrl");
                        String musicUrl = docRef.getString("musicUrl");

                        GameItem game = new GameItem(id, name, type, speed, imageUrl, musicUrl);
                        userGames.add(game);
                    }
                    gamesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateGame.this, "Error loading games: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onGameClick(GameItem game) {
        launchSelectedGame(game);
    }

    @Override
    public void onShareClick(GameItem game) {
        shareGame(game);
    }

    private void shareGame(GameItem game) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to share games",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Get the user's username
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    if (username == null || username.isEmpty()) {
                        username = "Anonymous";
                    }

                    // Create shared game data
                    Map<String, Object> sharedGameData = new HashMap<>();
                    sharedGameData.put("name", game.getName());
                    sharedGameData.put("type", game.getType());
                    sharedGameData.put("speed", game.getSpeed());
                    sharedGameData.put("creatorId", userId);
                    sharedGameData.put("creatorName", username);
                    sharedGameData.put("sharedAt", System.currentTimeMillis());
                    sharedGameData.put("originalGameId", game.getId());

                    if (game.hasImage()) {
                        sharedGameData.put("imageUrl", game.getImageUrl());
                    }

                    if (game.hasMusic()) {
                        sharedGameData.put("musicUrl", game.getMusicUrl());
                    }

                    // Add to shared games collection
                    db.collection("sharedGames")
                            .add(sharedGameData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(CreateGame.this, "Game shared successfully!",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CreateGame.this, "Error sharing game: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateGame.this, "Error getting user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void launchSelectedGame(GameItem game) {
        Intent intent;

        if (game.getType().equals("Snake Game")) {
            intent = new Intent(CreateGame.this, SnakeGameActivity.class);
        } else if (game.getType().equals("Space Invaders")) {
            intent = new Intent(CreateGame.this, SpaceInvadersGameActivity.class);
        } else {
            // Default to Snake Game
            intent = new Intent(CreateGame.this, SnakeGameActivity.class);
        }

        intent.putExtra("GAME_SPEED", game.getSpeed());

        // Pass the image URL to the game activity
        if (game.hasImage()) {
            intent.putExtra("IMAGE_URL", game.getImageUrl());
        }
        if (game.hasMusic()) {
            intent.putExtra("MUSIC_URL", game.getMusicUrl());
        }

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
        bottomNavigationView.setSelectedItemId(R.id.menuCreateGame);
        // Refresh games list
        loadUserGames();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}