package com.example.newhabitquest;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskRepository {
    public static void addTask(Task task) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", task.userId);
        data.put("status", task.status);
        data.put("category", task.category);
        data.put("difficulty", task.difficulty);
        data.put("xp", task.xp);
        data.put("date", task.date);
        data.put("specialMission", task.specialMission);
        db.collection("tasks").add(data);
    }
}

