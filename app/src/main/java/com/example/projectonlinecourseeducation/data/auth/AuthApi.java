package com.example.projectonlinecourseeducation.data.auth;

import com.example.projectonlinecourseeducation.core.model.User;
import com.example.projectonlinecourseeducation.core.model.User.Role;

public interface AuthApi {

    // Đăng nhập bằng username
    ApiResult<User> loginByUsername(String username, String password);

    // Đăng ký (sau này map với POST /auth/register)
    ApiResult<User> register(
            String name,
            String username,
            String email,
            String password,
            Role role
    );

    // Quên mật khẩu – xin link reset (POST /auth/forgot-password)
    ApiResult<String> requestPasswordResetLink(String email);

    // Mô phỏng bước đổi mật khẩu sau khi click link trong email
    ApiResult<Boolean> finalizeResetViaLink(String token, String newPassword);
}
