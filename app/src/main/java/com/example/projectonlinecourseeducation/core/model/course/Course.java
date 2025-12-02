package com.example.projectonlinecourseeducation.core.model.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {

    // --- Thông tin dùng chung ---
    private String id;
    private String title;
    private String teacher;
    private String imageUrl;
    private String category; // Java / C / C++ / Python ...
    private int lectures;    // số bài giảng
    private int students;    // số học viên
    private double rating;   // 0..5
    private double price;    // giá (VND)

    // --- Thông tin chi tiết cho màn Course Detail ---
    private String description;       // mô tả tổng quát khóa học
    private String createdAt;         // thời gian tạo
    private int ratingCount;          // số lượt đánh giá
    private int totalDurationMinutes; // tổng thời lượng khóa (phút)

    private List<String> skills;       // các skill / insight "Bạn sẽ học được gì"
    private List<String> requirements; // yêu cầu học viên

    // ====== Constructor rỗng (bắt buộc nên có để dùng với Gson/Retrofit/Room) ======
    public Course() {
        this.skills = new ArrayList<>();
        this.requirements = new ArrayList<>();
    }

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

        this(); // khởi tạo list rỗng trước

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

        setSkills(skills);
        setRequirements(requirements);
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

    // ====== Setter – thêm mới để fake CRUD & cho Gson/Retrofit dễ map ======

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setLectures(int lectures) { this.lectures = lectures; }
    public void setStudents(int students) { this.students = students; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPrice(double price) { this.price = price; }

    public void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = (createdAt != null) ? createdAt : "";
    }

    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public void setSkills(List<String> skills) {
        this.skills = (skills != null) ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = (requirements != null) ? new ArrayList<>(requirements) : new ArrayList<>();
    }
}