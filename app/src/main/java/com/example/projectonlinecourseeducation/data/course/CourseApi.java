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
}