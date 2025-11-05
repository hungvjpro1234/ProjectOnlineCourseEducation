// app/src/main/java/com/example/projectonlinecourseeducation/FakeApiService.java
package com.example.projectonlinecourseeducation.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.projectonlinecourseeducation.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakeApiService {

    public static class ApiResult<T> {
        public final boolean success;
        public final String message;
        public final T data;
        public ApiResult(boolean success, String message, T data) {
            this.success = success; this.message = message; this.data = data;
        }
        public static <T> ApiResult<T> ok(String msg, T data) { return new ApiResult<>(true, msg, data); }
        public static <T> ApiResult<T> fail(String msg) { return new ApiResult<>(false, msg, null); }
    }

    private static FakeApiService instance;
    public static FakeApiService getInstance() {
        if (instance == null) instance = new FakeApiService();
        return instance;
    }

    private final List<User> users = new ArrayList<>();

    // JSON seed mô phỏng data từ backend
    // Có thể chuyển sang đọc từ assets/users.json nếu muốn.
    private static final String SEED_JSON = "[\n" +
            "  {\n" +
            "    \"id\": \"u1\",\n" +
            "    \"name\": \"Student One\",\n" +
            "    \"email\": \"student1@example.com\",\n" +
            "    \"password\": \"Pass123\",\n" +
            "    \"verified\": true\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"u2\",\n" +
            "    \"name\": \"Teacher Demo\",\n" +
            "    \"email\": \"teacher@example.com\",\n" +
            "    \"password\": \"Teach123\",\n" +
            "    \"verified\": true\n" +
            "  }\n" +
            "]";

    private FakeApiService() {
        try {
            JSONArray arr = new JSONArray(SEED_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                users.add(new User(
                        o.optString("id"),
                        o.optString("name"),
                        o.optString("email"),
                        o.optString("password"),
                        o.optBoolean("verified", false),
                        null
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Đăng nhập
    public ApiResult<User> login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                if (!u.isVerified()) {
                    return ApiResult.fail("Tài khoản chưa xác minh.");
                }
                if (u.getPassword().equals(password)) {
                    return ApiResult.ok("Đăng nhập thành công", u);
                } else {
                    return ApiResult.fail("Sai mật khẩu.");
                }
            }
        }
        return ApiResult.fail("Không tìm thấy tài khoản với email này.");
    }

    // Đăng ký
    public ApiResult<User> register(String name, String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return ApiResult.fail("Email đã tồn tại.");
            }
        }
        User nu = new User(UUID.randomUUID().toString(), name, email, password, true, null);
        users.add(nu);
        return ApiResult.ok("Đăng ký thành công. Bạn có thể đăng nhập.", nu);
    }

    // Gửi mã reset (mock gửi mail): trả về mã để dev dễ test
    public ApiResult<String> requestPasswordReset(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                String code = String.valueOf((int)(Math.random() * 900000 + 100000)); // 6 số
                u.setResetCode(code);
                return ApiResult.ok("Mã đặt lại đã được gửi (demo).", code);
            }
        }
        return ApiResult.fail("Email không tồn tại trong hệ thống.");
    }

    // Đặt lại mật khẩu
    public ApiResult<Boolean> resetPassword(String email, String code, String newPassword) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                String expected = u.getResetCode();
                if (expected == null) return ApiResult.fail("Vui lòng yêu cầu mã trước.");
                if (!expected.equals(code)) return ApiResult.fail("Mã xác nhận không đúng.");
                u.setPassword(newPassword);
                u.setResetCode(null);
                return ApiResult.ok("Đặt lại mật khẩu thành công.", true);
            }
        }
        return ApiResult.fail("Email không tồn tại.");
    }
}
