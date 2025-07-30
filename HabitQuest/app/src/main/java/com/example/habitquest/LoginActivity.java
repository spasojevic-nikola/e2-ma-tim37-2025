package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        sessionManager = new SessionManager(getApplicationContext());

        // Ako je već ulogovan, idi na MainActivity
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));

            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Popuni sva polja", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loginUser(String email, String password) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Constants.BASE_URL + "/login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int code = conn.getResponseCode();

                InputStream is;
                if (code >= 200 && code < 400) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                int finalCode = code;
                runOnUiThread(() -> {
                    if (finalCode == 200) {
                        sessionManager.setLogin(true);
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Greška pri loginu: " + response, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }


}
