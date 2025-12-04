package com.example.projectonlinecourseeducation.data.auth.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /forgot-password-request
 */
public class ForgotPasswordRequest {

    @SerializedName("email")
    private String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}