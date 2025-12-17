package com.example.projectonlinecourseeducation.data.course.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CourseRetrofitService {

    @GET("courses")
    Call<CoursesResponse> getAllCourses();

    @GET("courses/{courseId}")
    Call<CourseDto> getCourseDetail(@Path("courseId") String courseId);
}
