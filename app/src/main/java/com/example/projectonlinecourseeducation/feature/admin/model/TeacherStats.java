package com.example.projectonlinecourseeducation.feature.admin.model;

import com.example.projectonlinecourseeducation.core.model.user.User;

/**
 * Model chứa thông tin teacher + statistics
 */
public class TeacherStats {
    private final User user;
    private final int totalCourses;        // Tổng số khóa học sở hữu
    private final double totalRevenue;     // Tổng thu nhập từ các khóa học
    private final double averageRating;    // Rating trung bình của các khóa học

    public TeacherStats(User user, int totalCourses, double totalRevenue, double averageRating) {
        this.user = user;
        this.totalCourses = totalCourses;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
    }

    public User getUser() {
        return user;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getAverageRating() {
        return averageRating;
    }
}