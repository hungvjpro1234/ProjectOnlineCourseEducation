package com.example.projectonlinecourseeducation.data.course.remote;

import android.util.Log;

import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.data.course.CourseStudentApi;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Remote implementation của CourseStudentApi dùng Retrofit
 *
 * ⚠️ MUST be wrapped with AsyncApiHelper when used in UI
 */
public class CourseStudentRemoteApiService implements CourseStudentApi {

    private static final String TAG = "CourseStudentRemote";

    private final CourseStudentRetrofitService retrofitService;
    private final List<StudentUpdateListener> listeners = new ArrayList<>();

    public CourseStudentRemoteApiService() {
        this.retrofitService = RetrofitClient.getCourseStudentService();
    }

    @Override
    public List<CourseStudent> getStudentsForCourse(String courseId) {
        if (courseId == null) return new ArrayList<>();

        try {
            Response<CourseStudentApiResponse<List<CourseStudentDto>>> response =
                    retrofitService.getStudentsForCourse(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseStudentApiResponse<List<CourseStudentDto>> apiResponse =
                        response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return mapDtoList(apiResponse.getData());
                } else {
                    Log.w(TAG, "getStudentsForCourse failed: " + apiResponse.getMessage());
                }
            } else {
                Log.e(TAG, "HTTP error: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getStudentsForCourse", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error getStudentsForCourse", e);
        }

        return new ArrayList<>();
    }

    // ================= LISTENER =================

    @Override
    public void addStudentUpdateListener(StudentUpdateListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeStudentUpdateListener(StudentUpdateListener l) {
        listeners.remove(l);
    }

    /**
     * Call khi course có purchase mới (từ CourseRemoteApiService.recordPurchase)
     */
    public void notifyStudentsChanged(String courseId) {
        for (StudentUpdateListener l : new ArrayList<>(listeners)) {
            try {
                l.onStudentsChanged(courseId);
            } catch (Exception ignored) {}
        }
    }

    // ================= MAPPING =================

    private List<CourseStudent> mapDtoList(List<CourseStudentDto> dtos) {
        List<CourseStudent> list = new ArrayList<>();
        for (CourseStudentDto dto : dtos) {
            list.add(mapDto(dto));
        }
        return list;
    }

    private CourseStudent mapDto(CourseStudentDto dto) {
        long enrolledAtMillis = 0L;

        try {
            if (dto.getEnrolledAt() != null) {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                java.util.Locale.US
                        );
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                enrolledAtMillis = sdf.parse(dto.getEnrolledAt()).getTime();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse enrolled_at: " + dto.getEnrolledAt(), e);
        }

        return new CourseStudent(
                dto.getUserId(),
                dto.getName(),
                dto.getEmail(),
                enrolledAtMillis
        );
    }
}
