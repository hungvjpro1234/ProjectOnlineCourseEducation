// app/src/main/java/com/example/projectonlinecourseeducation/data/auth/AuthFakeApiService.java
package com.example.projectonlinecourseeducation.data.auth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.model.user.User.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AuthFakeApiService implements AuthApi {

    private static AuthFakeApiService instance;

    public static AuthFakeApiService getInstance() {
        if (instance == null) instance = new AuthFakeApiService();
        return instance;
    }

    private final List<User> users = new ArrayList<>();

    // User hiện đang đăng nhập (fake session local)
    private User currentUser;

    // Seed JSON: có username + role
    private static final String SEED_JSON = "[\n" +
            "  {\n" +
            "    \"id\": \"u1\",\n" +
            "    \"name\": \"Student One\",\n" +
            "    \"username\": \"student1\",\n" +
            "    \"email\": \"student1@example.com\",\n" +
            "    \"password\": \"Pass123\",\n" +
            "    \"verified\": true,\n" +
            "    \"role\": \"STUDENT\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"u2\",\n" +
            "    \"name\": \"Nguyễn A\",\n" +
            "    \"username\": \"teacher\",\n" +
            "    \"email\": \"teacher@example.com\",\n" +
            "    \"password\": \"teacher\",\n" +
            "    \"verified\": true,\n" +
            "    \"role\": \"TEACHER\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"u3\",\n" +
            "    \"name\": \"Admin Boss\",\n" +
            "    \"username\": \"admin\",\n" +
            "    \"email\": \"admin@example.com\",\n" +
            "    \"password\": \"Admin123\",\n" +
            "    \"verified\": true,\n" +
            "    \"role\": \"ADMIN\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"u4\",\n" +
            "    \"name\": \"Student Two\",\n" +
            "    \"username\": \"student2\",\n" +
            "    \"email\": \"student2@example.com\",\n" +
            "    \"password\": \"Pass456\",\n" +
            "    \"verified\": true,\n" +
            "    \"role\": \"STUDENT\"\n" +
            "  }\n" +
            "]";

    private AuthFakeApiService() {
        try {
            JSONArray arr = new JSONArray(SEED_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                users.add(new User(
                        o.optString("id"),
                        o.optString("name"),
                        o.optString("username"),
                        o.optString("email"),
                        o.optString("password"),
                        o.optBoolean("verified", false),
                        null,
                        Role.valueOf(o.optString("role", "STUDENT").toUpperCase(Locale.US))
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ========== IMPLEMENT AuthApi ==========

    @Override
    public ApiResult<User> loginByUsername(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (!u.isVerified()) return ApiResult.fail("Tài khoản chưa xác minh email.");
                if (u.getPassword().equals(password)) {

                    // 🔐 Lưu lại user hiện tại để chỗ khác (Home) có thể đọc được
                    currentUser = u;

                    return ApiResult.ok("Đăng nhập thành công", u);
                } else {
                    return ApiResult.fail("Sai mật khẩu.");
                }
            }
        }
        return ApiResult.fail("Không tìm thấy tài khoản với username này.");
    }

    @Override
    public ApiResult<User> register(String name,
                                    String username,
                                    String email,
                                    String password,
                                    Role role) {
        if (role == Role.ADMIN) {
            return ApiResult.fail("Không thể tự đăng ký ADMIN.");
        }

        // Unique email + username
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return ApiResult.fail("Email đã tồn tại.");
            }
            if (u.getUsername().equalsIgnoreCase(username)) {
                return ApiResult.fail("Username đã tồn tại.");
            }
        }

        // Demo: verified = true để bỏ qua bước xác minh email
        User nu = new User(
                UUID.randomUUID().toString(),
                name,
                username,
                email,
                password,
                true,
                null,
                role
        );
        users.add(nu);
        return ApiResult.ok("Đăng ký thành công. Bạn có thể đăng nhập.", nu);
    }

    @Override
    public ApiResult<String> requestPasswordResetLink(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                String token = UUID.randomUUID().toString();
                u.setResetToken(token);

                // Link demo để dev test (prod sẽ gửi mail thực)
                String fakeLink = "https://example.com/reset?token=" + token;
                return ApiResult.ok("Đã gửi link đặt lại mật khẩu (demo).", fakeLink);
            }
        }
        return ApiResult.fail("Email không tồn tại trong hệ thống.");
    }

    @Override
    public ApiResult<Boolean> finalizeResetViaLink(String token, String newPassword) {
        if (token == null || token.isEmpty()) return ApiResult.fail("Token không hợp lệ.");
        for (User u : users) {
            if (token.equals(u.getResetToken())) {
                u.setPassword(newPassword);
                u.setResetToken(null);
                return ApiResult.ok("Đổi mật khẩu thành công qua link.", true);
            }
        }
        return ApiResult.fail("Token không hợp lệ hoặc đã hết hạn.");
    }

    // ====== Session hiện tại ======

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // PUT /auth/profile (fake)
    @Override
    public ApiResult<User> updateCurrentUserProfile(String newName,
                                                    String newEmail,
                                                    String newUsername) {
        if (currentUser == null) {
            return ApiResult.fail("Không tìm thấy user đang đăng nhập.");
        }

        // Validate đơn giản
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

        // Check trùng email/username với user khác
        for (User u : users) {
            if (u == currentUser) continue; // bỏ qua chính nó
            if (u.getEmail().equalsIgnoreCase(newEmail)) {
                return ApiResult.fail("Email đã được sử dụng bởi tài khoản khác.");
            }
            if (u.getUsername().equalsIgnoreCase(newUsername)) {
                return ApiResult.fail("Username đã được sử dụng bởi tài khoản khác.");
            }
        }

        // Cập nhật vào currentUser (và list users vì cùng reference)
        currentUser.setName(newName.trim());
        currentUser.setEmail(newEmail.trim());
        currentUser.setUsername(newUsername.trim());

        return ApiResult.ok("Cập nhật thông tin thành công.", currentUser);
    }

    // POST /auth/change-password (fake)
    @Override
    public ApiResult<Boolean> changeCurrentUserPassword(String oldPassword,
                                                        String newPassword) {
        if (currentUser == null) {
            return ApiResult.fail("Không tìm thấy user đang đăng nhập.");
        }

        if (oldPassword == null || oldPassword.isEmpty()) {
            return ApiResult.fail("Vui lòng nhập mật khẩu cũ.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ApiResult.fail("Mật khẩu mới không được để trống.");
        }
        if (newPassword.length() < 6) {
            return ApiResult.fail("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        if (!currentUser.getPassword().equals(oldPassword)) {
            return ApiResult.fail("Mật khẩu cũ không chính xác.");
        }

        currentUser.setPassword(newPassword);
        return ApiResult.ok("Đổi mật khẩu thành công.", true);
    }
}