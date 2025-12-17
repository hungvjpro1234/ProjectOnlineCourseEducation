package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

public class CourseStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("status")
    private String status;

    @SerializedName("item")
    private CourseStatusItemDto item;

    public boolean isSuccess() { return success; }
    public String getStatus() { return status; }
    public CourseStatusItemDto getItem() { return item; }
}

