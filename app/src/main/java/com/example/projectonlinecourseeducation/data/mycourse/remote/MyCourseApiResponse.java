package com.example.projectonlinecourseeducation.data.mycourse.remote;

import java.util.List;

public class MyCourseApiResponse {

    private boolean success;
    private String message;
    private List<CourseDto> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<CourseDto> getData() {
        return data;
    }
}
