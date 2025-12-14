package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for course status from backend
 * GET /course/:userId/:courseId/status returns: { success, data: { status: "..." } }
 */
public class CourseStatusDto {

    @SerializedName("status")
    private String status; // "NOT_PURCHASED" | "IN_CART" | "PURCHASED"

    public CourseStatusDto() {
    }

    public CourseStatusDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
