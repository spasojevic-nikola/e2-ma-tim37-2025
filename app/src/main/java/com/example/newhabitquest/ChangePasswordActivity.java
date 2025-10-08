package com.example.newhabitquest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        EditText oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        EditText newPasswordEditText = findViewById(R.id.newPasswordEditText);
        EditText confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        Button changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(v -> {
            String oldPassword = oldPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                showMessage("Popunite sva polja za promenu lozinke.");
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                showMessage("Nove lozinke se ne poklapaju.");
                return;
            }
            if (newPassword.length() < 6) {
                showMessage("Nova lozinka mora imati bar 6 karaktera.");
                return;
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null) {
                showMessage("Greška sa korisničkim nalogom.");
                return;
            }
            // Reautentifikacija
            com.google.firebase.auth.AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), oldPassword);
            auth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(task -> {
                    auth.getCurrentUser().updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> {
                            showMessage("Lozinka uspešno promenjena.");
                            finish();
                        })
                        .addOnFailureListener(e -> showMessage("Greška pri promeni lozinke: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showMessage("Stara lozinka nije ispravna."));
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

