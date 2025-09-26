package com.example.newhabitquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView avatarView = view.findViewById(R.id.profileAvatar);
        TextView usernameView = view.findViewById(R.id.profileUsername);
        //TextView emailView = view.findViewById(R.id.profileEmail);
        TextView levelView = view.findViewById(R.id.profileLevel);
        TextView titulaView = view.findViewById(R.id.profileTitula);
        TextView ppView = view.findViewById(R.id.profilePP);
        TextView xpView = view.findViewById(R.id.profileXP);
        TextView coinsView = view.findViewById(R.id.profileCoins);
        TextView badgesCountView = view.findViewById(R.id.profileBadgesCount);
        TextView badgesView = view.findViewById(R.id.profileBadges);
        TextView equipmentView = view.findViewById(R.id.profileEquipment);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        String userId = fAuth.getCurrentUser() != null ? fAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            DocumentReference docRef = fStore.collection("users").document(userId);
            docRef.get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String avatarName = documentSnapshot.getString("avatar");
                        Long level = documentSnapshot.getLong("level");
                        String titula = documentSnapshot.getString("titula");
                        Long pp = documentSnapshot.getLong("pp");
                        Long xp = documentSnapshot.getLong("xp");
                        Long coins = documentSnapshot.getLong("coins");
                        java.util.List<String> badges = (java.util.List<String>) documentSnapshot.get("badges");
                        java.util.List<String> equipment = (java.util.List<String>) documentSnapshot.get("equipment");
                        usernameView.setText(username != null ? username : "");
                        //emailView.setText("Email: " + (email != null ? email : ""));
                        if (avatarName != null) {
                            int resId = requireContext().getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
                            if (resId != 0) {
                                avatarView.setImageResource(resId);
                                avatarView.setVisibility(View.VISIBLE);
                            }
                        }
                        levelView.setText("Level: " + (level != null ? level : 1));
                        titulaView.setText("Titula: " + (titula != null ? titula : "Početnik"));
                        ppView.setText("PP: " + (pp != null ? pp : 0));
                        xpView.setText("XP: " + (xp != null ? xp : 0));
                        coinsView.setText("Coins: " + (coins != null ? coins : 0));
                        badgesCountView.setText("Broj bedževa: " + (badges != null ? badges.size() : 0));
                        badgesView.setText("Bedževi: " + (badges != null && !badges.isEmpty() ? android.text.TextUtils.join(", ", badges) : "-") );
                        equipmentView.setText("Oprema: " + (equipment != null && !equipment.isEmpty() ? android.text.TextUtils.join(", ", equipment) : "-") );
                    }
                }
            });
        }
        return view;
    }
}
