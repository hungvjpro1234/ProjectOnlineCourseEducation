package com.example.projectonlinecourseeducation.data.auth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.projectonlinecourseeducation.core.model.User;
import com.example.projectonlinecourseeducation.core.model.User.Role;

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
            "    \"name\": \"Teacher Demo\",\n" +
            "    \"username\": \"teacher\",\n" +
            "    \"email\": \"teacher@example.com\",\n" +
            "    \"password\": \"Teach123\",\n" +
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
}
