package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.feature.auth.activity.MainActivity2;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherHomeFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherManagementFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherNotificationFragment;
import com.example.projectonlinecourseeducation.feature.teacher.fragment.TeacherUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.google.android.material.badge.BadgeDrawable;

public class TeacherHomeActivity extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private TextView tvGreeting;
    private Button btnLogout;

    private FragmentManager fragmentManager;
    private NotificationApi notificationApi;
    private String currentUserId;

    // ---- Thêm biến để xử lý double-back logout giống Student ----
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Listener cập nhật badge khi có thay đổi thông báo
    private final NotificationApi.NotificationUpdateListener notificationListener = new NotificationApi.NotificationUpdateListener() {
        @Override
        public void onNotificationsChanged(String userId) {
            if (currentUserId != null && currentUserId.equals(userId)) {
                runOnUiThread(() -> updateNotificationBadge());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        bindViews();

        // Khởi tạo NotificationApi và lấy currentUserId
        notificationApi = ApiProvider.getNotificationApi();
        User currentUser = ApiProvider.getAuthApi().getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getId() : null;

        setupGreeting();
        setupActions();
        setupFragmentManager();

        // Mặc định show Home Fragment
        if (savedInstanceState == null) {
            showFragment(new TeacherHomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Cập nhật badge lần đầu
        updateNotificationBadge();

        // 🚀 Back Press Callback mới theo chuẩn AndroidX: back sẽ dùng chung logic double-check đăng xuất
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requestLogoutWithDoubleCheck();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Đăng ký listener để cập nhật badge khi có thay đổi thông báo
        if (notificationApi != null && currentUserId != null) {
            notificationApi.addNotificationUpdateListener(notificationListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký listener để tránh leak
        if (notificationApi != null && currentUserId != null) {
            notificationApi.removeNotificationUpdateListener(notificationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần activity resume, cập nhật lại tên greeting từ AuthApi
        updateGreeting();

        // Cập nhật lại badge
        updateNotificationBadge();
    }

    private void bindViews() {
        fragmentContainer = findViewById(R.id.teacher_fragment_container);
        bottomNav = findViewById(R.id.bottomNav);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupGreeting() {
        // Gọi updateGreeting để tránh lặp code
        updateGreeting();
    }

    /**
     * Đọc user hiện tại từ AuthApi (fake session) và set text lời chào.
     * Dùng cho onCreate + onResume.
     */
    private void updateGreeting() {
        AuthApi authApi = ApiProvider.getAuthApi();
        User currentUser = authApi.getCurrentUser();

        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) {
            String greeting = "Xin chào, " + currentUser.getName() + "!";
            tvGreeting.setText(greeting);
        } else {
            tvGreeting.setText("Xin chào");
        }
    }

    private void setupActions() {
        // Thay vì logout trực tiếp, dùng requestLogoutWithDoubleCheck để yêu cầu bấm 2 lần
        btnLogout.setOnClickListener(v -> requestLogoutWithDoubleCheck());

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

    /**
     * Yêu cầu bấm 2 lần trong 2s để xác nhận đăng xuất.
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
        // 🔓 Clear fake session khi logout (giống Student)
        ApiProvider.getAuthApi().setCurrentUser(null);

        Intent intent = new Intent(this, MainActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Cập nhật badge số lượng thông báo chưa xem lên icon chuông
     */
    private void updateNotificationBadge() {
        if (notificationApi == null || currentUserId == null) return;
        int unreadCount = notificationApi.getUnreadCount(currentUserId);
        setNotificationBadge(unreadCount);
    }

    /**
     * Hiển thị hoặc ẩn badge trên tab Thông báo
     */
    private void setNotificationBadge(int count) {
        if (bottomNav == null) return;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_notification);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
}
