package com.example.projectonlinecourseeducation.data.auth.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Generic response wrapper cho tất cả backend API responses
 * Backend format: { success: boolean, message: string, data: T, token?: string }
 */
public class AuthApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("token")
    private String token; // Optional, chỉ có trong login response

    public AuthApiResponse() {}

    public AuthApiResponse(boolean success, String message, T data) {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
