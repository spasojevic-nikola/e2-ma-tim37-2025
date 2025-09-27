package com.example.newhabitquest;

import java.util.ArrayList;
import java.util.List;

public class Alliance {
    private String allianceId;
    private String name;
    private String description;
    private String leaderId;
    private List<String> memberIds;
    private String currentTask;
    private int maxMembers;
    private long createdTimestamp;
    private boolean isActive;
    private int totalPoints;
    private boolean hasMissionActive; // New field to track active missions

    public Alliance() {
        memberIds = new ArrayList<>();
    }

    public Alliance(String allianceId, String name, String description, String leaderId) {
        this.allianceId = allianceId;
        this.name = name;
        this.description = description;
        this.leaderId = leaderId;
        this.memberIds = new ArrayList<>();
        this.memberIds.add(leaderId); // Leader is automatically a member
        this.maxMembers = 5; // Default max members
        this.createdTimestamp = System.currentTimeMillis();
        this.isActive = true;
        this.totalPoints = 0;
        this.hasMissionActive = false; // Default no active mission
    }

    // Getters and Setters
    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getCurrentTask() { return currentTask; }
    public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }

    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }

    public long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public boolean isFull() {
        return memberIds.size() >= maxMembers;
    }

    public boolean hasMissionActive() { return hasMissionActive; }
    public void setHasMissionActive(boolean hasMissionActive) { this.hasMissionActive = hasMissionActive; }

    public boolean canJoin() {
        return isActive && !isFull();
    }

    public boolean canLeave() {
        return !hasMissionActive; // Users can't leave during active missions
    }

    public boolean canDisband() {
        return !hasMissionActive; // Leader can't disband during active missions
    }
}
