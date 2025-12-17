package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

public class CourseStatusItemDto {

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("course_id")
    private Integer courseId;

    @SerializedName("status")
    private String status;

    @SerializedName("price_snapshot")
    private Double priceSnapshot;

    @SerializedName("course_name")
    private String courseName;

    // getters
}
