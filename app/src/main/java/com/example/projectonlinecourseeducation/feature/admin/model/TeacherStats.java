package com.example.projectonlinecourseeducation.feature.admin.model;

import com.example.projectonlinecourseeducation.core.model.user.User;

public class TeacherStats {

    private User user;
    private int totalCourses;
    private double totalRevenue;

    public TeacherStats(User user, int totalCourses, double totalRevenue) {
        this.user = user;
        this.totalCourses = totalCourses;
        this.totalRevenue = totalRevenue;
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
}
