// app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/activity/LoginActivity.java
package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.content.Intent; // <-- thêm
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.User;
import com.example.projectonlinecourseeducation.core.utils.Utils;
import com.example.projectonlinecourseeducation.data.FakeApiService;
// import 3 màn hình mới
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

            FakeApiService.ApiResult<User> result =
                    FakeApiService.getInstance().loginByUsername(username, password);

            if (result.success) {
                Toast.makeText(this,
                        "Đăng nhập thành công. Xin chào " + result.data.getName()
                                + " (" + result.data.getRole().name() + ")",
                        Toast.LENGTH_SHORT).show();

                // Điều hướng theo role
                switch (result.data.getRole()) {
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
                finish(); // đóng màn login để không back về
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
