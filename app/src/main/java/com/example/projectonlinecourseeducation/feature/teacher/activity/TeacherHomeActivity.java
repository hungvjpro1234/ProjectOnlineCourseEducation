// app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/TeacherHomeActivity.java
package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherHomeFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherManagementFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherNotificationFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherHomeActivity extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private TextView tvGreeting;
    private Button btnLogout;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        bindViews();
        setupGreeting();
        setupActions();
        setupFragmentManager();

        // Mặc định show Home Fragment
        if (savedInstanceState == null) {
            showFragment(new TeacherHomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void bindViews() {
        fragmentContainer = findViewById(R.id.teacher_fragment_container);
        bottomNav = findViewById(R.id.bottomNav);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupGreeting() {
        AuthApi authApi = ApiProvider.getAuthApi();
        User currentUser = authApi.getCurrentUser();

        if (currentUser != null) {
            String greeting = "Xin chào, " + currentUser.getName() + "!";
            tvGreeting.setText(greeting);
        }
    }

    private void setupActions() {
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            finish();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new TeacherHomeFragment();
            } else if (itemId == R.id.nav_management) {
                fragment = new TeacherManagementFragment();
            } else if (itemId == R.id.nav_notification) {
                fragment = new TeacherNotificationFragment();
            } else if (itemId == R.id.nav_user) {
                fragment = new TeacherUserFragment();
            }

            if (fragment != null) {
                showFragment(fragment);
            }

            return true;
        });
    }

    private void setupFragmentManager() {
        fragmentManager = getSupportFragmentManager();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.teacher_fragment_container, fragment);
        transaction.commit();
    }
}