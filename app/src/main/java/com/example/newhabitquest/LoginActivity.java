package com.example.newhabitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLoginBtn;

    TextView mCreateBtn;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createText);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if(email.isEmpty()){
                    mEmail.setError("Email is required.");
                    return;
                }

                if(password.isEmpty()){
                    mPassword.setError("Password is required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password must be >= 6 characters.");
                    return;
                }

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            if(fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                String userId = fAuth.getCurrentUser().getUid();
                                FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                                    if(documentSnapshot.exists()) {
                                        Long registrationTime = documentSnapshot.getLong("registrationTime");
                                        long currentTime = System.currentTimeMillis();
                                        if(registrationTime != null && currentTime - registrationTime > 24 * 60 * 60 * 1000) {
                                            // Brisanje korisnika iz Auth i Firestore
                                            fAuth.getCurrentUser().delete();
                                            FirebaseFirestore.getInstance().collection("users").document(userId).delete();
                                            Toast.makeText(LoginActivity.this, "Link za verifikaciju je istekao. Registrujte se ponovo.", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
                                            finish();
                                        } else {
                                            fAuth.signOut();
                                            Toast.makeText(LoginActivity.this, "Verifikujte email pre prijave!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });


    }
}