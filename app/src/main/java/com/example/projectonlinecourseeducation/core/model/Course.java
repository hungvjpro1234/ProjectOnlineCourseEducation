package com.example.projectonlinecourseeducation.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {

    // --- Thông tin dùng chung ---
    private final String id;
    private final String title;
    private final String teacher;
    private final String imageUrl;
    private final String category; // Java / C / C++ / Python ...
    private final int lectures;    // số bài giảng
    private final int students;    // số học viên
    private final double rating;   // 0..5
    private final double price;    // giá (VND)

    // --- Thông tin chi tiết cho màn Course Detail ---
    private final String description;       // mô tả tổng quát khóa học
    private final String createdAt;         // thời gian tạo
    private final int ratingCount;          // số lượt đánh giá
    private final int totalDurationMinutes; // tổng thời lượng khóa (phút)

    private final List<String> skills;       // các skill / insight "Bạn sẽ học được gì"
    private final List<String> requirements; // yêu cầu học viên

    // Constructor cũ – để code cũ vẫn chạy (ví dụ màn Home, Fake API cũ...)
    public Course(String id, String title, String teacher, String imageUrl,
                  String category, int lectures, int students,
                  double rating, double price) {
        this(id, title, teacher, imageUrl, category, lectures, students,
                rating, price,
                "", "", 0, 0,
                null, null);
    }

    // Constructor đầy đủ dùng cho màn chi tiết
    public Course(String id, String title, String teacher, String imageUrl,
                  String category, int lectures, int students,
                  double rating, double price,
                  String description, String createdAt,
                  int ratingCount, int totalDurationMinutes,
                  List<String> skills, List<String> requirements) {

        this.id = id;
        this.title = title;
        this.teacher = teacher;
        this.imageUrl = imageUrl;
        this.category = category;
        this.lectures = lectures;
        this.students = students;
        this.rating = rating;
        this.price = price;

        this.description = description != null ? description : "";
        this.createdAt = createdAt != null ? createdAt : "";
        this.ratingCount = ratingCount;
        this.totalDurationMinutes = totalDurationMinutes;

        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.requirements = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
    }

    // --- Getter cũ ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getLectures() { return lectures; }
    public int getStudents() { return students; }
    public double getRating() { return rating; }
    public double getPrice() { return price; }

    // --- Getter mới cho màn chi tiết ---
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public int getRatingCount() { return ratingCount; }
    public int getTotalDurationMinutes() { return totalDurationMinutes; }

    public List<String> getSkills() { return Collections.unmodifiableList(skills); }
    public List<String> getRequirements() { return Collections.unmodifiableList(requirements); }
}
