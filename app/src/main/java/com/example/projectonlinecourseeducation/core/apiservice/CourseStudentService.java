package com.example.projectonlinecourseeducation.core.apiservice;

import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.data.course.CourseStudentApi;
import com.example.projectonlinecourseeducation.data.course.remote.CourseStudentDto;
import com.example.projectonlinecourseeducation.data.course.remote.CourseStudentRetrofitService;
import com.example.projectonlinecourseeducation.data.course.remote.CourseStudentsResponse;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class CourseStudentService implements CourseStudentApi {

    private final CourseStudentRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(CourseStudentRetrofitService.class);

    private CourseStudent map(CourseStudentDto d) {
        if (d == null) return null;

        long enrolledAtTs = 0L;
        try {
            enrolledAtTs = Long.parseLong(d.enrolledAt);
        } catch (Exception ignored) {}

        return new CourseStudent(
                d.studentId,
                d.studentName,
                d.avatarUrl,
                enrolledAtTs
        );
    }


    @Override
    public List<CourseStudent> getStudentsForCourse(String courseId) {
        if (courseId == null) return new ArrayList<>();
        try {
            Response<CourseStudentsResponse> res =
                    api.getStudentsForCourse(courseId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                List<CourseStudent> result = new ArrayList<>();
                for (CourseStudentDto d : res.body().data) {
                    CourseStudent s = map(d);
                    if (s != null) result.add(s);
                }
                return result;
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    // ===== Listener: NO-OP (Remote không realtime) =====

    @Override
    public void addStudentUpdateListener(StudentUpdateListener l) {
        // NO-OP
    }

    @Override
    public void removeStudentUpdateListener(StudentUpdateListener l) {
        // NO-OP
    }
}
