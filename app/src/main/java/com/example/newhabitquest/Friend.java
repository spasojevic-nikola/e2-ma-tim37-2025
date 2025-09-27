package com.example.newhabitquest;

public class Friend {
    private String userId;
    private String username;
    private String avatar;
    private int level;
    private String status; // "pending", "accepted", "blocked"
    private long addedTimestamp;

    public Friend() {}

    public Friend(String userId, String username, String avatar, int level) {
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.status = "pending";
        this.addedTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }
}
