package com.example.cspapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenu extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnLogin, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        // Set up button listeners
        btnLogin.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, SignIn.class)));
        btnSignup.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, SignUp.class)));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and redirect to SharedGames if they are
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, go directly to SharedGames
            startActivity(new Intent(MainMenu.this, SharedGames.class));
            finish();
        }
    }
}