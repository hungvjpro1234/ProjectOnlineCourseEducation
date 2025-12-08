package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation cho CourseStudentApi (in-memory).
 * Chỉ phục vụ dev/test. Khi remote chuyển sang Retrofit, implement lại và set vào ApiProvider.
 *
 * NOTE: KHÔNG seed hardcoded students nữa — nếu cần demo, dùng helper addStudentToCourse().
 */
public class CourseStudentFakeApiService implements CourseStudentApi {

    private static CourseStudentFakeApiService instance;
    public static CourseStudentFakeApiService getInstance() {
        if (instance == null) instance = new CourseStudentFakeApiService();
        return instance;
    }

    // courseId -> list students
    private final java.util.Map<String, List<CourseStudent>> courseStudents = new java.util.HashMap<>();
    private final List<StudentUpdateListener> listeners = new ArrayList<>();

    private CourseStudentFakeApiService() {
        // no default seeding here.
        // If you want to seed data for local dev only, call addStudentToCourse(...) from a dev entrypoint.
    }

    @Override
    public synchronized List<CourseStudent> getStudentsForCourse(String courseId) {
        if (courseId == null) return new ArrayList<>();
        List<CourseStudent> list = courseStudents.get(courseId);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list); // return copy
    }

    @Override
    public synchronized void addStudentUpdateListener(StudentUpdateListener l) {
        if (l == null) return;
        if (!listeners.contains(l)) listeners.add(l);
    }

    @Override
    public synchronized void removeStudentUpdateListener(StudentUpdateListener l) {
        listeners.remove(l);
    }

    /**
     * DEV helper: thêm học viên và notify listeners
     * Bạn có thể gọi method này từ unit-test hoặc dev tooling để tạo demo data.
     */
    public synchronized void addStudentToCourse(String courseId, CourseStudent student) {
        if (courseId == null || student == null) return;
        List<CourseStudent> list = courseStudents.get(courseId);
        if (list == null) {
            list = new ArrayList<>();
            courseStudents.put(courseId, list);
        }
        list.add(student);
        notifyStudentsChanged(courseId);
    }

    private synchronized void notifyStudentsChanged(String courseId) {
        List<StudentUpdateListener> copy = new ArrayList<>(listeners);
        for (StudentUpdateListener l : copy) {
            try {
                l.onStudentsChanged(courseId);
            } catch (Exception ignored) {}
        }
    }
}
