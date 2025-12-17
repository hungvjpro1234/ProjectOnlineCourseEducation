package com.example.projectonlinecourseeducation.data.course.remote;

import java.util.List;

public class CourseDto {
    public String id;
    public String title;
    public String teacher;
    public String imageUrl;
    public String category;
    public int lectures;
    public int students;
    public double rating;
    public double price;

    public String description;
    public String createdAt;
    public int ratingCount;
    public int totalDurationMinutes;

    public List<String> skills;
    public List<String> requirements;

    public boolean isInitialApproved;
    public boolean isEditApproved;
    public boolean isDeleteRequested;
}
