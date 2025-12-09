// app/src/main/java/com/example/projectonlinecourseeducation/data/auth/AuthApi.java
package com.example.projectonlinecourseeducation.data.auth;

import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.model.user.User.Role;

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

    // ====== Thêm cho session hiện tại (phục vụ hiển thị Xin chào, ...) ======

    /**
     * Lấy user hiện đang đăng nhập (fake session trên client).
     * Trả về null nếu chưa đăng nhập hoặc đã logout.
     */
    User getCurrentUser();

    /**
     * Ghi lại user hiện tại sau khi login, hoặc truyền null khi logout.
     */
    void setCurrentUser(User user);

    // ====== Thêm: cập nhật thông tin cơ bản của user hiện tại ======
    /**
     * Giống PUT /auth/profile (fake):
     * Cập nhật name, email, username cho user đang đăng nhập.
     * Validate và check trùng email/username như lúc register.
     */
    ApiResult<User> updateCurrentUserProfile(String newName,
                                             String newEmail,
                                             String newUsername);

    /**
     * Giống POST /auth/change-password (fake):
     * Đổi mật khẩu cho user hiện tại, yêu cầu nhập đúng mật khẩu cũ.
     */
    ApiResult<Boolean> changeCurrentUserPassword(String oldPassword,
                                                 String newPassword);

    // ====== ADMIN: Lấy danh sách users theo role ======
    /**
     * Lấy tất cả users theo role (dành cho admin)
     * @param role Role cần lọc (STUDENT, TEACHER, ADMIN)
     * @return Danh sách users có role tương ứng
     */
    java.util.List<User> getAllUsersByRole(Role role);
}
