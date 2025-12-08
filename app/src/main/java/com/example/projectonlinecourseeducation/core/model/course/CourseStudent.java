package com.example.projectonlinecourseeducation.core.model.course;

/**
 * Đơn giản model CourseStudent để UI hiển thị tên, avatar, enrollment time.
 */
public class CourseStudent {
    private String id;
    private String name;
    private String avatarUrl;
    private long enrolledAt; // timestamp millis

    public CourseStudent() {}

    public CourseStudent(String id, String name, String avatarUrl, long enrolledAt) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.enrolledAt = enrolledAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public long getEnrolledAt() { return enrolledAt; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setEnrolledAt(long enrolledAt) { this.enrolledAt = enrolledAt; }
}
