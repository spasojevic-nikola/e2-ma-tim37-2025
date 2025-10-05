package com.example.newhabitquest;

public class AllianceInvitation {
    private String invitationId;
    private String allianceId;
    private String allianceName;
    private String senderUserId;
    private String senderUsername;
    private String receiverUserId;
    private long timestamp;
    private InvitationStatus status;
    private String message;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public AllianceInvitation() {
        // Default constructor for Firestore
    }

    public AllianceInvitation(String invitationId, String allianceId, String allianceName,
                             String senderUserId, String senderUsername, String receiverUserId, String message) {
        this.invitationId = invitationId;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.receiverUserId = receiverUserId;
        this.timestamp = System.currentTimeMillis();
        this.status = InvitationStatus.PENDING;
        this.message = message;
    }

    // Getters and Setters
    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getAllianceName() { return allianceName; }
    public void setAllianceName(String allianceName) { this.allianceName = allianceName; }

    public String getSenderUserId() { return senderUserId; }
    public void setSenderUserId(String senderUserId) { this.senderUserId = senderUserId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(String receiverUserId) { this.receiverUserId = receiverUserId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }
}
