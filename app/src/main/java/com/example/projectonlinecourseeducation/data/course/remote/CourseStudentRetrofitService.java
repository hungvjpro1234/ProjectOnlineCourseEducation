package com.example.projectonlinecourseeducation.data.course.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit service cho CourseStudent API
 */
public interface CourseStudentRetrofitService {

    /**
     * GET /course/:id/students
     * Teacher/Admin only
     *
     * Response: { success, data: List<CourseStudentDto> }
     */
    @GET("course/{id}/students")
    Call<CourseStudentApiResponse<List<CourseStudentDto>>> getStudentsForCourse(
            @Path("id") String courseId
    );
}
