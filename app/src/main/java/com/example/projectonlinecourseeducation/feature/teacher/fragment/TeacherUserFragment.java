package com.example.projectonlinecourseeducation.feature.teacher.fragment;

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
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherEditProfileActivity;

/**
 * Fragment hiển thị thông tin cá nhân của Teacher
 * Chỉ đọc + điều hướng sang màn hình chỉnh sửa
 */
public class TeacherUserFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvRole;
    private Button btnEditProfile;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.ivTeacherAvatar);
        tvName = view.findViewById(R.id.tvTeacherName);
        tvUsername = view.findViewById(R.id.tvTeacherUsername);
        tvEmail = view.findViewById(R.id.tvTeacherEmail);
        tvRole = view.findViewById(R.id.tvTeacherRole);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Avatar mặc định cho Teacher
        ivAvatar.setImageResource(R.drawable.ava_teacher);

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        bindCurrentUser();
                    }
                }
        );

        btnEditProfile.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), TeacherEditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        bindCurrentUser();
    }

    private void bindCurrentUser() {
        User currentUser = ApiProvider.getAuthApi().getCurrentUser();

        if (currentUser == null) {
            tvName.setText("Chưa đăng nhập");
            tvUsername.setText("Username: -");
            tvEmail.setText("Email: -");
            tvRole.setText("Vai trò: -");
            btnEditProfile.setEnabled(false);
            Toast.makeText(getContext(),
                    "Không tìm thấy thông tin giảng viên.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        tvName.setText(currentUser.getName() != null ? currentUser.getName() : "-");
        tvUsername.setText("Username: " + currentUser.getUsername());
        tvEmail.setText("Email: " + currentUser.getEmail());
        tvRole.setText("Vai trò: Giảng viên");

        btnEditProfile.setEnabled(true);
    }
}
