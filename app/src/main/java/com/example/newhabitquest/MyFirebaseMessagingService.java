package com.example.newhabitquest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "alliance_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody(),
                remoteMessage.getData()
            );
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");

            if (title != null && body != null) {
                showNotification(title, body, remoteMessage.getData());
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to server (Firestore)
        FCMTokenManager.updateUserToken(token);
    }

    private void showNotification(String title, String body, java.util.Map<String, String> data) {
        createNotificationChannel();

        Intent intent;
        String type = data.get("type");

        if ("ALLIANCE_INVITATION".equals(type)) {
            intent = new Intent(this, SocialActivity.class);
            intent.putExtra("showInvitations", true);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent);

        // Add action buttons for alliance invitations
        if ("ALLIANCE_INVITATION".equals(type)) {
            String invitationId = data.get("invitationId");
            if (invitationId != null) {
                // Accept action
                Intent acceptIntent = new Intent(this, AllianceInvitationReceiver.class);
                acceptIntent.setAction("ACCEPT_INVITATION");
                acceptIntent.putExtra("invitationId", invitationId);
                PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                    this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Reject action
                Intent rejectIntent = new Intent(this, AllianceInvitationReceiver.class);
                rejectIntent.setAction("REJECT_INVITATION");
                rejectIntent.putExtra("invitationId", invitationId);
                PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(
                    this, 2, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                notificationBuilder
                    .addAction(R.drawable.ic_check, "Prihvati", acceptPendingIntent)
                    .addAction(R.drawable.ic_close, "Odbij", rejectPendingIntent);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alliance Notifications",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for alliance invitations and updates");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
