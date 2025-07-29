package com.example.habitquest;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private int selectedAvatar = 0;

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

        avatar1.setOnClickListener(v -> selectedAvatar = 0);
        avatar2.setOnClickListener(v -> selectedAvatar = 1);
        avatar3.setOnClickListener(v -> selectedAvatar = 2);
        avatar4.setOnClickListener(v -> selectedAvatar = 3);
        avatar5.setOnClickListener(v -> selectedAvatar = 4);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            Log.d("Register", "Klik na dugme Registruj se");

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Log.w("Register", "Neka polja su prazna");
                Toast.makeText(this, "Sva polja su obavezna!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.w("Register", "Neispravan email: " + email);
                Toast.makeText(this, "Neispravan email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Log.w("Register", "Lozinke se ne poklapaju");
                Toast.makeText(this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Log.w("Register", "Lozinka je prekratka: " + password.length());
                Toast.makeText(this, "Lozinka mora imati barem 6 karaktera!", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = UUID.randomUUID().toString();

            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("username", username);
                jsonInput.put("email", email);
                jsonInput.put("password", password);
                jsonInput.put("avatar", selectedAvatar);
                jsonInput.put("token", token);
            } catch (Exception e) {
                Log.e("Register", "Greška kod kreiranja JSON-a", e);
                Toast.makeText(this, "Greška prilikom pripreme podataka", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Register", "Saljem podatke: " + jsonInput.toString());

            new Thread(() -> {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://13.13.13.71:3000/register");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json"); // **OVDE DODAJ**
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }

                    int code = conn.getResponseCode();
                    Log.d("Register", "HTTP response code: " + code);

                    // Čitanje odgovora sa servera (uspeh ili greška)
                    InputStream is;
                    if (code >= 200 && code < 400) {
                        is = conn.getInputStream();
                    } else {
                        is = conn.getErrorStream();
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String responseBody = response.toString();
                    Log.d("Register", "Response body: " + responseBody);

                    runOnUiThread(() -> {
                        if (code == 200) {
                            Toast.makeText(this, "Registracija uspešna!", Toast.LENGTH_LONG).show();

                            etUsername.setText("");
                            etEmail.setText("");
                            etPassword.setText("");
                            etConfirmPassword.setText("");
                        } else {
                            Log.w("Register", "Registracija nije uspela, kod: " + code);
                            Toast.makeText(this, "Greška pri registraciji! Kod: " + code + "\n" + responseBody, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e("Register", "Greška prilikom slanja zahteva", e);
                    runOnUiThread(() -> Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }).start();
        });

    }
}
