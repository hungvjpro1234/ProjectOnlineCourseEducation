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
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.feature.auth.activity.MainActivity2;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentCartFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentHomeFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentMyCourseFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentNotificationFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.google.android.material.badge.BadgeDrawable;

public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout;
    private BottomNavigationView bottomNav;

    private NotificationApi notificationApi;
    private String currentUserId;

    // biến flag để kiểm tra double-back
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);

        // Lấy NotificationApi và userId
        notificationApi = com.example.projectonlinecourseeducation.data.ApiProvider.getNotificationApi();
        User currentUser = com.example.projectonlinecourseeducation.data.ApiProvider.getAuthApi().getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getId() : null;

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

        // ✅ FIX: Force sync MyCourse và Cart cache khi app start để đồng bộ với backend
        preloadMyCourseCache();
        preloadCartCache();

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

        // Cập nhật badge lần đầu khi vào màn hình
        updateNotificationBadge();

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
        // Mỗi lần quay lại màn StudentHomeActivity, đọc lại currentUser để greeting luôn mới
        updateGreeting();

        // Cập nhật lại badge mỗi lần quay lại activity
        updateNotificationBadge();
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

    /**
     * Cập nhật badge số lượng thông báo chưa xem lên icon chuông
     */
    private void updateNotificationBadge() {
        if (notificationApi == null || currentUserId == null) return;

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    return notificationApi.getUnreadCount(currentUserId);
                },
                new AsyncApiHelper.ApiCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        // ===== MAIN THREAD =====
                        if (count == null) return;
                        setNotificationBadge(count);
                    }

                    @Override
                    public void onError(Exception e) {
                        // silent fail – không cần toast
                    }
                }
        );
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

    /**
     * ✅ FIX CRITICAL: Preload MyCourse cache để sync với backend
     *
     * VẤN ĐỀ: MyCourse cache chỉ init khi user mở tab MyCourse
     * → isPurchased() check cache rỗng → trả về false → hiển thị sai
     *
     * GIẢI PHÁP: Gọi getMyCourses() ngay khi app start để sync cache
     */
    private void preloadMyCourseCache() {
        AsyncApiHelper.execute(
                () -> {
                    // Gọi getMyCourses() để sync cache với backend
                    ApiProvider.getMyCourseApi().getMyCourses();
                    return null;
                },
                new AsyncApiHelper.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Cache đã sync - CourseStatusResolver.isPurchased() giờ sẽ đúng
                        android.util.Log.d("StudentHomeActivity", "✅ MyCourse cache preloaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log lỗi nhưng không crash app
                        android.util.Log.e("StudentHomeActivity", "❌ Failed to preload MyCourse cache", e);
                    }
                }
        );
    }

    /**
     * ✅ FIX CRITICAL: Preload Cart cache để sync với backend
     *
     * VẤN ĐỀ: Cart cache chỉ init khi user mở tab Cart
     * → isInCart() check cache rỗng → trả về false → hiển thị sai trạng thái nút
     *
     * GIẢI PHÁP: Gọi getCartCourses() ngay khi app start để sync cache
     */
    private void preloadCartCache() {
        AsyncApiHelper.execute(
                () -> {
                    // Gọi getCartCourses() để sync cache với backend
                    ApiProvider.getCartApi().getCartCourses();
                    return null;
                },
                new AsyncApiHelper.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Cache đã sync - CourseStatusResolver.isInCart() giờ sẽ đúng
                        android.util.Log.d("StudentHomeActivity", "✅ Cart cache preloaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log lỗi nhưng không crash app
                        android.util.Log.e("StudentHomeActivity", "❌ Failed to preload Cart cache", e);
                    }
                }
        );
    }
}