package com.example.projectonlinecourseeducation.data.auth.remote;

import com.example.projectonlinecourseeducation.data.auth.ApiResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit service interface cho Auth API endpoints
 * Base URL: http://127.0.0.1:3000 (hoặc configure trong RetrofitClient)
 */
public interface AuthRetrofitService {

    /**
     * POST /login
     * Request: { username, password }
     * Response: { success, message, data: UserDto, token }
     */
    @POST("login")
    Call<AuthApiResponse<UserDto>> login(@Body LoginRequest request);

    /**
     * POST /signup
     * Request: { name, username, email, password, role }
     * Response: { success, message, data: UserDto }
     */
    @POST("signup")
    Call<AuthApiResponse<UserDto>> register(@Body RegisterRequest request);

    /**
     * POST /forgot-password-request
     * Request: { email }
     * Response: { success, message, data: string (reset link) }
     */
    @POST("forgot-password-request")
    Call<AuthApiResponse<String>> requestPasswordReset(@Body ForgotPasswordRequest request);

    /**
     * POST /forgot-password-update
     * Request: { token, newPassword }
     * Response: { success, message, data: boolean }
     */
    @POST("forgot-password-update")
    Call<AuthApiResponse<Boolean>> resetPassword(@Body ResetPasswordRequest request);

    /**
     * GET /admin/users?role=STUDENT|TEACHER|ADMIN
     */
    @GET("admin/users")
    Call<AuthApiResponse<List<UserDto>>> getUsersByRole(
            @Query("role") String role
    );
}