package com.example.projectonlinecourseeducation.data.course.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CourseStudentRetrofitService {

    @GET("courses/{courseId}/students")
    Call<CourseStudentsResponse> getStudentsForCourse(
            @Path("courseId") String courseId
    );
}
