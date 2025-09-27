package com.example.newhabitquest;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceInvitationManager {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;
    private AllianceNotificationManager notificationManager;

    public AllianceInvitationManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.notificationManager = new AllianceNotificationManager(context);
    }

    public void sendInvitation(String allianceId, String allianceName, String friendUserId,
                              String senderUsername, String message) {
        // Check if user is already in this specific alliance or has pending invitation for it
        checkUserEligibilityForAlliance(allianceId, friendUserId, new UserEligibilityCallback() {
            @Override
            public void onResult(boolean canInvite, String reason) {
                if (canInvite) {
                    createInvitation(allianceId, allianceName, friendUserId, senderUsername, message);
                } else {
                    Toast.makeText(context, reason, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createInvitation(String allianceId, String allianceName, String friendUserId,
                                 String senderUsername, String message) {
        String invitationId = db.collection("alliance_invitations").document().getId();
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) return;

        AllianceInvitation invitation = new AllianceInvitation(
            invitationId, allianceId, allianceName,
            currentUserId, senderUsername, friendUserId, message
        );

        Map<String, Object> invitationData = new HashMap<>();
        invitationData.put("invitationId", invitation.getInvitationId());
        invitationData.put("allianceId", invitation.getAllianceId());
        invitationData.put("allianceName", invitation.getAllianceName());
        invitationData.put("senderUserId", invitation.getSenderUserId());
        invitationData.put("senderUsername", invitation.getSenderUsername());
        invitationData.put("receiverUserId", invitation.getReceiverUserId());
        invitationData.put("timestamp", invitation.getTimestamp());
        invitationData.put("status", invitation.getStatus().toString());
        invitationData.put("message", invitation.getMessage());

        db.collection("alliance_invitations").document(invitationId)
            .set(invitationData)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("InvitationManager", "Alliance invitation saved successfully for user: " + friendUserId);

                // Send notification to the invited user
                notificationManager.sendInvitationNotification(
                    friendUserId, senderUsername, allianceName, invitationId
                );

                // Create system notification
                notificationManager.createSystemNotification(
                    friendUserId,
                    "Poziv u savez",
                    senderUsername + " vas poziva u savez \"" + allianceName + "\"",
                    "ALLIANCE_INVITATION"
                );

                Toast.makeText(context, "Poziv poslat uspešno!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("InvitationManager", "Error sending invitation", e);
                Toast.makeText(context, "Greška pri slanju poziva", Toast.LENGTH_SHORT).show();
            });
    }

    public void acceptInvitation(String invitationId, String receiverUserId) {
        // Get invitation details first
        db.collection("alliance_invitations").document(invitationId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String allianceId = document.getString("allianceId");

                    if (allianceId != null) {
                        // Check if user is already a member of this specific alliance
                        db.collection("alliances").document(allianceId)
                            .get()
                            .addOnSuccessListener(allianceDoc -> {
                                if (allianceDoc.exists()) {
                                    List<String> memberIds = (List<String>) allianceDoc.get("memberIds");

                                    if (memberIds != null && memberIds.contains(receiverUserId)) {
                                        // User is already a member of this alliance
                                        updateInvitationStatus(invitationId, AllianceInvitation.InvitationStatus.REJECTED);
                                        notificationManager.cancelInvitationNotification(invitationId);
                                        Toast.makeText(context, "Već ste član ovog saveza", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // User is not a member, proceed with acceptance
                                        processAcceptInvitation(invitationId, receiverUserId);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Greška pri proveri članstva u savezu", Toast.LENGTH_SHORT).show();
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Greška pri preuzimanju pozivnice", Toast.LENGTH_SHORT).show();
            });
    }

    private void processAcceptInvitation(String invitationId, String receiverUserId) {
        db.collection("alliance_invitations").document(invitationId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String allianceId = document.getString("allianceId");
                    String allianceName = document.getString("allianceName");
                    String senderUserId = document.getString("senderUserId");

                    if (allianceId != null) {
                        // Add user to alliance
                        addUserToAlliance(allianceId, receiverUserId, new Runnable() {
                            @Override
                            public void run() {
                                // Update invitation status
                                updateInvitationStatus(invitationId, AllianceInvitation.InvitationStatus.ACCEPTED);

                                // Cancel notification
                                notificationManager.cancelInvitationNotification(invitationId);

                                // Get user's username for notification
                                getUserUsername(receiverUserId, username -> {
                                    // Notify alliance leader
                                    notificationManager.sendAcceptanceNotification(
                                        senderUserId, username, allianceName
                                    );

                                    notificationManager.createSystemNotification(
                                        senderUserId,
                                        "Novi član saveza",
                                        username + " je prihvatio poziv u savez \"" + allianceName + "\"",
                                        "ALLIANCE_MEMBER_JOINED"
                                    );
                                });

                                Toast.makeText(context, "Uspešno ste pristupili savezu!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
    }

    public void rejectInvitation(String invitationId, String receiverUserId) {
        updateInvitationStatus(invitationId, AllianceInvitation.InvitationStatus.REJECTED);
        notificationManager.cancelInvitationNotification(invitationId);
        Toast.makeText(context, "Poziv odbačen", Toast.LENGTH_SHORT).show();
    }

    private void addUserToAlliance(String allianceId, String userId, Runnable onSuccess) {
        db.collection("alliances").document(allianceId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    List<String> memberIds = (List<String>) document.get("memberIds");
                    if (memberIds == null) memberIds = new ArrayList<>();

                    if (!memberIds.contains(userId)) {
                        memberIds.add(userId);

                        db.collection("alliances").document(allianceId)
                            .update("memberIds", memberIds)
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(e ->
                                Toast.makeText(context, "Greška pri pristupanju savezu", Toast.LENGTH_SHORT).show()
                            );
                    } else {
                        // User is already a member, just run success callback
                        onSuccess.run();
                    }
                }
            });
    }

    private void updateInvitationStatus(String invitationId, AllianceInvitation.InvitationStatus status) {
        db.collection("alliance_invitations").document(invitationId)
            .update("status", status.toString())
            .addOnFailureListener(e ->
                android.util.Log.e("InvitationManager", "Error updating invitation status", e)
            );
    }

    private void checkUserEligibility(String userId, UserEligibilityCallback callback) {
        // Check if user has pending invitations for this specific alliance
        db.collection("alliance_invitations")
            .whereEqualTo("receiverUserId", userId)
            .whereEqualTo("status", AllianceInvitation.InvitationStatus.PENDING.toString())
            .get()
            .addOnSuccessListener(inviteSnapshot -> {
                if (!inviteSnapshot.isEmpty()) {
                    callback.onResult(false, "Korisnik već ima neodgovoreni poziv");
                } else {
                    callback.onResult(true, "");
                }
            })
            .addOnFailureListener(e -> callback.onResult(false, "Greška pri proveri korisnika"));
    }

    private void checkUserEligibilityForAlliance(String allianceId, String userId, UserEligibilityCallback callback) {
        // Check if user is already a member of the alliance or has a pending invitation for it
        db.collection("alliances").document(allianceId)
            .get()
            .addOnSuccessListener(allianceDoc -> {
                if (allianceDoc.exists()) {
                    List<String> memberIds = (List<String>) allianceDoc.get("memberIds");
                    if (memberIds != null && memberIds.contains(userId)) {
                        callback.onResult(false, "Korisnik je već član ovog saveza");
                        return;
                    }

                    // Check for pending invitation
                    db.collection("alliance_invitations")
                        .whereEqualTo("allianceId", allianceId)
                        .whereEqualTo("receiverUserId", userId)
                        .whereEqualTo("status", AllianceInvitation.InvitationStatus.PENDING.toString())
                        .get()
                        .addOnSuccessListener(inviteSnapshot -> {
                            if (!inviteSnapshot.isEmpty()) {
                                callback.onResult(false, "Korisniku je već poslat poziv za ovaj savez");
                            } else {
                                callback.onResult(true, "");
                            }
                        })
                        .addOnFailureListener(e -> callback.onResult(false, "Greška pri proveri pozivnica"));
                } else {
                    callback.onResult(true, "");
                }
            })
            .addOnFailureListener(e -> callback.onResult(false, "Greška pri proveri saveza"));
    }

    private void getUserUsername(String userId, UsernameCallback callback) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String username = document.getString("username");
                    callback.onUsername(username != null ? username : "Nepoznat korisnik");
                } else {
                    callback.onUsername("Nepoznat korisnik");
                }
            })
            .addOnFailureListener(e -> callback.onUsername("Nepoznat korisnik"));
    }

    public interface UserEligibilityCallback {
        void onResult(boolean canInvite, String reason);
    }

    public interface UsernameCallback {
        void onUsername(String username);
    }
}
