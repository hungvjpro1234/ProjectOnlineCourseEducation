package com.example.projectonlinecourseeducation.data.lessonprogress.remote;

import retrofit2.Call;
import retrofit2.http.*;

public interface LessonProgressRetrofitService {

    // ===== QUERY =====

    @GET("lesson-progress/{lessonId}")
    Call<LessonProgressResponse> getLessonProgress(
            @Path("lessonId") String lessonId,
            @Query("studentId") String studentId
    );

    // ===== UPDATE =====

    @POST("lesson-progress/update")
    Call<LessonProgressResponse> updateLessonProgress(
            @Body UpdateLessonProgressRequest request
    );

    @POST("lesson-progress/complete")
    Call<LessonProgressResponse> markLessonAsCompleted(
            @Body MarkCompletedRequest request
    );
}
