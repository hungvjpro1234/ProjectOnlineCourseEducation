package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

/**
 * Interface API cho LessonVideo
 * Quản lý thông tin video của từng bài học trong khóa học.
 *
 * Dù tên vẫn là LessonVideoApi nhưng model dùng chung là Lesson.
 */
public interface LessonVideoApi {

    /**
     * Lấy thông tin video chi tiết của một bài học
     * @param lessonId: ID của bài học
     * @return Lesson object chứa URL video, mô tả, v.v.
     */
    Lesson getLessonVideoDetail(String lessonId);
}
