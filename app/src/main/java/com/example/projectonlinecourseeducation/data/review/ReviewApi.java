package com.example.projectonlinecourseeducation.data.review;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;

import java.util.List;

public interface ReviewApi {

    // Mỗi khóa học có review riêng
    List<CourseReview> getReviewsForCourse(String courseId);

    // Thêm review mới cho khóa học
    CourseReview addReviewToCourse(String courseId, String studentName, float rating, String comment);
}