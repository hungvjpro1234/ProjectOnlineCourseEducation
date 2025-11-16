package com.example.projectonlinecourseeducation.data.auth;

public class ApiResult<T> {

    private final boolean success;
    private final String message;
    private final T data;

    public ApiResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok(String msg, T data) {
        return new ApiResult<>(true, msg, data);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(false, msg, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
