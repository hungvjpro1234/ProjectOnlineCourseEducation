// app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/activity/RegisterActivity.java
package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.model.user.User.Role;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.core.utils.Utils;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.ApiResult;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtUsername, edtEmail, edtPassword, edtPassword2;
    RadioGroup rgRole;
    RadioButton rbStudent, rbTeacher;
    Button btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);

        edtName      = findViewById(R.id.edtName);
        edtUsername  = findViewById(R.id.edtUsername);
        edtEmail     = findViewById(R.id.edtEmail);
        edtPassword  = findViewById(R.id.edtPassword);
        edtPassword2 = findViewById(R.id.edtPassword2);
        rgRole       = findViewById(R.id.rgRole);
        rbStudent    = findViewById(R.id.rbStudent);
        rbTeacher    = findViewById(R.id.rbTeacher);
        btnRegister  = findViewById(R.id.btnRegister);
        btnBack      = findViewById(R.id.btnBack);

        btnRegister.setOnClickListener(v -> {
            String name      = edtName.getText().toString().trim();
            String username  = edtUsername.getText().toString().trim();
            String email     = edtEmail.getText().toString().trim();
            String password  = edtPassword.getText().toString();
            String password2 = edtPassword2.getText().toString();

            // ====== Validate input (giữ y nguyên logic cũ) ======
            if (TextUtils.isEmpty(name)) {
                edtName.setError("Vui lòng nhập họ tên");
                return;
            }
            if (!Utils.isValidUsername(username)) {
                edtUsername.setError("Username 3-20 ký tự, chữ/số/._, bắt đầu bằng chữ");
                return;
            }
            if (!Utils.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }
            if (!Utils.isStrongPassword(password)) {
                edtPassword.setError("Mật khẩu tối thiểu 6 ký tự, gồm chữ & số");
                return;
            }
            if (!password.equals(password2)) {
                edtPassword2.setError("Mật khẩu không khớp");
                return;
            }

            Role role = rbTeacher.isChecked() ? Role.TEACHER : Role.STUDENT;

            // ====== Gọi API qua AuthApi với AsyncApiHelper (Fake/Remote đều dùng chung) ======
            AsyncApiHelper.execute(
                    // API call (runs on background thread)
                    () -> ApiProvider.getAuthApi().register(name, username, email, password, role),

                    // Callback (runs on main thread - can update UI)
                    new AsyncApiHelper.ApiCallback<ApiResult<User>>() {
                        @Override
                        public void onSuccess(ApiResult<User> result) {
                            Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                            if (result.isSuccess()) {
                                // Đăng ký xong quay lại màn Login
                                finish();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(RegisterActivity.this,
                                    "Lỗi kết nối: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
