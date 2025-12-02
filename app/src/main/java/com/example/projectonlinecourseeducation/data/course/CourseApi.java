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

    // ------------------ DETAIL ------------------
    Course getCourseDetail(String courseId);

    List<Course> getRelatedCourses(String courseId);

    // ------------------ CRUD (fake DB trong RAM) ------------------
    Course createCourse(Course newCourse);

    Course updateCourse(String id, Course updatedCourse);

    boolean deleteCourse(String id);
}
