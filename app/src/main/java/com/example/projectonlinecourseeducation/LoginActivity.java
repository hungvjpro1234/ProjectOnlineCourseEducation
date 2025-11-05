// app/src/main/java/com/example/projectonlinecourseeducation/LoginActivity.java
package com.example.projectonlinecourseeducation;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectonlinecourseeducation.data.FakeApiService;
import com.example.projectonlinecourseeducation.model.User;
import com.example.projectonlinecourseeducation.utils.Utils;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnSubmit, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (!Utils.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            FakeApiService.ApiResult<User> result =
                    FakeApiService.getInstance().login(email, password);

            if (result.success) {
                Toast.makeText(this,
                        "Đăng nhập thành công. Xin chào " + result.data.getName(),
                        Toast.LENGTH_SHORT).show();
                // TODO: chuyển sang màn hình chính sau đăng nhập
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
