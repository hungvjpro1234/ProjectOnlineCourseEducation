package com.example.projectonlinecourseeducation.data.auth.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /forgot-password-update
 */
public class ResetPasswordRequest {

    @SerializedName("token")
    private String token;

    @SerializedName("newPassword")
    private String newPassword;

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}