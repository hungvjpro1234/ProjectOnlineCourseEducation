package com.example.projectonlinecourseeducation.data.mycourse;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake implementation cho MyCourseApi.
 * Lưu danh sách khóa học đã mua trong RAM, không có backend.
 * Sau này thay bằng MyCourseRemoteApiService (Retrofit) là xong.
 *
 * FIX: Phân quyền my courses theo userId - mỗi user có danh sách khóa học riêng
 */
public class MyCourseFakeApiService implements MyCourseApi {

    private static MyCourseFakeApiService instance;

    public static MyCourseFakeApiService getInstance() {
        if (instance == null) {
            instance = new MyCourseFakeApiService();
        }
        return instance;
    }

    // "Bảng" my_courses trong RAM - PER USER (key = userId)
    private final Map<String, List<Course>> myCoursesMap = new HashMap<>();

    private MyCourseFakeApiService() {
    }

    /**
     * Helper: Lấy userId của user hiện tại
     */
    private String getCurrentUserId() {
        User currentUser = ApiProvider.getAuthApi() != null
            ? ApiProvider.getAuthApi().getCurrentUser()
            : null;
        if (currentUser == null || currentUser.getId() == null) {
            return "_GUEST_"; // fallback for guest/unauthenticated users
        }
        return currentUser.getId();
    }

    /**
     * Helper: Lấy danh sách my courses của user hiện tại
     */
    private List<Course> getCurrentUserMyCourses() {
        String userId = getCurrentUserId();
        if (!myCoursesMap.containsKey(userId)) {
            myCoursesMap.put(userId, new ArrayList<>());
        }
        return myCoursesMap.get(userId);
    }

    @Override
    public synchronized List<Course> getMyCourses() {
        // trả bản copy của my courses user hiện tại để UI không chỉnh trực tiếp list bên trong
        return new ArrayList<>(getCurrentUserMyCourses());
    }

    @Override
    public synchronized boolean isPurchased(String courseId) {
        if (courseId == null) return false;
        List<Course> userMyCourses = getCurrentUserMyCourses();
        for (Course c : userMyCourses) {
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
        List<Course> userMyCourses = getCurrentUserMyCourses();
        userMyCourses.add(course);
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
        List<Course> userMyCourses = getCurrentUserMyCourses();
        userMyCourses.clear();
    }

    // ------------------ ADMIN: Get data for specific user ------------------

    @Override
    public synchronized List<Course> getMyCoursesForUser(String userId) {
        if (userId == null) return new ArrayList<>();
        if (!myCoursesMap.containsKey(userId)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(myCoursesMap.get(userId));
    }

    @Override
    public synchronized boolean isPurchasedForUser(String courseId, String userId) {
        if (courseId == null || userId == null) return false;
        List<Course> userCourses = getMyCoursesForUser(userId);
        for (Course c : userCourses) {
            if (c != null && courseId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }
}
