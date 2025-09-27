package com.example.newhabitquest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationListenerService extends Service {
    private static final String TAG = "NotificationListener";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration listenerRegistration;
    private AllianceNotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationManager = new AllianceNotificationManager(this);

        Log.d(TAG, "NotificationListenerService created");
        startListeningForNotifications();
    }

    private void startListeningForNotifications() {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated, cannot listen for notifications");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Starting to listen for notifications for user: " + currentUserId);

        // Also listen for regular alliance invitations as backup
        db.collection("alliance_invitations")
            .whereEqualTo("receiverUserId", currentUserId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to alliance invitations", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
                    Log.d(TAG, "Found " + value.size() + " pending alliance invitations");
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            handleAllianceInvitation(document);
                        }
                    }
                }
            });

        listenerRegistration = db.collection("live_notifications")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isRead", false)
            .whereEqualTo("showAsNotification", true)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
                    Log.d(TAG, "Found " + value.size() + " new live notifications");
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            handleNewNotification(document);
                        }
                    }
                } else {
                    Log.d(TAG, "No new notifications found");
                }
            });
    }

    private void handleNewNotification(QueryDocumentSnapshot document) {
        try {
            String title = document.getString("title");
            String message = document.getString("message");
            String type = document.getString("type");
            String invitationId = document.getString("invitationId");
            String notificationId = document.getId();

            Log.d(TAG, "Received new notification: " + title);

            // Show local notification
            notificationManager.showLocalNotification(title, message, type, invitationId);

            // Mark notification as read and hide it
            db.collection("live_notifications").document(notificationId)
                .update(
                    "isRead", true,
                    "showAsNotification", false
                )
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "Notification marked as read: " + notificationId))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Error marking notification as read", e));

        } catch (Exception e) {
            Log.e(TAG, "Error processing notification", e);
        }
    }

    private void handleAllianceInvitation(QueryDocumentSnapshot document) {
        try {
            String senderUsername = document.getString("senderUsername");
            String allianceName = document.getString("allianceName");
            String invitationId = document.getId();

            Log.d(TAG, "Received alliance invitation from: " + senderUsername);

            String title = "Poziv u savez";
            String message = senderUsername + " vas poziva u savez \"" + allianceName + "\"";

            // Show local notification
            notificationManager.showLocalNotification(title, message, "ALLIANCE_INVITATION", invitationId);

        } catch (Exception e) {
            Log.e(TAG, "Error processing alliance invitation", e);
        }
    }

    private void handleChatMessage(QueryDocumentSnapshot document) {
        try {
            String title = document.getString("title");
            String message = document.getString("message");
            String relatedId = document.getString("relatedId");

            Log.d(TAG, "Received chat message notification: " + title);

            // Create intent to open chat when notification is clicked
            Intent chatIntent = new Intent(this, AllianceChatActivity.class);
            chatIntent.putExtra("allianceId", relatedId);
            chatIntent.putExtra("allianceName", "Chat Saveza");
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            notificationManager.showLocalNotification(title, message, "ALLIANCE_MESSAGE", relatedId);

        } catch (Exception e) {
            Log.e(TAG, "Error processing chat message notification", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "NotificationListenerService started");
        return START_STICKY; // Restart service if it gets killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        Log.d(TAG, "NotificationListenerService destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
