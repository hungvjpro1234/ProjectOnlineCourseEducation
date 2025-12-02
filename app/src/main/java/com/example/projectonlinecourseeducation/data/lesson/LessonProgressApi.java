package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;

/**
 * Interface API cho LessonProgress
 * Quản lý tracking tiến độ xem video của từng bài học
 */
public interface LessonProgressApi {

    /**
     * Lấy tiến độ của một bài học
     * @param lessonId: ID bài học
     * @return LessonProgress
     */
    LessonProgress getLessonProgress(String lessonId);

    /**
     * Cập nhật tiến độ bài học (gọi liên tục khi xem video)
     * @param lessonId: ID bài học
     * @param currentSecond: vị trí hiện tại (giây)
     * @param totalSecond: tổng thời lượng (giây)
     */
    void updateLessonProgress(String lessonId, float currentSecond, float totalSecond);

    /**
     * Đánh dấu bài học là hoàn thành
     * @param lessonId: ID bài học
     */
    void markLessonAsCompleted(String lessonId);
}