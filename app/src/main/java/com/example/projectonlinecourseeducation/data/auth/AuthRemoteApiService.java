package com.example.projectonlinecourseeducation.data.auth;

import android.util.Log;

import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.auth.remote.AuthApiResponse;
import com.example.projectonlinecourseeducation.data.auth.remote.AuthRetrofitService;
import com.example.projectonlinecourseeducation.data.auth.remote.ForgotPasswordRequest;
import com.example.projectonlinecourseeducation.data.auth.remote.LoginRequest;
import com.example.projectonlinecourseeducation.data.auth.remote.RegisterRequest;
import com.example.projectonlinecourseeducation.data.auth.remote.ResetPasswordRequest;
import com.example.projectonlinecourseeducation.data.auth.remote.UserDto;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
import com.example.projectonlinecourseeducation.data.network.SessionManager;

import java.io.IOException;
import java.util.Locale;

import retrofit2.Response;

/**
 * AuthRemoteApiService - Implementation of AuthApi using Retrofit for real backend calls
 *
 * Usage:
 * 1. Initialize RetrofitClient first: RetrofitClient.initialize(context);
 * 2. Swap in ApiProvider: ApiProvider.setAuthApi(new AuthRemoteApiService());
 * 3. Use normally: ApiProvider.getAuthApi().loginByUsername(...);
 */
public class AuthRemoteApiService implements AuthApi {

    private static final String TAG = "AuthRemoteApiService";

    private final AuthRetrofitService retrofitService;
    private final SessionManager sessionManager;

    public AuthRemoteApiService() {
        this.retrofitService = RetrofitClient.getAuthService();
        this.sessionManager = RetrofitClient.getSessionManager();
    }

    @Override
    public ApiResult<User> loginByUsername(String username, String password) {
        try {
            LoginRequest request = new LoginRequest(username, password);
            Response<AuthApiResponse<UserDto>> response = retrofitService.login(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthApiResponse<UserDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    // Convert UserDto to User model
                    User user = convertDtoToUser(apiResponse.getData());

                    // Save session (token + user)
                    String token = apiResponse.getToken();
                    if (token != null) {
                        sessionManager.saveSession(token, user);
                    }

                    return ApiResult.ok(apiResponse.getMessage(), user);
                } else {
                    return ApiResult.fail(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Lỗi đăng nhập: " + response.code();
                if (response.errorBody() != null) {
                    try {
                        errorMsg = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
                return ApiResult.fail(errorMsg);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during login", e);
            return ApiResult.fail("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during login", e);
            return ApiResult.fail("Lỗi không xác định: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<User> register(String name, String username, String email, String password, User.Role role) {
        try {
            RegisterRequest request = new RegisterRequest(
                name,
                username,
                email,
                password,
                role.name() // "STUDENT" or "TEACHER"
            );

            Response<AuthApiResponse<UserDto>> response = retrofitService.register(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthApiResponse<UserDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    User user = convertDtoToUser(apiResponse.getData());
                    return ApiResult.ok(apiResponse.getMessage(), user);
                } else {
                    return ApiResult.fail(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Lỗi đăng ký: " + response.code();
                if (response.errorBody() != null) {
                    try {
                        errorMsg = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
                return ApiResult.fail(errorMsg);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during register", e);
            return ApiResult.fail("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during register", e);
            return ApiResult.fail("Lỗi không xác định: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<String> requestPasswordResetLink(String email) {
        try {
            ForgotPasswordRequest request = new ForgotPasswordRequest(email);
            Response<AuthApiResponse<String>> response = retrofitService.requestPasswordReset(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthApiResponse<String> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    String resetLink = apiResponse.getData();
                    return ApiResult.ok(apiResponse.getMessage(), resetLink);
                } else {
                    return ApiResult.fail(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Lỗi yêu cầu đặt lại mật khẩu: " + response.code();
                if (response.errorBody() != null) {
                    try {
                        errorMsg = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
                return ApiResult.fail(errorMsg);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during password reset request", e);
            return ApiResult.fail("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during password reset request", e);
            return ApiResult.fail("Lỗi không xác định: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<Boolean> finalizeResetViaLink(String token, String newPassword) {
        try {
            ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
            Response<AuthApiResponse<Boolean>> response = retrofitService.resetPassword(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthApiResponse<Boolean> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Boolean result = apiResponse.getData();
                    return ApiResult.ok(apiResponse.getMessage(), result != null ? result : true);
                } else {
                    return ApiResult.fail(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Lỗi đặt lại mật khẩu: " + response.code();
                if (response.errorBody() != null) {
                    try {
                        errorMsg = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
                return ApiResult.fail(errorMsg);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during password reset", e);
            return ApiResult.fail("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during password reset", e);
            return ApiResult.fail("Lỗi không xác định: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) {
            // Logout - clear session
            sessionManager.clearSession();
        } else {
            // Update current user
            sessionManager.updateCurrentUser(user);
        }
    }

    @Override
    public ApiResult<User> updateCurrentUserProfile(String newName, String newEmail, String newUsername) {
        // TODO: Backend endpoint not implemented yet
        // For now, update locally only
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResult.fail("Không tìm thấy user đang đăng nhập.");
        }

        // Validate
        if (newName == null || newName.trim().isEmpty()) {
            return ApiResult.fail("Tên không được để trống.");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ApiResult.fail("Email không được để trống.");
        }
        if (!newEmail.contains("@")) {
            return ApiResult.fail("Email không hợp lệ.");
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ApiResult.fail("Username không được để trống.");
        }
        if (newUsername.trim().length() < 4) {
            return ApiResult.fail("Username phải có ít nhất 4 ký tự.");
        }

        // Update locally (in production, should call backend API)
        currentUser.setName(newName.trim());
        currentUser.setEmail(newEmail.trim());
        currentUser.setUsername(newUsername.trim());

        sessionManager.updateCurrentUser(currentUser);

        Log.w(TAG, "updateCurrentUserProfile: Backend endpoint not implemented, updating locally only");
        return ApiResult.ok("Cập nhật thông tin thành công (local).", currentUser);
    }

    @Override
    public ApiResult<Boolean> changeCurrentUserPassword(String oldPassword, String newPassword) {
        // TODO: Backend endpoint not implemented yet
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResult.fail("Không tìm thấy user đang đăng nhập.");
        }

        // Validate
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ApiResult.fail("Vui lòng nhập mật khẩu cũ.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ApiResult.fail("Mật khẩu mới không được để trống.");
        }
        if (newPassword.length() < 6) {
            return ApiResult.fail("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        // In production, should call backend API to verify old password and update
        // For now, just verify locally (if password is stored)
        if (currentUser.getPassword() != null && !currentUser.getPassword().equals(oldPassword)) {
            return ApiResult.fail("Mật khẩu cũ không chính xác.");
        }

        currentUser.setPassword(newPassword);
        sessionManager.updateCurrentUser(currentUser);

        Log.w(TAG, "changeCurrentUserPassword: Backend endpoint not implemented, updating locally only");
        return ApiResult.ok("Đổi mật khẩu thành công (local).", true);
    }

    /**
     * Convert UserDto (from backend) to User (app model)
     */
    private User convertDtoToUser(UserDto dto) {
        User.Role role;
        try {
            role = User.Role.valueOf(dto.getRole().toUpperCase(Locale.US));
        } catch (Exception e) {
            Log.e(TAG, "Invalid role: " + dto.getRole() + ", defaulting to STUDENT", e);
            role = User.Role.STUDENT;
        }

        return new User(
            dto.getId(),
            dto.getName(),
            dto.getUsername(),
            dto.getEmail(),
            dto.getPassword(), // Should be null from backend
            dto.isVerified(),
            null, // resetToken not needed on client
            role
        );
    }

    @Override
    public java.util.List<User> getAllUsersByRole(User.Role role) {
        if (role == null) return new java.util.ArrayList<>();

        try {
            Response<AuthApiResponse<java.util.List<UserDto>>> response =
                    retrofitService.getUsersByRole(role.name()).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthApiResponse<java.util.List<UserDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    java.util.List<User> result = new java.util.ArrayList<>();
                    for (UserDto dto : apiResponse.getData()) {
                        result.add(convertDtoToUser(dto));
                    }
                    return result;
                } else {
                    Log.w(TAG, "getAllUsersByRole failed: " + apiResponse.getMessage());
                }
            } else {
                Log.e(TAG, "getAllUsersByRole HTTP error: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getAllUsersByRole", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error getAllUsersByRole", e);
        }
        return new java.util.ArrayList<>();
    }
}
