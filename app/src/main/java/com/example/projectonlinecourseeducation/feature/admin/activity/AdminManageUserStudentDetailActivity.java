package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserStudentInCartCourseAdapter;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserStudentPurchasedCourseAdapter;
import com.example.projectonlinecourseeducation.feature.admin.model.CourseProgressStats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity hiển thị chi tiết student: cart courses, purchased courses với progress
 *
 * Sửa đổi:
 *  - Truyền userId cho adapter để adapter có thể load per-student lesson progress on-expand.
 *  - Load course-level progress in background (avoid UI blocking).
 *  - Đăng ký LessonProgressUpdateListener để refresh summary when progress changes.
 */
public class AdminManageUserStudentDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminManageUserStudentDetailAct";

    private ImageButton btnBack;
    private RecyclerView rvCartCourses;
    private RecyclerView rvPurchasedCourses;
    private android.widget.TextView tvCartCourseCount;
    private android.widget.TextView tvPurchasedCourseCount;
    private android.widget.TextView tvStudentName;
    private android.widget.TextView tvTotalSpent;

    private UserStudentInCartCourseAdapter cartCourseAdapter;
    private UserStudentPurchasedCourseAdapter purchasedCourseAdapter;

    private CartApi cartApi;
    private MyCourseApi myCourseApi;
    private LessonApi lessonApi;
    private LessonProgressApi lessonProgressApi;

    private String userId;
    private String userName;

    // background executor to compute progress without blocking UI
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    // listener to refresh when lesson progress updates
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_student_detail);

        initAPIs();
        getIntentData();
        initViews();
        setupAdapters();
        setupListeners();
        registerLessonProgressListener();
        loadStudentData(); // this will run heavy parts in background
    }

    private void initAPIs() {
        cartApi = ApiProvider.getCartApi();
        myCourseApi = ApiProvider.getMyCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        lessonProgressApi = ApiProvider.getLessonProgressApi();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        userId = intent != null ? intent.getStringExtra("userId") : null;
        userName = intent != null ? intent.getStringExtra("userName") : null;

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin student", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCartCourses = findViewById(R.id.rvCartCourses);
        rvPurchasedCourses = findViewById(R.id.rvPurchasedCourses);
        tvCartCourseCount = findViewById(R.id.tvCartCourseCount);
        tvPurchasedCourseCount = findViewById(R.id.tvPurchasedCourseCount);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);

        if (userName != null && !userName.isEmpty()) {
            tvStudentName.setText(userName);
        }
    }

    private void setupAdapters() {
        cartCourseAdapter = new UserStudentInCartCourseAdapter();
        rvCartCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCartCourses.setAdapter(cartCourseAdapter);

        // **Important**: pass userId to adapter so it can call LessonProgressApi.getLessonProgress(lessonId, studentId)
        purchasedCourseAdapter = new UserStudentPurchasedCourseAdapter(courseStats -> {
            Toast.makeText(this,
                    "Xem chi tiết khóa học: " + (courseStats != null && courseStats.getCourse() != null ? courseStats.getCourse().getTitle() : ""),
                    Toast.LENGTH_SHORT).show();
            // TODO: navigate to course detail if needed
        }, userId);

        rvPurchasedCourses.setLayoutManager(new LinearLayoutManager(this));
        rvPurchasedCourses.setAdapter(purchasedCourseAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Register listener for lesson progress updates. On update we refresh course-level summaries.
     * Listener callback gives lessonId; for simplicity we recompute summaries for all purchased courses.
     */
    private void registerLessonProgressListener() {
        try {
            if (lessonProgressApi == null) return;
            lessonProgressListener = new LessonProgressApi.LessonProgressUpdateListener() {
                @Override
                public void onLessonProgressChanged(String lessonId) {
                    // Recompute summaries (background)
                    Log.d(TAG, "LessonProgress changed for lessonId=" + lessonId + " -> refreshing summaries");
                    loadStudentData();
                }
            };
            lessonProgressApi.addLessonProgressUpdateListener(lessonProgressListener);
        } catch (Exception e) {
            Log.w(TAG, "Failed to register lessonProgressListener: " + e.getMessage(), e);
        }
    }

    /**
     * Load cart + purchased courses and compute course-level progress for this student.
     * Heavy work runs on background executor; UI updated on main thread.
     */
    private void loadStudentData() {
        bgExecutor.execute(() -> {
            try {
                // load cart courses
                List<Course> cartCourses = new ArrayList<>();
                try {
                    List<Course> tmp = cartApi.getCartCoursesForUser(userId);
                    if (tmp != null) cartCourses = tmp;
                } catch (Exception ignored) {}

                // load purchased courses
                List<Course> purchasedCourses = new ArrayList<>();
                try {
                    List<Course> tmp = myCourseApi.getMyCoursesForUser(userId);
                    if (tmp != null) purchasedCourses = tmp;
                } catch (Exception ignored) {}

                // compute total spent
                double totalSpent = 0;
                for (Course c : purchasedCourses) {
                    if (c != null) totalSpent += c.getPrice();
                }

                // compute course-level progress for each purchased course (background)
                List<CourseProgressStats> purchasedWithProgress = new ArrayList<>();
                for (Course course : purchasedCourses) {
                    if (course == null) continue;
                    CourseProgressStats stats = calculateCourseProgress(course, userId);
                    purchasedWithProgress.add(stats);
                }

                // Make final copies to be captured by the UI lambda
                final List<Course> finalCartCourses = cartCourses;
                final List<CourseProgressStats> finalPurchasedWithProgress = purchasedWithProgress;
                final double finalTotalSpent = totalSpent;
                final int finalPurchasedCount = purchasedCourses.size();

                // update UI on main thread
                runOnUiThread(() -> {
                    cartCourseAdapter.setCourses(finalCartCourses);
                    tvCartCourseCount.setText("Giỏ hàng (" + finalCartCourses.size() + " khóa)");

                    NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
                    tvTotalSpent.setText("Tổng chi: " + currencyFormat.format(finalTotalSpent) + " VNĐ");

                    purchasedCourseAdapter.setCourses(finalPurchasedWithProgress);
                    tvPurchasedCourseCount.setText("Khóa học đã mua (" + finalPurchasedCount + " khóa)");
                });
            } catch (Exception e) {
                Log.w(TAG, "loadStudentData failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Compute course-level progress for a specific student.
     * This runs on background thread when called from loadStudentData.
     */
    private CourseProgressStats calculateCourseProgress(Course course, String studentId) {
        int totalLessons = 0;
        int completedLessons = 0;

        try {
            List<Lesson> lessons = new ArrayList<>();
            if (lessonApi != null) {
                List<Lesson> tmp = lessonApi.getLessonsForCourse(course.getId());
                if (tmp != null) lessons = tmp;
            }
            totalLessons = lessons.size();

            if (lessonProgressApi != null && studentId != null) {
                for (Lesson lesson : lessons) {
                    try {
                        LessonProgress lp = lessonProgressApi.getLessonProgress(lesson.getId(), studentId);
                        if (lp != null && lp.isCompleted()) completedLessons++;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "calculateCourseProgress error for course " + (course != null ? course.getId() : "null") + ": " + e.getMessage());
        }

        CourseProgressStats stats = new CourseProgressStats(course, totalLessons, completedLessons);
        // compute percent on-the-fly where needed (adapter will compute from total/completed)
        return stats;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (lessonProgressListener != null && lessonProgressApi != null) {
                lessonProgressApi.removeLessonProgressUpdateListener(lessonProgressListener);
            }
        } catch (Exception ignored) {}

        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}
