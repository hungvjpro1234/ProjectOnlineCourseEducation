package com.example.projectonlinecourseeducation.core.model;

public class CourseReview {
    private final String studentName;
    private final float rating;   // 0..5
    private final String comment; // nội dung đánh giá

    public CourseReview(String studentName, float rating, String comment) {
        this.studentName = studentName;
        this.rating = rating;
        this.comment = comment;
    }

    public String getStudentName() { return studentName; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
}
