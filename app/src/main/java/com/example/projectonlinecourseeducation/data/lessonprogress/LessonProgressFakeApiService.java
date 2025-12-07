package com.example.projectonlinecourseeducation.data.lessonprogress;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake API Service cho LessonProgress — now per-student.
 *
 * Quy ước key lưu: key = lessonId + "|" + (studentId == null ? "_GLOBAL_" : studentId)
 *
 * Behavior:
 * - getLessonProgress(lessonId, studentId) trả progress của student nếu có.
 * - getLessonProgress(lessonId) (legacy) trả progress global (nếu có per-student tồn tại, trả bản đầu tiên)
 *
 * NOTE:
 * - Listener notification vẫn chỉ báo `lessonId` (không báo studentId) để giữ interface đơn giản.
 */
public class LessonProgressFakeApiService implements LessonProgressApi {

    private static LessonProgressFakeApiService instance;
    public static LessonProgressFakeApiService getInstance() {
        if (instance == null) instance = new LessonProgressFakeApiService();
        return instance;
    }

    // key -> LessonProgress
    private final Map<String, LessonProgress> progressCache = new HashMap<>();

    // Registered listeners
    private final List<LessonProgressApi.LessonProgressUpdateListener> listeners = new ArrayList<>();

    private LessonProgressFakeApiService() {
        // no-op
    }

    // Helper to build key
    private String buildKey(String lessonId, String studentId) {
        if (lessonId == null) lessonId = "";
        if (studentId == null || studentId.trim().isEmpty()) {
            return lessonId + "|_GLOBAL_";
        }
        return lessonId + "|" + studentId;
    }

    @Override
    public synchronized LessonProgress getLessonProgress(String lessonId) {
        if (lessonId == null) return null;
        // try global key first
        String globalKey = buildKey(lessonId, null);
        if (progressCache.containsKey(globalKey)) {
            return progressCache.get(globalKey);
        }
        // otherwise return any per-student entry for that lesson (first found) to keep legacy behavior
        for (LessonProgress lp : progressCache.values()) {
            if (lp != null && lessonId.equals(lp.getLessonId())) {
                return lp;
            }
        }
        // create default 0 progress (global)
        LessonProgress defaultProgress = new LessonProgress(
                "progress_" + lessonId,
                lessonId,
                extractCourseId(lessonId),
                null,
                0f,
                0f,
                0,
                false,
                System.currentTimeMillis()
        );
        progressCache.put(globalKey, defaultProgress);
        return defaultProgress;
    }

    @Override
    public synchronized LessonProgress getLessonProgress(String lessonId, String studentId) {
        if (lessonId == null) return null;
        String key = buildKey(lessonId, studentId);
        if (!progressCache.containsKey(key)) {
            // if not exists, create default
            LessonProgress defaultProgress = new LessonProgress(
                    "progress_" + lessonId + (studentId != null ? ("_" + studentId) : ""),
                    lessonId,
                    extractCourseId(lessonId),
                    studentId,
                    0f,
                    0f,
                    0,
                    false,
                    System.currentTimeMillis()
            );
            progressCache.put(key, defaultProgress);
        }
        return progressCache.get(key);
    }

    @Override
    public synchronized void updateLessonProgress(String lessonId, float currentSecond, float totalSecond) {
        // legacy: update global key
        updateLessonProgress(lessonId, currentSecond, totalSecond, null);
    }

    @Override
    public synchronized void updateLessonProgress(String lessonId, float currentSecond, float totalSecond, String studentId) {
        if (lessonId == null) return;
        String key = buildKey(lessonId, studentId);
        LessonProgress existing = progressCache.get(key);

        float bestCurrentSecond = currentSecond;
        float bestTotalSecond = totalSecond;

        if (existing != null) {
            // keep totalSecond max
            if (bestTotalSecond <= 0 && existing.getTotalSecond() > 0) {
                bestTotalSecond = existing.getTotalSecond();
            }
            // keep best current second
            bestCurrentSecond = Math.max(existing.getCurrentSecond(), currentSecond);
        }

        int completionPercentage = 0;
        if (bestTotalSecond > 0) {
            completionPercentage = (int) ((bestCurrentSecond / bestTotalSecond) * 100);
            completionPercentage = Math.min(100, Math.max(0, completionPercentage));
        }

        boolean wasCompleted = existing != null && existing.isCompleted();
        boolean isCompleted = wasCompleted || completionPercentage >= 90;

        LessonProgress updated = new LessonProgress(
                "progress_" + lessonId + (studentId != null ? ("_" + studentId) : ""),
                lessonId,
                extractCourseId(lessonId),
                studentId,
                bestCurrentSecond,
                bestTotalSecond,
                completionPercentage,
                isCompleted,
                System.currentTimeMillis()
        );

        progressCache.put(key, updated);

        notifyLessonProgressChanged(lessonId);
    }

    @Override
    public synchronized void markLessonAsCompleted(String lessonId) {
        markLessonAsCompleted(lessonId, null);
    }

    @Override
    public synchronized void markLessonAsCompleted(String lessonId, String studentId) {
        if (lessonId == null) return;
        String key = buildKey(lessonId, studentId);
        LessonProgress current = progressCache.get(key);
        if (current == null) current = getLessonProgress(lessonId, studentId);

        float total = current.getTotalSecond();
        float currentSecond = total > 0 ? total : current.getCurrentSecond();

        LessonProgress completed = new LessonProgress(
                "progress_" + lessonId + (studentId != null ? ("_" + studentId) : ""),
                lessonId,
                current.getCourseId(),
                studentId,
                currentSecond,
                total,
                100,
                true,
                System.currentTimeMillis()
        );

        progressCache.put(key, completed);
        notifyLessonProgressChanged(lessonId);
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
        List<LessonProgressApi.LessonProgressUpdateListener> copy = new ArrayList<>(listeners);
        for (LessonProgressApi.LessonProgressUpdateListener l : copy) {
            try {
                l.onLessonProgressChanged(lessonId);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Helper: extract courseId từ lessonId (format: c1_l1, c2_l3, etc.)
     */
    private String extractCourseId(String lessonId) {
        if (lessonId != null && lessonId.contains("_")) {
            return lessonId.split("_")[0];
        }
        return "";
    }
}
