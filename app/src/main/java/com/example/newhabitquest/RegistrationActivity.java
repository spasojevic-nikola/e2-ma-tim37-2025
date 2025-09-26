package com.example.newhabitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    public static final String TAG = "RegistrationActivity";
    EditText mUsername, mEmail, mPassword, mConfirmPassword;
    Button mRegisterBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ImageView[] avatars = new ImageView[5];
    String selectedAvatar = "avatar1"; // default avatar
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirmPassword);
        mRegisterBtn = findViewById(R.id.registerBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        avatars[0] = findViewById(R.id.avatar1);
        avatars[1] = findViewById(R.id.avatar2);
        avatars[2] = findViewById(R.id.avatar3);
        avatars[3] = findViewById(R.id.avatar4);
        avatars[4] = findViewById(R.id.avatar5);
        for (int i = 0; i < avatars.length; i++) {
            final int index = i;
            avatars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ImageView avatar : avatars) {
                        avatar.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    }
                    avatars[index].setBackgroundColor(android.graphics.Color.BLUE); // highlight
                    selectedAvatar = "avatar" + (index + 1);
                }
            });
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirmPassword = mConfirmPassword.getText().toString().trim();
                String username = mUsername.getText().toString().trim();
                String avatar = selectedAvatar;

                if(username.isEmpty()){
                    mUsername.setError("Korisničko ime je obavezno.");
                    return;
                }
                if(email.isEmpty()){
                    mEmail.setError("Email je obavezan.");
                    return;
                }
                if(password.isEmpty()){
                    mPassword.setError("Lozinka je obavezna.");
                    return;
                }
                if(password.length() < 6){
                    mPassword.setError("Lozinka mora imati najmanje 6 karaktera.");
                    return;
                }
                if(!password.equals(confirmPassword)){
                    mConfirmPassword.setError("Lozinke se ne poklapaju.");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String userId = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userId);
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            user.put("avatar", avatar);
                            user.put("level", 1);
                            user.put("titula", "Početnik");
                            user.put("pp", 0);
                            user.put("xp", 0);
                            user.put("coins", 0);
                            user.put("badges", new ArrayList<String>());
                            user.put("equipment", new ArrayList<String>());
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: user Profile is created for " + userId);
                                }
                            });
                            Toast.makeText(RegistrationActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}