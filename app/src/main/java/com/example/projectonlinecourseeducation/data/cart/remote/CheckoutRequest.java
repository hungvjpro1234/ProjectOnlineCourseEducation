package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body for POST /cart/checkout
 * Backend expects: { userId, courseIds: [1, 2, 3] }
 */
public class CheckoutRequest {

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("courseIds")
    private List<Integer> courseIds;

    public CheckoutRequest() {
    }

    public CheckoutRequest(Integer userId, List<Integer> courseIds) {
        this.userId = userId;
        this.courseIds = courseIds;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<Integer> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Integer> courseIds) {
        this.courseIds = courseIds;
    }
}
