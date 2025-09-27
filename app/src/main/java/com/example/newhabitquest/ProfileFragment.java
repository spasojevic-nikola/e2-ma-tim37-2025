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
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {
    private ListenerRegistration userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Add null checks and validation
        if (!isAdded() || getContext() == null) {
            return view;
        }

        ImageView avatarView = view.findViewById(R.id.profileAvatar);
        TextView usernameView = view.findViewById(R.id.profileUsername);
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
             // Load user profile data with real-time listener
            DocumentReference docRef = fStore.collection("users").document(userId);
            userListener = docRef.addSnapshotListener((documentSnapshot, e) -> {
                // Add fragment validation and auth check
                if (!isAdded() || getContext() == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
                    return;
                }

                if (e != null) {
                    android.util.Log.e("ProfileFragment", "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        String username = documentSnapshot.getString("username");
                        String avatarName = documentSnapshot.getString("avatar");
                        Long level = documentSnapshot.getLong("level");
                        String titula = documentSnapshot.getString("titula");
                        Long pp = documentSnapshot.getLong("pp");
                        Long xp = documentSnapshot.getLong("xp");
                        Long coins = documentSnapshot.getLong("coins");
                        java.util.List<String> badges = (java.util.List<String>) documentSnapshot.get("badges");
                        java.util.List<String> equipment = (java.util.List<String>) documentSnapshot.get("equipment");

                        // Set profile data with null checks
                        if (usernameView != null) usernameView.setText(username != null ? username : "");

                        if (avatarView != null && avatarName != null && getContext() != null) {
                            int resId = getContext().getResources().getIdentifier(avatarName, "drawable", getContext().getPackageName());
                            if (resId != 0) {
                                avatarView.setImageResource(resId);
                                avatarView.setVisibility(View.VISIBLE);
                            }
                        }

                        if (levelView != null) levelView.setText("Level: " + (level != null ? level : 1));
                        if (titulaView != null) titulaView.setText("Titula: " + (titula != null ? titula : "Početnik"));
                        if (ppView != null) ppView.setText("PP: " + (pp != null ? pp : 0));
                        if (xpView != null) xpView.setText("XP: " + (xp != null ? xp : 0));
                        if (coinsView != null) coinsView.setText("Coins: " + (coins != null ? coins : 0));
                        if (badgesCountView != null) badgesCountView.setText("Broj bedževa: " + (badges != null ? badges.size() : 0));
                        if (badgesView != null) badgesView.setText("Bedževi: " + (badges != null && !badges.isEmpty() ? android.text.TextUtils.join(", ", badges) : "-"));

                        // Display equipment
                        if (equipmentView != null) {
                            displayEquipmentInfo(fStore, userId, equipment, equipmentView);
                        }
                    } catch (Exception ex) {
                        android.util.Log.e("ProfileFragment", "Error updating profile UI", ex);
                    }
                }
            });
        }
        return view;
    }

    private void displayEquipmentInfo(FirebaseFirestore fStore, String userId, java.util.List<String> ownedEquipment, TextView equipmentView) {
        // Add fragment validation
        if (!isAdded() || getContext() == null || equipmentView == null) {
            return;
        }

        if (ownedEquipment == null || ownedEquipment.isEmpty()) {
            equipmentView.setText("Equipment: -");
            return;
        }

        // Simply display owned equipment without active equipment lookup
        try {
            equipmentView.setText("Equipment: " + android.text.TextUtils.join(", ", ownedEquipment));
        } catch (Exception e) {
            android.util.Log.e("ProfileFragment", "Error displaying equipment", e);
            equipmentView.setText("Equipment: -");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up listener when fragment is destroyed
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clean up listener when fragment is detached
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }
}
