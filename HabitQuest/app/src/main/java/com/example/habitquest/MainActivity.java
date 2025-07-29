package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    private int selectedAvatar = -1;

    String token = UUID.randomUUID().toString();

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

        // Povezivanje UI elemenata
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.button);

        ImageView avatar1 = findViewById(R.id.avatar1);
        ImageView avatar2 = findViewById(R.id.avatar2);
        ImageView avatar3 = findViewById(R.id.avatar3);
        ImageView avatar4 = findViewById(R.id.avatar4);
        ImageView avatar5 = findViewById(R.id.avatar5);

        dbHelper = new DatabaseHelper(this);

        // Listener za registraciju
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Sva polja su obavezna!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(MainActivity.this, "Neispravan email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(MainActivity.this, "Lozinka mora imati barem 6 karaktera!", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.insertUser(username, email, password, selectedAvatar, token);
                if (success) {
                    // GENERIŠI TOKEN
                    String token = UUID.randomUUID().toString();

                    // POŠALJI EMAIL, USERNAME I TOKEN SERVERU
                    new Thread(() -> {
                        try {
                            URL url = new URL("http://10.0.2.2:3000/sendActivation"); // emulator: 10.0.2.2, telefon: tvoja lokalna IP adresa
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json; utf-8");
                            conn.setDoOutput(true);

                            String jsonInputString = String.format("{\"email\":\"%s\",\"username\":\"%s\",\"token\":\"%s\"}",
                                    email, username, token);

                            try (OutputStream os = conn.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }

                            int code = conn.getResponseCode();
                            runOnUiThread(() -> {
                                if (code == 200) {
                                    Toast.makeText(MainActivity.this, "Aktivacioni email je poslat!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Greška pri slanju emaila!", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            e.printStackTrace();
                        }
                    }).start();

                    // OVDE upiši korisnika u SQLite uz token (dodaj kolonu token i avatar u tvoju DB ako nisi)
                    // (Ne zaboravi da pozoveš i activity za aktivaciju ako želiš simulaciju)

                    etUsername.setText("");
                    etEmail.setText("");
                    etPassword.setText("");
                    etConfirmPassword.setText("");
                }
                else {
                    Toast.makeText(MainActivity.this, "Korisnik sa tim emailom ili korisničkim imenom već postoji!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
