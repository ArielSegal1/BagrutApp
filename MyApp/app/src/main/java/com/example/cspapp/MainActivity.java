package com.example.cspapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUp.class)));
        findViewById(R.id.btnSignIn).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignIn.class)));
        findViewById(R.id.btnCreateGame).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateGame.class)));
        findViewById(R.id.btnSharedGames).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SharedGames.class)));
    }
}