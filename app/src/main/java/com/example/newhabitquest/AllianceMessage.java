package com.example.newhabitquest;

public class AllianceMessage {
    private String messageId;
    private String allianceId;
    private String senderUserId;
    private String senderUsername;
    private String messageText;
    private long timestamp;

    public AllianceMessage() {
        // Required empty constructor for Firestore
    }

    public AllianceMessage(String messageId, String allianceId, String senderUserId,
                          String senderUsername, String messageText) {
        this.messageId = messageId;
        this.allianceId = allianceId;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.messageText = messageText;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getSenderUserId() { return senderUserId; }
    public void setSenderUserId(String senderUserId) { this.senderUserId = senderUserId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
