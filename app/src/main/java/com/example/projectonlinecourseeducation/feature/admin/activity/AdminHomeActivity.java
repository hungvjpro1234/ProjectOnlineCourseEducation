package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminStatisticFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminUserManagementFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminCourseManagementFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminCourseApprovalFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminNotificationsFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminProfileFragment;

/**
 * Activity trang chủ Admin
 * Hiển thị Dashboard với 6 menu chính qua Bottom Navigation
 */
public class AdminHomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        initViews();
        setupBottomNavigation();
        setupListeners();

        // Load fragment mặc định
        if (savedInstanceState == null) {
            replaceFragment(new AdminStatisticFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_statistics) {
                selectedFragment = new AdminStatisticFragment();
            } else if (itemId == R.id.nav_user_management) {
                selectedFragment = new AdminUserManagementFragment();
            } else if (itemId == R.id.nav_course_management) {
                selectedFragment = new AdminCourseManagementFragment();
            } else if (itemId == R.id.nav_course_approval) {
                selectedFragment = new AdminCourseApprovalFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new AdminNotificationsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new AdminProfileFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            // TODO: Xử lý logout
            finish();
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
