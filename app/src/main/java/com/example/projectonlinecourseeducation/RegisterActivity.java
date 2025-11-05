// app/src/main/java/com/example/projectonlinecourseeducation/RegisterActivity.java
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
import com.example.projectonlinecourseeducation.model.User;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtPassword2;
    Button btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword2 = findViewById(R.id.edtPassword2);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String password2 = edtPassword2.getText().toString();

            if (TextUtils.isEmpty(name)) {
                edtName.setError("Vui lòng nhập họ tên");
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

            FakeApiService.ApiResult<User> result =
                    FakeApiService.getInstance().register(name, email, password);

            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            if (result.success) {
                finish(); // quay lại để đăng nhập
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
