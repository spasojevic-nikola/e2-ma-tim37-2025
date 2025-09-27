package com.example.newhabitquest;

import java.util.Date;

public class Task {
    public String id;
    public String userId;
    public String status; // kreiran, uradjen, neuradjen, otkazan
    public String category; // zdravlje, ucenje, posao, itd
    public int difficulty; // 1-5
    public int xp;
    public Date date;
    public boolean specialMission;

    public Task() {}

    public Task(String userId, String status, String category, int difficulty, int xp, Date date, boolean specialMission) {
        this.userId = userId;
        this.status = status;
        this.category = category;
        this.difficulty = difficulty;
        this.xp = xp;
        this.date = date;
        this.specialMission = specialMission;
    }
}

