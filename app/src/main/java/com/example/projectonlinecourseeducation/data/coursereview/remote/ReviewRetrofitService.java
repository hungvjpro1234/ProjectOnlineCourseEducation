package com.example.projectonlinecourseeducation.data.coursereview.remote;

import retrofit2.Call;
import retrofit2.http.*;

public interface ReviewRetrofitService {

    @GET("courses/{courseId}/reviews")
    Call<CourseReviewsResponse> getReviewsForCourse(
            @Path("courseId") String courseId
    );

    @POST("courses/{courseId}/reviews")
    Call<CourseReviewDto> addReview(
            @Path("courseId") String courseId,
            @Body AddReviewRequest request
    );

    // Optional
    @DELETE("courses/{courseId}/reviews/{reviewId}")
    Call<Void> deleteReview(
            @Path("courseId") String courseId,
            @Path("reviewId") String reviewId
    );
}
