package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.Toast;


public class ActivationActivity extends AppCompatActivity {

    private Button btnActivate;
    private DatabaseHelper dbHelper;
    private String email; // prosleđuješ email korisnika koji se aktivira

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        btnActivate = findViewById(R.id.btnActivate);
        dbHelper = new DatabaseHelper(this);

        // Preuzmi email iz intent-a
        email = getIntent().getStringExtra("email");

        btnActivate.setOnClickListener(v -> {
            dbHelper.activateUser(email);
            Toast.makeText(this, "Nalog uspešno aktiviran!", Toast.LENGTH_SHORT).show();
            // Vraćaš korisnika na login ili glavni ekran
            Intent intent = new Intent(ActivationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
