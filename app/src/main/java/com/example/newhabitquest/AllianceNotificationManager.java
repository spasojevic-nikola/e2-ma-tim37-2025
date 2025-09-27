package com.example.newhabitquest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AllianceNotificationManager {
    private static final String CHANNEL_ID = "alliance_invitations";
    private static final String CHANNEL_NAME = "Alliance Invitations";
    private static final String CHANNEL_DESCRIPTION = "Notifications for alliance invitations";

    private final Context context;
    private final FirebaseFirestore db;

    public AllianceNotificationManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendInvitationNotification(String receiverUserId, String senderUsername,
                                          String allianceName, String invitationId) {
        // Create a live notification in Firestore that will trigger notification on receiver's device
        createLiveNotification(
            receiverUserId,
            "Poziv u savez",
            senderUsername + " vas poziva u savez \"" + allianceName + "\"",
            "ALLIANCE_INVITATION",
            invitationId
        );
    }

    public void sendAcceptanceNotification(String leaderUserId, String accepterUsername, String allianceName) {
        // Create a live notification in Firestore that will trigger notification on leader's device
        createLiveNotification(
            leaderUserId,
            "Novi član saveza",
            accepterUsername + " je prihvatio poziv u savez \"" + allianceName + "\"",
            "ALLIANCE_MEMBER_JOINED",
            null
        );
    }

    public void createLiveNotification(String userId, String title, String message, String type, String relatedId) {
        String notificationId = db.collection("live_notifications").document().getId();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("notificationId", notificationId);
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);

        // Use relatedId for both invitationId and other related IDs
        if (relatedId != null) {
            notificationData.put("invitationId", relatedId);
            notificationData.put("relatedId", relatedId);
        }

        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("showAsNotification", true);

        db.collection("live_notifications").document(notificationId)
            .set(notificationData)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("AllianceNotificationManager", "Live notification created for user: " + userId);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AllianceNotificationManager", "Error creating live notification", e);
            });
    }

    public void showLocalNotification(String title, String message, String type, String invitationId) {
        // Create intent for notification action
        Intent intent;
        if ("ALLIANCE_INVITATION".equals(type)) {
            intent = new Intent(context, SocialActivity.class);
            intent.putExtra("showInvitations", true);
        } else {
            intent = new Intent(context, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon as fallback
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Add action buttons for alliance invitations
        if ("ALLIANCE_INVITATION".equals(type) && invitationId != null) {
            // Accept action
            Intent acceptIntent = new Intent(context, AllianceInvitationReceiver.class);
            acceptIntent.setAction("ACCEPT_INVITATION");
            acceptIntent.putExtra("invitationId", invitationId);
            PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                context, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Reject action
            Intent rejectIntent = new Intent(context, AllianceInvitationReceiver.class);
            rejectIntent.setAction("REJECT_INVITATION");
            rejectIntent.putExtra("invitationId", invitationId);
            PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(
                context, 2, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            builder.addAction(android.R.drawable.ic_menu_send, "Prihvati", acceptPendingIntent)
                   .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Odbij", rejectPendingIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(invitationId != null ? invitationId.hashCode() : (int)System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            android.util.Log.e("AllianceNotificationManager", "Permission not granted for notifications", e);
        }
    }

    public void cancelInvitationNotification(String invitationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(invitationId.hashCode());
    }

    public void createSystemNotification(String userId, String title, String message, String type) {
        String notificationId = db.collection("notifications").document().getId();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("notificationId", notificationId);
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);

        db.collection("notifications").document(notificationId)
            .set(notificationData)
            .addOnFailureListener(e ->
                android.util.Log.e("NotificationManager", "Error creating notification", e)
            );
    }
}
