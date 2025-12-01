package com.example.projectonlinecourseeducation.data.lessonvideo;

import com.example.projectonlinecourseeducation.core.model.LessonVideo;

/**
 * Interface API cho LessonVideo
 * Quản lý thông tin video của từng bài học trong khóa học
 */
public interface LessonVideoApi {

    /**
     * Lấy thông tin video chi tiết của một bài học
     * @param lessonId: ID của bài học
     * @return LessonVideo object chứa URL video, mô tả, v.v.
     */
    LessonVideo getLessonVideoDetail(String lessonId);
}