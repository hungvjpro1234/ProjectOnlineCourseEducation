package com.example.projectonlinecourseeducation.data.lesson.remote;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface LessonRetrofitService {

    // ===== QUERY =====

    @GET("lesson/course/{courseId}")
    Call<LessonListResponse> getLessonsForCourse(@Path("courseId") String courseId);

    @GET("lesson/{lessonId}")
    Call<LessonDetailResponse> getLessonDetail(@Path("lessonId") String lessonId);

    @GET("lesson/pending")
    Call<LessonListResponse> getPendingLessons();

    @GET("lesson/pending/{courseId}")
    Call<LessonListResponse> getPendingLessonsForCourse(
            @Path("courseId") String courseId
    );

    // ===== CREATE / UPDATE / DELETE =====

    @POST("lesson")
    Call<LessonDetailResponse> createLesson(@Body CreateLessonRequest request);

    @PUT("lesson/{lessonId}")
    Call<LessonDetailResponse> updateLesson(
            @Path("lessonId") String lessonId,
            @Body UpdateLessonRequest request
    );

    @DELETE("lesson/{lessonId}")
    Call<LessonDetailResponse> deleteLesson(@Path("lessonId") String lessonId);

    // ===== APPROVAL =====

    @POST("lesson/{lessonId}/approve-initial")
    Call<LessonDetailResponse> approveInitialCreation(
            @Path("lessonId") String lessonId
    );

    @POST("lesson/{lessonId}/reject-initial")
    Call<LessonDetailResponse> rejectInitialCreation(
            @Path("lessonId") String lessonId
    );

    @POST("lesson/{lessonId}/approve-edit")
    Call<LessonDetailResponse> approveLessonEdit(
            @Path("lessonId") String lessonId
    );

    @POST("lesson/{lessonId}/reject-edit")
    Call<LessonDetailResponse> rejectLessonEdit(
            @Path("lessonId") String lessonId
    );

    @POST("lesson/{lessonId}/permanent-delete")
    Call<LessonDetailResponse> permanentlyDeleteLesson(
            @Path("lessonId") String lessonId
    );

    @POST("lesson/{lessonId}/cancel-delete")
    Call<LessonDetailResponse> cancelDeleteRequest(
            @Path("lessonId") String lessonId
    );

    // ===== COURSE LEVEL =====

    @POST("lesson/course/{courseId}/approve-all")
    Call<Void> approveAllPendingLessonsForCourse(
            @Path("courseId") String courseId
    );

    @POST("lesson/course/{courseId}/reject-all")
    Call<Void> rejectAllPendingLessonsForCourse(
            @Path("courseId") String courseId
    );
}
