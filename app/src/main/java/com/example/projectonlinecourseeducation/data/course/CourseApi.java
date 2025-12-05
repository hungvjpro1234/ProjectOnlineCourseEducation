package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.Course;

import java.util.List;

public interface CourseApi {

    // Sort cho màn Home / list
    enum Sort { AZ, ZA, RATING_UP, RATING_DOWN }

    // ------------------ LIST / HOME ------------------
    List<Course> listAll();

    List<Course> filterSearchSort(
            String categoryOrAll,
            String query,
            Sort sort,
            int limit
    );

    // NEW: Lấy danh sách khóa học do teacher tạo
    List<Course> getCoursesByTeacher(String teacherName);

    // ------------------ DETAIL ------------------
    Course getCourseDetail(String courseId);

    List<Course> getRelatedCourses(String courseId);

    // ------------------ CRUD (fake DB trong RAM) ------------------
    Course createCourse(Course newCourse);

    Course updateCourse(String id, Course updatedCourse);

    boolean deleteCourse(String id);

    // Tính toán lại rating của khóa học từ danh sách reviews
    Course recalculateCourseRating(String courseId);

    /**
     * Ghi nhận 1 giao dịch mua (thực tế) cho 1 khóa học.
     * Khi backend thật, endpoint này sẽ tăng số học viên (students) và có thể tạo log/transaction.
     * Trả về Course đã cập nhật (hoặc null nếu không tìm thấy).
     *
     * Trong FakeApi sẽ tăng students +1 và trả về Course.
     */
    Course recordPurchase(String courseId);

    // ------------------ COURSE UPDATE LISTENER ------------------
    /**
     * Listener để UI hoặc các thành phần khác đăng ký nhận sự kiện khi 1 Course thay đổi.
     * courseId là id của course thay đổi; updatedCourse là đối tượng Course mới (null nếu course bị xóa).
     */
    interface CourseUpdateListener {
        void onCourseUpdated(String courseId, Course updatedCourse);
    }

    /**
     * Đăng ký listener nhận sự kiện cập nhật course.
     */
    void addCourseUpdateListener(CourseUpdateListener l);

    /**
     * Hủy đăng ký listener.
     */
    void removeCourseUpdateListener(CourseUpdateListener l);
}
