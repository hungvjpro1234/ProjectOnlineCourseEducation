package com.example.projectonlinecourseeducation.data.mycourse.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CourseDto {

    @SerializedName("course_id")
    private int courseId;

    private String title;

    // Backend thường trả teacher_name hoặc teacher_id
    @SerializedName(value = "teacher", alternate = {"teacher_name"})
    private String teacher;

    @SerializedName("image")
    private String imageUrl;

    private String category;

    private int students;

    private double price;

    // Các field có thể CHƯA có ở backend (optional)
    private Double rating;
    private Integer ratingCount;
    private Integer lectures;
    private Integer totalDurationMinutes;

    private String description;
    private String createdAt;

    private List<String> skills;
    private List<String> requirements;

    // Approval flags (backend có thể chưa trả)
    private Boolean isInitialApproved;
    private Boolean isEditApproved;
    private Boolean isDeleteRequested;

    // -------- getters --------

    public int getCourseId() { return courseId; }

    public String getTitle() { return title; }

    public String getTeacher() { return teacher; }

    public String getImageUrl() { return imageUrl; }

    public String getCategory() { return category; }

    public int getStudents() { return students; }

    public double getPrice() { return price; }

    public Double getRating() { return rating; }

    public Integer getRatingCount() { return ratingCount; }

    public Integer getLectures() { return lectures; }

    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }

    public String getDescription() { return description; }

    public String getCreatedAt() { return createdAt; }

    public List<String> getSkills() { return skills; }

    public List<String> getRequirements() { return requirements; }

    public Boolean getInitialApproved() { return isInitialApproved; }

    public Boolean getEditApproved() { return isEditApproved; }

    public Boolean getDeleteRequested() { return isDeleteRequested; }
}
