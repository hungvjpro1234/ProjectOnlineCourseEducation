package com.example.projectonlinecourseeducation.core.model.course;

public class CourseReview {
    private final String id;           // Mã review
    private final String courseId;     // Mã khóa học
    private final String studentName;
    private final float rating;        // 0..5
    private final String comment;      // nội dung đánh giá
    private final long createdAt;      // Timestamp tạo review

    public CourseReview(String id, String courseId, String studentName, float rating, String comment, long createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.studentName = studentName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getStudentName() { return studentName; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getCreatedAt() { return createdAt; }
}