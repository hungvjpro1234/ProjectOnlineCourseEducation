// app/src/main/java/com/example/projectonlinecourseeducation/ForgotPasswordActivity.java
package com.example.projectonlinecourseeducation;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectonlinecourseeducation.data.FakeApiService;

import com.example.projectonlinecourseeducation.utils.Utils;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtEmail, edtCode, edtNewPassword;
    Button btnSendCode, btnReset, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        edtCode = findViewById(R.id.edtCode);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnReset = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);

        btnSendCode.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (!Utils.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }
            FakeApiService.ApiResult<String> res =
                    FakeApiService.getInstance().requestPasswordReset(email);
            Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show();
            if (res.success) {
                // Hiển thị code demo để dev test (mô phỏng gửi mail)
                edtCode.setText(res.data); // chỉ để tiện thử
            }
        });

        btnReset.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String code = edtCode.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString();

            if (!Utils.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }
            if (TextUtils.isEmpty(code)) {
                edtCode.setError("Vui lòng nhập mã xác nhận");
                return;
            }
            if (!Utils.isStrongPassword(newPass)) {
                edtNewPassword.setError("Mật khẩu tối thiểu 6 ký tự, gồm chữ & số");
                return;
            }

            FakeApiService.ApiResult<Boolean> res =
                    FakeApiService.getInstance().resetPassword(email, code, newPass);

            Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show();
            if (res.success) finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
