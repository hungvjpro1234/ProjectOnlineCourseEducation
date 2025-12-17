package com.example.projectonlinecourseeducation.data.lessoncomment.remote;

import retrofit2.Call;
import retrofit2.http.*;

public interface LessonCommentRetrofitService {

    // ===== QUERY =====

    @GET("lesson-comment/{lessonId}")
    Call<LessonCommentListResponse> getCommentsForLesson(
            @Path("lessonId") String lessonId
    );

    @GET("lesson-comment/{lessonId}/count")
    Call<Integer> getCommentCount(
            @Path("lessonId") String lessonId
    );

    // ===== COMMENT =====

    @POST("lesson-comment")
    Call<LessonCommentDetailResponse> addComment(
            @Body AddCommentRequest request
    );

    @DELETE("lesson-comment/{commentId}")
    Call<Void> deleteComment(
            @Path("commentId") String commentId
    );

    // ===== TEACHER ACTION =====

    @POST("lesson-comment/{commentId}/reply")
    Call<LessonCommentDetailResponse> addReply(
            @Path("commentId") String commentId,
            @Body AddReplyRequest request
    );

    @DELETE("lesson-comment/{commentId}/reply")
    Call<LessonCommentDetailResponse> deleteReply(
            @Path("commentId") String commentId
    );

    @POST("lesson-comment/{commentId}/mark-deleted")
    Call<LessonCommentDetailResponse> markCommentAsDeleted(
            @Path("commentId") String commentId
    );
}
