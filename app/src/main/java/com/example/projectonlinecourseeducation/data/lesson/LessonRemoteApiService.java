package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.data.lesson.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Remote implementation of LessonApi
 * - All calls are synchronous
 * - MUST be wrapped by AsyncApiHelper at UI layer
 * - Fake-only features are safely ignored
 */
public class LessonRemoteApiService implements LessonApi {

    // ✅ FIX 1: Lấy Retrofit ĐÚNG CÁCH (giống AuthRemoteApiService)
    private final LessonRetrofitService api =
            RetrofitClient.getInstance().getRetrofit()
                    .create(LessonRetrofitService.class);

    // =====================================================
    // Mapping
    // =====================================================

    private Lesson mapDto(LessonDto d) {
        if (d == null) return null;

        Lesson l = new Lesson(
                d.id,
                d.courseId,
                d.title,
                d.description,
                d.videoUrl,
                d.duration,
                d.order
        );
        l.setInitialApproved(d.isInitialApproved);
        l.setEditApproved(d.isEditApproved);
        l.setDeleteRequested(d.isDeleteRequested);
        return l;
    }

    private List<Lesson> mapList(List<LessonDto> dtos) {
        List<Lesson> list = new ArrayList<>();
        if (dtos == null) return list;
        for (LessonDto d : dtos) list.add(mapDto(d));
        return list;
    }

    // =====================================================
    // Query
    // =====================================================

    @Override
    public List<Lesson> getLessonsForCourse(String courseId) {
        try {
            Response<LessonListResponse> res =
                    api.getLessonsForCourse(courseId).execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapList(res.body().data)
                    : new ArrayList<>();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Lesson getLessonDetail(String lessonId) {
        try {
            Response<LessonDetailResponse> res =
                    api.getLessonDetail(lessonId).execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    // =====================================================
    // Create / Update / Delete
    // =====================================================

    @Override
    public Lesson createLesson(Lesson newLesson) {
        try {
            CreateLessonRequest req = new CreateLessonRequest();
            req.courseId = newLesson.getCourseId();
            req.title = newLesson.getTitle();
            req.description = newLesson.getDescription();
            req.videoUrl = newLesson.getVideoUrl();
            req.order = newLesson.getOrder();

            Response<LessonDetailResponse> res =
                    api.createLesson(req).execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Lesson updateLesson(String lessonId, Lesson updatedLesson) {
        try {
            UpdateLessonRequest req = new UpdateLessonRequest();
            req.title = updatedLesson.getTitle();
            req.description = updatedLesson.getDescription();
            req.videoUrl = updatedLesson.getVideoUrl();
            req.order = updatedLesson.getOrder();

            Response<LessonDetailResponse> res =
                    api.updateLesson(lessonId, req).execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean deleteLesson(String lessonId) {
        try {
            Response<LessonDetailResponse> res =
                    api.deleteLesson(lessonId).execute();

            return res.isSuccessful() && res.body() != null && res.body().success;

        } catch (Exception e) {
            return false;
        }
    }

    // =====================================================
    // Approval workflow
    // =====================================================

    @Override
    public List<Lesson> getPendingLessons() {
        try {
            Response<LessonListResponse> res =
                    api.getPendingLessons().execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapList(res.body().data)
                    : new ArrayList<>();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Lesson> getPendingLessonsForCourse(String courseId) {
        try {
            Response<LessonListResponse> res =
                    api.getPendingLessonsForCourse(courseId).execute();

            return (res.isSuccessful() && res.body() != null && res.body().success)
                    ? mapList(res.body().data)
                    : new ArrayList<>();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean approveInitialCreation(String lessonId) {
        try {
            return api.approveInitialCreation(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rejectInitialCreation(String lessonId) {
        try {
            return api.rejectInitialCreation(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean approveLessonEdit(String lessonId) {
        try {
            return api.approveLessonEdit(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rejectLessonEdit(String lessonId) {
        try {
            return api.rejectLessonEdit(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    // =====================================================
    // Fake-only features (Remote fallback)
    // =====================================================

    @Override
    public Lesson getPendingEdit(String lessonId) {
        // ❗ Backend không hỗ trợ xem pending version
        return null;
    }

    @Override
    public boolean hasPendingEdit(String lessonId) {
        return false;
    }

    @Override
    public boolean permanentlyDeleteLesson(String lessonId) {
        try {
            return api.permanentlyDeleteLesson(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean cancelDeleteRequest(String lessonId) {
        try {
            return api.cancelDeleteRequest(lessonId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void approveAllPendingLessonsForCourse(String courseId) {
        try {
            api.approveAllPendingLessonsForCourse(courseId).execute();
        } catch (Exception ignored) {}
    }

    @Override
    public void rejectAllPendingLessonsForCourse(String courseId) {
        try {
            api.rejectAllPendingLessonsForCourse(courseId).execute();
        } catch (Exception ignored) {}
    }

    // =====================================================
    // Listener (NO-OP)
    // =====================================================

    @Override
    public void addLessonUpdateListener(LessonUpdateListener l) {
        // NO-OP
    }

    @Override
    public void removeLessonUpdateListener(LessonUpdateListener l) {
        // NO-OP
    }
}
