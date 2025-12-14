package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /cart/add
 * Backend expects: { userId, courseId, price_snapshot?, course_name? }
 */
public class AddToCartRequest {

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("courseId")
    private Integer courseId;

    @SerializedName("price_snapshot")
    private Double priceSnapshot;

    @SerializedName("course_name")
    private String courseName;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Integer userId, Integer courseId, Double priceSnapshot, String courseName) {
        this.userId = userId;
        this.courseId = courseId;
        this.priceSnapshot = priceSnapshot;
        this.courseName = courseName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Double getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(Double priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
