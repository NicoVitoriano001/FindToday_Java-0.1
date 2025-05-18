package com.app.fintoday.ui;
//Criado em 16.05.25

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.app.fintoday.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            FirebaseApp.initializeApp(this);
            setContentView(R.layout.activity_login);
            // Resto do código...
        } catch (Exception e) {
         //  Log.e("INIT", "Firebase init failed", e);
            finish();
        }

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(v -> {
            // Implemente sua lógica de login aqui
            // Exemplo simplificado:
            mAuth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Falha no login", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }// FIM ON CREATE
}

