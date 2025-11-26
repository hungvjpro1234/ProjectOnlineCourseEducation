package com.example.projectonlinecourseeducation.data.mycourse;

import com.example.projectonlinecourseeducation.core.model.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation cho MyCourseApi.
 * Lưu danh sách khóa học đã mua trong RAM, không có backend.
 * Sau này thay bằng MyCourseRemoteApiService (Retrofit) là xong.
 */
public class MyCourseFakeApiService implements MyCourseApi {

    private static MyCourseFakeApiService instance;

    public static MyCourseFakeApiService getInstance() {
        if (instance == null) {
            instance = new MyCourseFakeApiService();
        }
        return instance;
    }

    // "Bảng" my_courses trong RAM
    private final List<Course> myCourses = new ArrayList<>();

    private MyCourseFakeApiService() {
    }

    @Override
    public synchronized List<Course> getMyCourses() {
        // trả bản copy để UI không chỉnh trực tiếp list bên trong
        return new ArrayList<>(myCourses);
    }

    @Override
    public synchronized boolean isPurchased(String courseId) {
        if (courseId == null) return false;
        for (Course c : myCourses) {
            if (c != null && courseId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void addPurchasedCourse(Course course) {
        if (course == null || course.getId() == null) return;
        // Không thêm trùng courseId
        if (isPurchased(course.getId())) return;
        myCourses.add(course);
    }

    @Override
    public synchronized void addPurchasedCourses(List<Course> courses) {
        if (courses == null) return;
        for (Course c : courses) {
            addPurchasedCourse(c);
        }
    }

    @Override
    public synchronized void clearMyCourses() {
        myCourses.clear();
    }
}
