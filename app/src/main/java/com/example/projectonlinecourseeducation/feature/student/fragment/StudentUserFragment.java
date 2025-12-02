package com.example.projectonlinecourseeducation.feature.student.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentEditProfileActivity;


public class StudentUserFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvRole;
    private Button btnEditProfile;

    // Launcher để nhận kết quả sau khi chỉnh sửa profile
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_student_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.ivStudentAvatar);
        tvName = view.findViewById(R.id.tvStudentName);
        tvUsername = view.findViewById(R.id.tvStudentUsername);
        tvEmail = view.findViewById(R.id.tvStudentEmail);
        tvRole = view.findViewById(R.id.tvStudentRole);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Avatar default cho student
        ivAvatar.setImageResource(R.drawable.ava_student);

        // Đăng ký launcher nhận kết quả từ màn hình Edit Profile
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Sau khi chỉnh sửa thành công -> load lại thông tin user
                        bindCurrentUser();
                    }
                }
        );

        btnEditProfile.setOnClickListener(v -> {
            if (getActivity() == null) return;

            // Mở màn hình chỉnh sửa thông tin
            Intent intent = new Intent(getActivity(), StudentEditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        // Lần đầu vào fragment -> bind dữ liệu user hiện tại
        bindCurrentUser();
    }

    private void bindCurrentUser() {
        User currentUser = ApiProvider.getAuthApi().getCurrentUser();
        if (currentUser == null) {
            // Không có user -> ẩn nút edit và hiển thị thông báo đơn giản
            tvName.setText("Chưa đăng nhập");
            tvUsername.setText("Username: -");
            tvEmail.setText("Email: -");
            tvRole.setText("Vai trò: -");
            btnEditProfile.setEnabled(false);
            Toast.makeText(getContext(), "Không tìm thấy thông tin học viên.", Toast.LENGTH_SHORT).show();
            return;
        }

        tvName.setText(currentUser.getName() != null ? currentUser.getName() : "-");
        tvUsername.setText("Username: " + (currentUser.getUsername() != null ? currentUser.getUsername() : "-"));
        tvEmail.setText("Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "-"));
        tvRole.setText("Vai trò: Học viên");

        btnEditProfile.setEnabled(true);
    }
}
