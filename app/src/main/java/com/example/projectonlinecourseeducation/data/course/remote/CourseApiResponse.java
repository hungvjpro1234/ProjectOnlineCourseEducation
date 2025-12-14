package com.example.projectonlinecourseeducation.data.course.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Generic response wrapper cho Course API responses từ backend
 * Backend format: { success: boolean, message: string, data: T }
 *
 * NOTE: Giống AuthApiResponse nhưng tách riêng cho Course module
 */
public class CourseApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public CourseApiResponse() {}

    public CourseApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
