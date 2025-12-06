package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake API Service cho LessonProgress
 * Lưu trữ tracking progress của từng bài học (memory-based).
 *
 * QUY ƯỚC (bắt chước backend thật):
 *  - Luôn lưu "mốc tiến độ cao nhất" mà user đã xem (không cho % bị giảm khi tua lại).
 *  - isCompleted = true khi completionPercentage >= 90% hoặc đã được markCompleted trước đó.
 *  - UI chỉ làm việc qua LessonProgressApi, nên sau này có LessonProgressRemoteApiService
 *    thì chỉ cần set vào ApiProvider, không phải sửa UI.
 *
 *  CHANGES:
 *  - Thêm cơ chế LessonProgressUpdateListener để UI có thể đăng ký nhận notify
 *    khi có thay đổi update/markCompleted (tương tự CartFakeApiService).
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

    // Registered listeners
    private final List<LessonProgressApi.LessonProgressUpdateListener> listeners = new ArrayList<>();

    private LessonProgressFakeApiService() {
        // Khởi tạo với progress mặc định (0% progress)
    }

    @Override
    public synchronized LessonProgress getLessonProgress(String lessonId) {
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

        // Trả về đối tượng trong cache (immutable nếu LessonProgress là immutable)
        return progressCache.get(lessonId);
    }

    @Override
    public synchronized void updateLessonProgress(String lessonId, float currentSecond, float totalSecond) {
        if (lessonId == null) return;

        // Lấy progress hiện tại (nếu có) để so sánh, đảm bảo chỉ tăng tiến độ, không giảm.
        LessonProgress existing = progressCache.get(lessonId);

        float bestCurrentSecond = currentSecond;
        float bestTotalSecond = totalSecond;

        if (existing != null) {
            // Giữ tổng thời lượng lớn nhất (thường là cùng 1 giá trị, phòng trường hợp lần đầu = 0)
            if (bestTotalSecond <= 0 && existing.getTotalSecond() > 0) {
                bestTotalSecond = existing.getTotalSecond();
            }

            // Luôn lưu vị trí xem lớn nhất (max)
            bestCurrentSecond = Math.max(existing.getCurrentSecond(), currentSecond);
        }

        // Tính completion percentage dựa trên mốc cao nhất
        int completionPercentage = 0;
        if (bestTotalSecond > 0) {
            completionPercentage = (int) ((bestCurrentSecond / bestTotalSecond) * 100);
            completionPercentage = Math.min(100, Math.max(0, completionPercentage));
        }

        // Nếu đã từng completed thì giữ nguyên completed = true
        boolean wasCompleted = existing != null && existing.isCompleted();
        boolean isCompleted = wasCompleted || completionPercentage >= 90;

        // Cập nhật progress
        LessonProgress updated = new LessonProgress(
                "progress_" + lessonId,
                lessonId,
                extractCourseId(lessonId),
                bestCurrentSecond,
                bestTotalSecond,
                completionPercentage,
                isCompleted,
                System.currentTimeMillis()
        );

        progressCache.put(lessonId, updated);

        // Notify listeners về lesson này đã thay đổi
        notifyLessonProgressChanged(lessonId);

        // TODO Backend thật:
        // PATCH /api/lesson-progress/{lessonId}
        // body: { currentSecond: bestCurrentSecond, totalSecond: bestTotalSecond }
    }

    @Override
    public synchronized void markLessonAsCompleted(String lessonId) {
        if (lessonId == null) return;

        LessonProgress current = progressCache.get(lessonId);
        if (current == null) {
            current = getLessonProgress(lessonId);
        }

        float total = current.getTotalSecond();
        float currentSecond = total > 0 ? total : current.getCurrentSecond();

        // Tạo completed version
        LessonProgress completed = new LessonProgress(
                "progress_" + lessonId,
                lessonId,
                current.getCourseId(),
                currentSecond,
                total,
                100,
                true,
                System.currentTimeMillis()
        );

        progressCache.put(lessonId, completed);

        // Notify listeners về lesson này đã completed
        notifyLessonProgressChanged(lessonId);

        // TODO Backend thật:
        // POST /api/lesson-progress/{lessonId}/mark-completed
    }

    // ----------------- Listener registration -----------------

    @Override
    public synchronized void addLessonProgressUpdateListener(LessonProgressApi.LessonProgressUpdateListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public synchronized void removeLessonProgressUpdateListener(LessonProgressApi.LessonProgressUpdateListener listener) {
        listeners.remove(listener);
    }

    private synchronized void notifyLessonProgressChanged(String lessonId) {
        // copy to avoid concurrent modification while notifying
        List<LessonProgressApi.LessonProgressUpdateListener> copy = new ArrayList<>(listeners);
        for (LessonProgressApi.LessonProgressUpdateListener l : copy) {
            try {
                l.onLessonProgressChanged(lessonId);
            } catch (Exception ignored) {
                // ignore listener exceptions to avoid breaking others
            }
        }
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
