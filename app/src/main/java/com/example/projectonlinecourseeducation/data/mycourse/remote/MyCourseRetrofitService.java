package com.example.projectonlinecourseeducation.data.mycourse.remote;

import com.example.projectonlinecourseeducation.data.mycourse.remote.AddMyCourseRequest;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCoursesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyCourseRetrofitService {

    // Get my courses (current user)
    @GET("/api/my-courses")
    Call<MyCoursesResponse> getMyCourses();

    // Add purchased course
    @POST("/api/my-courses")
    Call<MyCoursesResponse> addPurchasedCourse(@Body AddMyCourseRequest request);

    // ADMIN – get my courses of specific user
    @GET("/api/admin/my-courses/{userId}")
    Call<MyCoursesResponse> getMyCoursesForUser(@Path("userId") String userId);
}
