package com.example.newhabitquest;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.LinearLayout;
import android.view.View;

public class FriendProfileActivity extends AppCompatActivity {
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        String friendId = getIntent().getStringExtra("friendId");
        if (friendId == null) {
            Toast.makeText(this, "Greška: Nepoznat korisnik.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView avatarView = findViewById(R.id.profileAvatar);
        TextView usernameView = findViewById(R.id.profileUsername);
        TextView levelView = findViewById(R.id.profileLevel);
        TextView titulaView = findViewById(R.id.profileTitula);
        TextView xpView = findViewById(R.id.profileXP);
        TextView badgesView = findViewById(R.id.profileBadges);
        TextView equipmentView = findViewById(R.id.profileEquipment);
        ImageView qrCodeView = findViewById(R.id.profileQRCode);
        // Ostali podaci nisu prikazani

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        DocumentReference docRef = fStore.collection("users").document(friendId);
        userListener = docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null || documentSnapshot == null || !documentSnapshot.exists()) {
                Toast.makeText(this, "Greška pri učitavanju profila.", Toast.LENGTH_SHORT).show();
                return;
            }
            String username = documentSnapshot.getString("username");
            String avatarName = documentSnapshot.getString("avatar");
            Long level = documentSnapshot.getLong("level");
            String titula = documentSnapshot.getString("titula");
            Long xp = documentSnapshot.getLong("xp");
            java.util.List<String> badges = (java.util.List<String>) documentSnapshot.get("badges");
            java.util.List<String> equipment = (java.util.List<String>) documentSnapshot.get("equipment");
            String qrCode = documentSnapshot.getString("qrCode");

            if (usernameView != null) usernameView.setText(username != null ? username : "");
            if (avatarView != null && avatarName != null) {
                int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                if (resId != 0) {
                    avatarView.setImageResource(resId);
                    avatarView.setVisibility(View.VISIBLE);
                }
            }
            if (levelView != null) levelView.setText("Level: " + (level != null ? level : 1));
            if (titulaView != null) titulaView.setText("Titula: " + (titula != null ? titula : "Početnik"));
            if (xpView != null) xpView.setText("XP: " + (xp != null ? xp : 0));
            if (badgesView != null) badgesView.setText("Bedževi: " + (badges != null && !badges.isEmpty() ? android.text.TextUtils.join(", ", badges) : "-"));
            if (equipmentView != null) equipmentView.setText("Oprema: " + (equipment != null && !equipment.isEmpty() ? android.text.TextUtils.join(", ", equipment) : "-"));
            // QR kod prikaz (može biti string ili bitmapa, ovde samo kao tekst)
            if (qrCodeView != null && qrCode != null) {
                // Prikaz QR koda kao tekst, za pravu implementaciju koristi se Bitmap
                qrCodeView.setVisibility(View.VISIBLE);
                // Ostaviti prazno ili koristiti biblioteku za prikaz QR koda
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
    }
}
