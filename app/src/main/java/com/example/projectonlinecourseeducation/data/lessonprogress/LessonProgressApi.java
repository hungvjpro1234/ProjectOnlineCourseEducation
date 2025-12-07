package com.example.projectonlinecourseeducation.data.lessonprogress;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;

/**
 * Interface API cho LessonProgress
 *
 * - Thêm overload hỗ trợ studentId để lưu/đọc per-student progress.
 * - Các phương thức cũ vẫn tồn tại để giữ tương thích; implementer có thể override new methods.
 */
public interface LessonProgressApi {

    /**
     * Legacy: Lấy tiến độ của một bài học (không phân biệt student).
     * @param lessonId ID lesson
     * @return LessonProgress (legacy/global)
     */
    LessonProgress getLessonProgress(String lessonId);

    /**
     * NEW: Lấy tiến độ của một bài học cho một student cụ thể.
     * Default: fallback về getLessonProgress(lessonId) nếu implementer không hỗ trợ per-student.
     *
     * @param lessonId id bài học
     * @param studentId id học viên (có thể null để lấy global)
     * @return LessonProgress
     */
    default LessonProgress getLessonProgress(String lessonId, String studentId) {
        return getLessonProgress(lessonId);
    }

    /**
     * Legacy: Cập nhật tiến độ (không có studentId)
     */
    void updateLessonProgress(String lessonId, float currentSecond, float totalSecond);

    /**
     * NEW: Cập nhật tiến độ cho student cụ thể.
     * Default: fallback gọi updateLessonProgress(lessonId, currentSecond, totalSecond) nếu implementer cũ.
     */
    default void updateLessonProgress(String lessonId, float currentSecond, float totalSecond, String studentId) {
        updateLessonProgress(lessonId, currentSecond, totalSecond);
    }

    /**
     * Legacy: Đánh dấu hoàn thành (không có studentId)
     */
    void markLessonAsCompleted(String lessonId);

    /**
     * NEW: Đánh dấu hoàn thành cho student cụ thể.
     * Default: fallback gọi markLessonAsCompleted(lessonId)
     */
    default void markLessonAsCompleted(String lessonId, String studentId) {
        markLessonAsCompleted(lessonId);
    }

    // ------------------ LISTENER / NOTIFY ------------------

    interface LessonProgressUpdateListener {
        /**
         * Thông báo tiến độ của lessonId đã thay đổi.
         * Nếu provider hỗ trợ per-student, listener có thể gọi lại getLessonProgress(lessonId, studentId).
         * @param lessonId id của lesson thay đổi (có thể null/empty nếu là thay đổi chung)
         */
        void onLessonProgressChanged(String lessonId);
    }

    void addLessonProgressUpdateListener(LessonProgressUpdateListener listener);
    void removeLessonProgressUpdateListener(LessonProgressUpdateListener listener);
}
