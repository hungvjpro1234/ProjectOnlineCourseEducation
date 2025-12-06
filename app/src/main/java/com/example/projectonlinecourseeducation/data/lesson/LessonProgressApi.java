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

    // ------------------ LISTENER / NOTIFY ------------------

    /**
     * Listener để UI hoặc các component khác đăng ký nhận thông báo khi
     * tiến độ của 1 lesson thay đổi (update/markCompleted).
     *
     * Khi onLessonProgressChanged(lessonId) được gọi, component nên gọi lại
     * getLessonProgress(lessonId) để lấy trạng thái mới.
     */
    interface LessonProgressUpdateListener {
        /**
         * Thông báo tiến độ của lessonId đã thay đổi.
         * @param lessonId id của lesson thay đổi (có thể null/empty nếu là thay đổi chung)
         */
        void onLessonProgressChanged(String lessonId);
    }

    /**
     * Đăng ký listener.
     */
    void addLessonProgressUpdateListener(LessonProgressUpdateListener listener);

    /**
     * Hủy đăng ký listener.
     */
    void removeLessonProgressUpdateListener(LessonProgressUpdateListener listener);
}
