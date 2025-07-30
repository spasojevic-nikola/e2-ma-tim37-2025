package com.example.habitquest;

public class TaskCategory {
    private int id;
    private String name;
    private String color; // heks kod boje, može i int

    public TaskCategory(int id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public int getId() { return id; }
}
