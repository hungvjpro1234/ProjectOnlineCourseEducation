package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import java.util.List;

public interface LessonApi {

    /**
     * Lấy danh sách bài học của một khóa học
     * @param courseId: ID khóa học
     * @return danh sách Lesson
     */
    List<Lesson> getLessonsForCourse(String courseId);

    /**
     * Lấy chi tiết bài học (bao gồm videoUrl, description, order)
     * @param lessonId: ID bài học
     * @return Lesson object
     */
    Lesson getLessonDetail(String lessonId);
}