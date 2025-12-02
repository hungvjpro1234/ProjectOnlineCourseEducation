package com.example.projectonlinecourseeducation.data.review;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;

import java.util.List;

public interface ReviewApi {

    // Mỗi khóa học có review riêng
    List<CourseReview> getReviewsForCourse(String courseId);
}
