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

    /**
     * Tạo bài học mới
     * @param newLesson: Lesson object cần tạo
     * @return Lesson object đã được tạo (có ID)
     */
    Lesson createLesson(Lesson newLesson);

    /**
     * Cập nhật bài học
     * @param lessonId: ID bài học cần cập nhật
     * @param updatedLesson: Thông tin cập nhật
     * @return Lesson object sau khi cập nhật
     */
    Lesson updateLesson(String lessonId, Lesson updatedLesson);

    /**
     * Xóa bài học
     * @param lessonId: ID bài học cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    boolean deleteLesson(String lessonId);
}