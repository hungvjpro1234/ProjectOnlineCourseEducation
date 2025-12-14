package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /cart/remove
 * Backend expects: { userId, courseId }
 */
public class RemoveFromCartRequest {

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("courseId")
    private Integer courseId;

    public RemoveFromCartRequest() {
    }

    public RemoveFromCartRequest(Integer userId, Integer courseId) {
        this.userId = userId;
        this.courseId = courseId;
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
}
