package com.example.projectonlinecourseeducation.data.review;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;

import java.util.List;

public interface ReviewApi {

    // Mỗi khóa học có review riêng
    List<CourseReview> getReviewsForCourse(String courseId);

    // Thêm review mới cho khóa học
    CourseReview addReviewToCourse(String courseId, String studentName, float rating, String comment);

    // ------------------ LISTENER / NOTIFY ------------------

    /**
     * Listener để UI hoặc các component khác đăng ký nhận thông báo khi review thay đổi.
     * Khi onReviewsChanged(courseId) được gọi, component nên gọi lại getReviewsForCourse(courseId)
     * để lấy trạng thái mới.
     */
    interface ReviewUpdateListener {
        /**
         * Gọi khi review của một course thay đổi (add/remove/update).
         * courseId có thể là null nếu thay đổi áp dụng cho nhiều course (tuỳ implement).
         */
        void onReviewsChanged(String courseId);
    }

    /**
     * Đăng ký listener.
     */
    void addReviewUpdateListener(ReviewUpdateListener listener);

    /**
     * Hủy đăng ký listener.
     */
    void removeReviewUpdateListener(ReviewUpdateListener listener);
}
