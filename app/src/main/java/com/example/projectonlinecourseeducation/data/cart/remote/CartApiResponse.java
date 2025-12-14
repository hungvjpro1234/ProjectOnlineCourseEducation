package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Generic response wrapper from Cart API endpoints
 * Matches backend response format: { success, message?, data }
 */
public class CartApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public CartApiResponse() {
    }

    public CartApiResponse(boolean success, String message, T data) {
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
