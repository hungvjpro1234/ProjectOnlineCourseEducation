package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.ApiResult;
import com.example.projectonlinecourseeducation.feature.auth.activity.MainActivity2;

public class StudentEditProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtName;
    private EditText edtEmail;
    private EditText edtUsername;
    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private Button btnSave;

    // Lưu user hiện tại + username ban đầu để biết có đổi username hay không
    private User currentUser;
    private String originalUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        edtName = findViewById(R.id.edtStudentName);
        edtEmail = findViewById(R.id.edtStudentEmail);
        edtUsername = findViewById(R.id.edtStudentUsername);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSave = findViewById(R.id.btnSaveProfile);

        // Load thông tin user hiện tại để fill vào form
        currentUser = ApiProvider.getAuthApi().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Không tìm thấy thông tin học viên.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        originalUsername = currentUser.getUsername();

        if (currentUser.getName() != null) {
            edtName.setText(currentUser.getName());
        }
        if (currentUser.getEmail() != null) {
            edtEmail.setText(currentUser.getEmail());
        }
        if (currentUser.getUsername() != null) {
            edtUsername.setText(currentUser.getUsername());
        }

        btnBack.setOnClickListener(v -> onBackPressed());

        // Bấm lưu -> hiện dialog confirm trước khi thực sự gọi saveProfile()
        btnSave.setOnClickListener(v ->
                DialogConfirmHelper.showConfirmDialog(
                        this,
                        "Xác nhận lưu thay đổi",
                        "Bạn có chắc muốn cập nhật thông tin của mình?",
                        R.drawable.question, // không dùng icon riêng, để 0 cho an toàn
                        "Xác nhận",
                        "Hủy",
                        R.color.colorSecondary,
                        () -> saveProfile()
                )
        );
    }

    private void saveProfile() {

        String newName = edtName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newUsername = edtUsername.getText().toString().trim();

        String oldPassword = edtOldPassword.getText().toString();
        String newPassword = edtNewPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // ===== VALIDATE (UI THREAD) =====
        if (TextUtils.isEmpty(newName)) {
            edtName.setError("Vui lòng nhập tên.");
            edtName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newUsername)) {
            edtUsername.setError("Vui lòng nhập username.");
            edtUsername.requestFocus();
            return;
        }
        if (newUsername.length() < 4) {
            edtUsername.setError("Username phải có ít nhất 4 ký tự.");
            edtUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newEmail)) {
            edtEmail.setError("Vui lòng nhập email.");
            edtEmail.requestFocus();
            return;
        }
        if (!newEmail.contains("@")) {
            edtEmail.setError("Email không hợp lệ.");
            edtEmail.requestFocus();
            return;
        }

        boolean wantChangePassword =
                !TextUtils.isEmpty(oldPassword)
                        || !TextUtils.isEmpty(newPassword)
                        || !TextUtils.isEmpty(confirmPassword);

        if (wantChangePassword) {
            if (TextUtils.isEmpty(oldPassword)) {
                edtOldPassword.setError("Vui lòng nhập mật khẩu cũ.");
                edtOldPassword.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(newPassword)) {
                edtNewPassword.setError("Vui lòng nhập mật khẩu mới.");
                edtNewPassword.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu nhập lại không khớp.");
                edtConfirmPassword.requestFocus();
                return;
            }
        }

        // ===== ASYNC CALL =====
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====

                    ApiResult<User> profileResult =
                            ApiProvider.getAuthApi()
                                    .updateCurrentUserProfile(newName, newEmail, newUsername);

                    if (!profileResult.isSuccess()) {
                        return profileResult;
                    }

                    if (wantChangePassword) {
                        ApiResult<Boolean> pwdResult =
                                ApiProvider.getAuthApi()
                                        .changeCurrentUserPassword(oldPassword, newPassword);

                        if (!pwdResult.isSuccess()) {
                            return pwdResult;
                        }
                    }

                    return profileResult;
                },
                new AsyncApiHelper.ApiCallback<ApiResult<?>>() {
                    @Override
                    public void onSuccess(ApiResult<?> result) {
                        // ===== MAIN THREAD =====

                        boolean usernameChanged =
                                originalUsername == null
                                        ? !TextUtils.isEmpty(newUsername)
                                        : !originalUsername.equals(newUsername);

                        boolean loginInfoChanged = usernameChanged || wantChangePassword;

                        if (loginInfoChanged) {
                            Toast.makeText(
                                    StudentEditProfileActivity.this,
                                    "Cập nhật thông tin đăng nhập thành công. Vui lòng đăng nhập lại.",
                                    Toast.LENGTH_LONG
                            ).show();

                            ApiProvider.getAuthApi().setCurrentUser(null);

                            Intent intent = new Intent(
                                    StudentEditProfileActivity.this,
                                    MainActivity2.class
                            );
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            Toast.makeText(
                                    StudentEditProfileActivity.this,
                                    "Cập nhật thông tin thành công.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                StudentEditProfileActivity.this,
                                "Lỗi cập nhật thông tin. Vui lòng thử lại.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

}
