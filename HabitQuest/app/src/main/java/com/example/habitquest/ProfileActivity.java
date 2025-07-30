package com.example.habitquest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(getApplicationContext());

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);

        // Pretpostavka: SessionManager čuva email i username
        String email = sessionManager.getUserEmail();
        String username = sessionManager.getUsername();

        tvEmail.setText("Email: " + (email != null ? email : "Nepoznato"));
        tvUsername.setText("Korisničko ime: " + (username != null ? username : "Nepoznato"));
    }
}
