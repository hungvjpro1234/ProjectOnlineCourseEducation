package com.example.projectonlinecourseeducation.feature.admin.model;

import com.example.projectonlinecourseeducation.core.model.course.Course;

/**
 * Model chứa thông tin course + progress statistics cho student
 */
public class CourseProgressStats {
    private final Course course;
    private final int totalLessons;
    private final int completedLessons;
    private final int progressPercentage;

    public CourseProgressStats(Course course, int totalLessons, int completedLessons) {
        this.course = course;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.progressPercentage = totalLessons > 0
            ? (int) ((completedLessons * 100.0) / totalLessons)
            : 0;
    }

    public Course getCourse() {
        return course;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }
}