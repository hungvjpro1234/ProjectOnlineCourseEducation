package com.example.projectonlinecourseeducation.core.apiservice;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import retrofit2.Response;

/**
 * Remote implementation of LessonProgressApi
 *
 * - All calls are synchronous
 * - MUST be wrapped by AsyncApiHelper at UI layer
 * - Listener methods are NO-OP (backend không có realtime push)
 */
public class LessonProgressService implements LessonProgressApi {

    private final LessonProgressRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(LessonProgressRetrofitService.class);

    // ===================== Mapping =====================

    private LessonProgress mapDto(LessonProgressDto d) {
        if (d == null) return null;

        return new LessonProgress(
                d.id,
                d.lessonId,
                d.courseId,
                d.studentId,
                d.currentSecond,
                d.totalSecond,
                d.completionPercentage,
                d.isCompleted,
                d.updatedAt
        );
    }

    // ===================== API =====================

    @Override
    public LessonProgress getLessonProgress(String lessonId) {
        // legacy: call without studentId
        return getLessonProgress(lessonId, null);
    }

    @Override
    public LessonProgress getLessonProgress(String lessonId, String studentId) {
        try {
            Response<LessonProgressResponse> res =
                    api.getLessonProgress(lessonId, studentId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void updateLessonProgress(String lessonId,
                                     float currentSecond,
                                     float totalSecond) {
        updateLessonProgress(lessonId, currentSecond, totalSecond, null);
    }

    @Override
    public void updateLessonProgress(String lessonId,
                                     float currentSecond,
                                     float totalSecond,
                                     String studentId) {
        try {
            UpdateLessonProgressRequest req = new UpdateLessonProgressRequest();
            req.lessonId = lessonId;
            req.studentId = studentId;
            req.currentSecond = currentSecond;
            req.totalSecond = totalSecond;

            api.updateLessonProgress(req).execute();
        } catch (Exception ignored) {}
    }

    @Override
    public void markLessonAsCompleted(String lessonId) {
        markLessonAsCompleted(lessonId, null);
    }

    @Override
    public void markLessonAsCompleted(String lessonId, String studentId) {
        try {
            MarkCompletedRequest req = new MarkCompletedRequest();
            req.lessonId = lessonId;
            req.studentId = studentId;

            api.markLessonAsCompleted(req).execute();
        } catch (Exception ignored) {}
    }

    // ===================== Listener (NO-OP) =====================

    @Override
    public void addLessonProgressUpdateListener(LessonProgressUpdateListener listener) {
        // NO-OP
    }

    @Override
    public void removeLessonProgressUpdateListener(LessonProgressUpdateListener listener) {
        // NO-OP
    }
}
