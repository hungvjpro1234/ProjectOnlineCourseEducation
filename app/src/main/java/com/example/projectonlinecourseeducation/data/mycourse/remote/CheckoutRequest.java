package com.example.projectonlinecourseeducation.data.mycourse.remote;

import java.util.List;

public class CheckoutRequest {

    private String userId;
    private List<Integer> courseIds;

    public CheckoutRequest(String userId, List<Integer> courseIds) {
        this.userId = userId;
        this.courseIds = courseIds;
    }

    public String getUserId() {
        return userId;
    }

    public List<Integer> getCourseIds() {
        return courseIds;
    }
}
