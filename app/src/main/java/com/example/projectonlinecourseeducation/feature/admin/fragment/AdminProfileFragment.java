package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;

/**
 * Fragment hiển thị thông tin Admin (CHỈ XEM – KHÔNG CHỈNH SỬA)
 */
public class AdminProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.ivAdminAvatar);
        tvName = view.findViewById(R.id.tvAdminName);
        tvUsername = view.findViewById(R.id.tvAdminUsername);
        tvEmail = view.findViewById(R.id.tvAdminEmail);
        tvRole = view.findViewById(R.id.tvAdminRole);

        // Avatar admin
        ivAvatar.setImageResource(R.drawable.ava_admin);

        bindCurrentAdmin();
    }

    private void bindCurrentAdmin() {
        User currentUser = ApiProvider.getAuthApi().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),
                    "Không tìm thấy thông tin admin.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        tvName.setText(currentUser.getName() != null
                ? currentUser.getName()
                : "-");

        tvUsername.setText("Username: "
                + (currentUser.getUsername() != null
                ? currentUser.getUsername()
                : "-"));

        tvEmail.setText("Email: "
                + (currentUser.getEmail() != null
                ? currentUser.getEmail()
                : "-"));

        tvRole.setText("Vai trò: Admin");
    }
}
