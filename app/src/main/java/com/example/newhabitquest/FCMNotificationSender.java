package com.example.newhabitquest;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FCMNotificationSender {
    private static final String TAG = "FCMNotificationSender";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void sendAllianceInvitation(String recipientToken, String senderUsername,
                                            String allianceName, String invitationId) {
        executor.execute(() -> {
            // Store notification request in Firestore for Firebase Function to process
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "ALLIANCE_INVITATION");
            notificationData.put("recipientToken", recipientToken);
            notificationData.put("title", "Poziv u savez");
            notificationData.put("body", senderUsername + " vas poziva u savez \"" + allianceName + "\"");
            notificationData.put("invitationId", invitationId);
            notificationData.put("senderUsername", senderUsername);
            notificationData.put("allianceName", allianceName);
            notificationData.put("timestamp", System.currentTimeMillis());
            notificationData.put("processed", false);

            db.collection("notification_queue").add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification queued for processing: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error queuing notification", e);
                });
        });
    }

    public static void sendAllianceAcceptance(String recipientToken, String accepterUsername, String allianceName) {
        executor.execute(() -> {
            // Store notification request in Firestore for Firebase Function to process
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "ALLIANCE_MEMBER_JOINED");
            notificationData.put("recipientToken", recipientToken);
            notificationData.put("title", "Novi član saveza");
            notificationData.put("body", accepterUsername + " je prihvatio poziv u savez \"" + allianceName + "\"");
            notificationData.put("accepterUsername", accepterUsername);
            notificationData.put("allianceName", allianceName);
            notificationData.put("timestamp", System.currentTimeMillis());
            notificationData.put("processed", false);

            db.collection("notification_queue").add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification queued for processing: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error queuing notification", e);
                });
        });
    }
}
