package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.Utils;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.ApiResult;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentHomeActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherHomeActivity;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminHomeActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnSubmit, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnSubmit.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (!Utils.isValidUsername(username)) {
                edtUsername.setError("Username không hợp lệ");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            AuthApi authApi = ApiProvider.getAuthApi();
            ApiResult<User> result = authApi.loginByUsername(username, password);

            if (result.isSuccess()) {
                User user = result.getData();

                // ✅ LƯU USER VÀO SESSION để dùng trong app (bình luận, profile, etc.)
                SessionManager.getInstance(this).saveSession("", user);

                Toast.makeText(
                        this,
                        "Đăng nhập thành công. Xin chào "
                                + user.getName() + " (" + user.getRole().name() + ")",
                        Toast.LENGTH_SHORT
                ).show();

                switch (user.getRole()) {
                    case STUDENT:
                        startActivity(new Intent(this, StudentHomeActivity.class));
                        break;
                    case TEACHER:
                        startActivity(new Intent(this, TeacherHomeActivity.class));
                        break;
                    case ADMIN:
                        startActivity(new Intent(this, AdminHomeActivity.class));
                        break;
                }
                finish();
            } else {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
