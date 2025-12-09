package com.example.projectonlinecourseeducation.data.mycourse;

import com.example.projectonlinecourseeducation.core.model.course.Course;

import java.util.List;

/**
 * API cho phần "Khóa học của tôi" (My Course).
 * Tưởng tượng tương đương các endpoint:
 * - GET    /my-courses
 * - POST   /my-courses
 * - GET    /my-courses/contains?courseId=...
 */
public interface MyCourseApi {

    /**
     * Lấy danh sách khóa học mà student đã mua / đã ghi danh.
     * Tương đương GET /my-courses
     */
    List<Course> getMyCourses();

    /**
     * Kiểm tra khóa học đã mua chưa.
     * Tương đương GET /my-courses/contains?courseId=...
     */
    boolean isPurchased(String courseId);

    /**
     * Thêm 1 khóa học vào My Course.
     * Tương đương POST /my-courses (1 item)
     */
    void addPurchasedCourse(Course course);

    /**
     * Thêm nhiều khóa học vào My Course (dùng khi thanh toán cả giỏ).
     * Tương đương POST /my-courses (nhiều item)
     */
    void addPurchasedCourses(List<Course> courses);

    /**
     * (Tùy chọn) Xóa toàn bộ danh sách My Course.
     * Chủ yếu phục vụ test/clear trong FakeApi.
     */
    void clearMyCourses();

    // ------------------ ADMIN: Get data for specific user ------------------

    /**
     * (ADMIN) Lấy danh sách khóa học đã mua của user cụ thể
     * @param userId ID của user cần xem
     * @return Danh sách courses đã mua
     */
    List<Course> getMyCoursesForUser(String userId);

    /**
     * (ADMIN) Kiểm tra user cụ thể đã mua khóa học chưa
     * @param courseId ID khóa học
     * @param userId ID của user cần check
     * @return true nếu đã mua, false nếu chưa
     */
    boolean isPurchasedForUser(String courseId, String userId);
}
