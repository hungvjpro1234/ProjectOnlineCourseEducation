package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Generic response wrapper from Cart API endpoints
 * Matches backend response format: { success, message?, data }
 */
public class CartApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private T data;

    @SerializedName("added")
    private Boolean added;

    @SerializedName("removed")
    private Boolean removed;

    public boolean isSuccess() { return success; }
    public T getData() { return data; }

    public boolean isAdded() {
        return Boolean.TRUE.equals(added);
    }

    public boolean isRemoved() {
        return Boolean.TRUE.equals(removed);
    }
}
