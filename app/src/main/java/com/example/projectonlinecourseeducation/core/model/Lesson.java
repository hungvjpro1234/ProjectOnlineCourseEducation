package com.example.projectonlinecourseeducation.core.model;

public class Lesson {
    private final String id;
    private final String title;
    private final String duration; // ví dụ: "12:30" hoặc "15 phút"

    public Lesson(String id, String title, String duration) {
        this.id = id;
        this.title = title;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
}
