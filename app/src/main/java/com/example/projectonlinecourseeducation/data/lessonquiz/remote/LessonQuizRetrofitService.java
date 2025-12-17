package com.example.projectonlinecourseeducation.data.lessonquiz.remote;

import retrofit2.Call;
import retrofit2.http.*;

public interface LessonQuizRetrofitService {

    // ===== QUIZ =====

    @GET("lesson-quiz/{lessonId}")
    Call<QuizResponse> getQuizForLesson(
            @Path("lessonId") String lessonId
    );

    @POST("lesson-quiz")
    Call<QuizResponse> createQuiz(
            @Body QuizDto quiz
    );

    @PUT("lesson-quiz/{quizId}")
    Call<QuizResponse> updateQuiz(
            @Path("quizId") String quizId,
            @Body QuizDto quiz
    );

    @DELETE("lesson-quiz/{quizId}")
    Call<Void> deleteQuiz(
            @Path("quizId") String quizId
    );

    // ===== ATTEMPT =====

    @POST("lesson-quiz/attempt")
    Call<QuizAttemptResponse> submitQuizAttempt(
            @Body SubmitQuizAttemptRequest request
    );

    @GET("lesson-quiz/attempts")
    Call<QuizAttemptResponse> getAttemptsForLesson(
            @Query("lessonId") String lessonId,
            @Query("studentId") String studentId
    );

    @GET("lesson-quiz/attempt/{attemptId}")
    Call<QuizAttemptResponse> getAttemptById(
            @Path("attemptId") String attemptId
    );
}
