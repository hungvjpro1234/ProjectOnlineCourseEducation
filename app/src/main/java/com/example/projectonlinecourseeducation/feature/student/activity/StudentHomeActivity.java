package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.feature.auth.activity.MainActivity2;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentCartFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentHomeFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentMyCourseFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentNotificationFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout;
    private BottomNavigationView bottomNav;

    // biến flag để kiểm tra double-back
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);

        // 👉 Lấy user hiện tại từ AuthApi (fake session) để hiển thị đúng tên
        updateGreeting();

        // Logout: yêu cầu bấm 2 lần để xác nhận đăng xuất
        btnLogout.setOnClickListener(v -> requestLogoutWithDoubleCheck());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                f = new StudentHomeFragment();
            } else if (id == R.id.nav_cart) {
                f = new StudentCartFragment();
            } else if (id == R.id.nav_mycourse) {
                f = new StudentMyCourseFragment();
            } else if (id == R.id.nav_notification) {
                f = new StudentNotificationFragment();
            } else { // R.id.nav_user
                f = new StudentUserFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.student_fragment_container, f)
                    .commit();
            return true;
        });

        // mặc định mở Home
        // Nếu được truyền flag open_cart từ StudentCourseDetailActivity thì mở tab Giỏ hàng
        // Nếu được truyền flag open_my_course từ thanh toán thì mở tab My Course
        boolean openCart = getIntent().getBooleanExtra("open_cart", false);
        boolean openMyCourse = getIntent().getBooleanExtra("open_my_course", false);

        if (openCart) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        } else if (openMyCourse) {
            bottomNav.setSelectedItemId(R.id.nav_mycourse);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // 🚀 Back Press Callback mới theo chuẩn AndroidX
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back cũng dùng chung logic double-check đăng xuất
                requestLogoutWithDoubleCheck();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần quay lại màn StudentHomeActivity, đọc lại currentUser để greeting luôn mới
        updateGreeting();
    }

    /**
     * Đọc user hiện tại từ AuthApi (fake session) và set text lời chào.
     * Dùng chung cho onCreate + onResume.
     */
    private void updateGreeting() {
        User currentUser = ApiProvider.getAuthApi().getCurrentUser();
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) {
            tvGreeting.setText("Xin chào, học viên " + currentUser.getName() + " !");
        } else {
            tvGreeting.setText("Xin chào");
        }
    }

    /**
     * Yêu cầu user bấm 2 lần trong 2s để xác nhận đăng xuất.
     * Dùng chung cho cả nút Logout và nút Back.
     */
    private void requestLogoutWithDoubleCheck() {
        if (!doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = true;
            Toast.makeText(
                    this,
                    "Bấm lần nữa để đăng xuất",
                    Toast.LENGTH_SHORT
            ).show();

            handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            doLogout();
        }
    }

    private void doLogout() {
        // 🔓 Clear fake session khi logout
        ApiProvider.getAuthApi().setCurrentUser(null);

        Intent intent = new Intent(this, MainActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}