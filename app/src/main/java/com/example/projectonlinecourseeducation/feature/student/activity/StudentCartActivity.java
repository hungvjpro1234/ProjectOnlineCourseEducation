package com.example.projectonlinecourseeducation.feature.student.activity;

import com.example.projectonlinecourseeducation.core.model.Course;

import java.util.ArrayList;
import java.util.List;

public class StudentCartActivity {
    private static StudentCartActivity instance;
    private List<Course> cartCourses = new ArrayList<>();

    public static StudentCartActivity getInstance() {
        if (instance == null)
            instance = new StudentCartActivity();
        return instance;
    }

    public void addCourse(Course course) { cartCourses.add(course); }
    public void removeCourse(Course course) { cartCourses.remove(course); }
    public List<Course> getCartCourses() { return new ArrayList<>(cartCourses); }
    public void clearCart() { cartCourses.clear(); }
}
