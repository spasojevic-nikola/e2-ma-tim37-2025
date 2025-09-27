package com.example.newhabitquest;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void initializeToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                Log.d(TAG, "FCM Registration Token: " + token);

                updateUserToken(token);
            });
    }

    public static void updateUserToken(String token) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("lastUpdated", System.currentTimeMillis());

            db.collection("users").document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "FCM token updated successfully for user: " + userId))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Error updating FCM token", e));
        }
    }

    public static void getUserFCMToken(String userId, FCMTokenCallback callback) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String token = document.getString("fcmToken");
                    callback.onTokenRetrieved(token);
                } else {
                    callback.onTokenRetrieved(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting FCM token", e);
                callback.onTokenRetrieved(null);
            });
    }

    public interface FCMTokenCallback {
        void onTokenRetrieved(String token);
    }
}
