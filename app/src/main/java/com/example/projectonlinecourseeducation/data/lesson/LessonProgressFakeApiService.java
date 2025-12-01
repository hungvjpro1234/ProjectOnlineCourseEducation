package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.LessonProgress;

import java.util.HashMap;
import java.util.Map;

/**
 * Fake API Service cho LessonProgress
 * Lưu trữ tracking progress của từng bài học (memory-based)
 * Sau này có thể thay bằng LessonProgressRemoteApiService kết nối backend
 */
public class LessonProgressFakeApiService implements LessonProgressApi {

    // Singleton
    private static LessonProgressFakeApiService instance;
    public static LessonProgressFakeApiService getInstance() {
        if (instance == null) instance = new LessonProgressFakeApiService();
        return instance;
    }

    // In-memory cache: lessonId -> LessonProgress
    // Thực tế sẽ lưu trong database/API
    private final Map<String, LessonProgress> progressCache = new HashMap<>();

    private LessonProgressFakeApiService() {
        // Khởi tạo với progress mặc định (0% progress)
    }

    @Override
    public LessonProgress getLessonProgress(String lessonId) {
        if (lessonId == null) return null;

        // Nếu chưa có progress, tạo mới với giá trị 0
        if (!progressCache.containsKey(lessonId)) {
            LessonProgress defaultProgress = new LessonProgress(
                    "progress_" + lessonId,
                    lessonId,
                    extractCourseId(lessonId),
                    0f,
                    0f,
                    0,
                    false,
                    System.currentTimeMillis()
            );
            progressCache.put(lessonId, defaultProgress);
        }

        return progressCache.get(lessonId);
    }

    @Override
    public void updateLessonProgress(String lessonId, float currentSecond, float totalSecond) {
        if (lessonId == null) return;

        // Tính completion percentage
        int completionPercentage = 0;
        if (totalSecond > 0) {
            completionPercentage = (int) ((currentSecond / totalSecond) * 100);
            completionPercentage = Math.min(100, Math.max(0, completionPercentage));
        }

        // Kiểm tra completion (>= 90%)
        boolean isCompleted = completionPercentage >= 90;

        // Cập nhật progress
        LessonProgress updated = new LessonProgress(
                "progress_" + lessonId,
                lessonId,
                extractCourseId(lessonId),
                currentSecond,
                totalSecond,
                completionPercentage,
                isCompleted,
                System.currentTimeMillis()
        );

        progressCache.put(lessonId, updated);

        // TODO: Gửi update lên backend
        // POST /api/lesson-progress/{lessonId} {currentSecond, totalSecond}
    }

    @Override
    public void markLessonAsCompleted(String lessonId) {
        if (lessonId == null) return;

        LessonProgress current = progressCache.get(lessonId);
        if (current == null) {
            current = getLessonProgress(lessonId);
        }

        // Tạo completed version
        LessonProgress completed = new LessonProgress(
                "progress_" + lessonId,
                lessonId,
                current.getCourseId(),
                current.getTotalSecond(),
                current.getTotalSecond(),
                100,
                true,
                System.currentTimeMillis()
        );

        progressCache.put(lessonId, completed);

        // TODO: Gửi marking lên backend
        // POST /api/lesson-progress/{lessonId}/mark-completed
    }

    /**
     * Helper: extract courseId từ lessonId (format: c1_l1, c2_l3, etc.)
     */
    private String extractCourseId(String lessonId) {
        if (lessonId != null && lessonId.contains("_")) {
            return lessonId.split("_")[0]; // "c1" from "c1_l1"
        }
        return "";
    }
}