package com.example.projectonlinecourseeducation.data.mycourse.remote;

public class MyCourseStatusResponse {

    private boolean success;
    private boolean purchased;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public String getMessage() {
        return message;
    }
}
