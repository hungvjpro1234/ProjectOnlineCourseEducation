package com.example.projectonlinecourseeducation.data.course.remote;

import com.google.gson.annotations.SerializedName;

/**
 * DTO cho học viên của course từ backend
 */
public class CourseStudentDto {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("enrolled_at")
    private String enrolledAt;

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }
}
