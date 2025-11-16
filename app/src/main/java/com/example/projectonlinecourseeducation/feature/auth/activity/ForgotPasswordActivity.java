package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.utils.Utils;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.ApiResult;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtEmail;
    Button btnSendLink, btnBack;
    TextView tvDebugLink; // hiển thị link demo để dev test nhanh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        btnBack = findViewById(R.id.btnBack);
        tvDebugLink = findViewById(R.id.tvDebugLink);

        btnSendLink.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (!Utils.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }

            AuthApi authApi = ApiProvider.getAuthApi();
            ApiResult<String> res = authApi.requestPasswordResetLink(email);

            Toast.makeText(this, res.getMessage(), Toast.LENGTH_SHORT).show();
            if (res.isSuccess()) {
                // Hiển thị link demo để dev có thể copy (mô phỏng inbox)
                tvDebugLink.setVisibility(TextView.VISIBLE);
                tvDebugLink.setText(
                        "Demo link: " + res.getData()
                                + "\n(Luồng thật: kiểm tra email của bạn)"
                );
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
