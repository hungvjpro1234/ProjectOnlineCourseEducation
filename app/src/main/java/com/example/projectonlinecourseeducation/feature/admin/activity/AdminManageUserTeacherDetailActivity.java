package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserTeacherOwnedCourseAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity hiển thị chi tiết teacher: courses owned
 */
public class AdminManageUserTeacherDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminManageUserTeacherDetail";

    private ImageButton btnBack;
    private RecyclerView rvOwnedCourses;
    private android.widget.TextView tvTeacherName;
    private android.widget.TextView tvTotalRevenue;
    private android.widget.TextView tvOwnedCourseCount;

    private UserTeacherOwnedCourseAdapter ownedCourseAdapter;
    private CourseApi courseApi;

    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_teacher_detail);

        initAPIs();
        getIntentData();
        initViews();
        setupAdapter();
        setupListeners();

        // Nếu userId hợp lệ thì load dữ liệu, nếu không thì activity đã finish trong getIntentData()
        if (userId != null && !userId.trim().isEmpty()) {
            loadTeacherData();
        }
    }

    private void initAPIs() {
        courseApi = ApiProvider.getCourseApi();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");

        Log.d(TAG, "Received intent extras: userId=" + userId + " userName=" + userName);

        if (userId == null || userId.trim().isEmpty()) {
            // Nếu không có userId thì báo và finish() để tránh activity ở trạng thái lửng
            Toast.makeText(this, "Không tìm thấy thông tin teacher (userId trống).", Toast.LENGTH_LONG).show();
            Log.e(TAG, "userId is null or empty - finishing activity to avoid undefined behavior.");
            finish();
            return;
        }

        // đảm bảo userName không null
        if (userName == null) {
            userName = "";
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOwnedCourses = findViewById(R.id.rvOwnedCourses);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvOwnedCourseCount = findViewById(R.id.tvOwnedCourseCount);

        // Set teacher name
        if (userName != null && !userName.isEmpty()) {
            tvTeacherName.setText(userName);
        } else {
            tvTeacherName.setText(getString(R.string.unknown_teacher));
        }
    }

    private void setupAdapter() {
        // Setup owned courses adapter (with click listener for course details)
        ownedCourseAdapter = new UserTeacherOwnedCourseAdapter(course -> {
            // Keep the callback (optional) — you can remove or change this if you don't need the toast.
            Toast.makeText(this,
                    "Xem chi tiết khóa học: " + course.getTitle(),
                    Toast.LENGTH_SHORT).show();

            // Optionally you could navigate from here instead of adapter.
            // Intent intent = new Intent(this, AdminCourseManagementDetailActivity.class);
            // intent.putExtra("courseId", course.getId());
            // intent.putExtra("courseTitle", course.getTitle());
            // startActivity(intent);
        });
        rvOwnedCourses.setLayoutManager(new LinearLayoutManager(this));
        rvOwnedCourses.setAdapter(ownedCourseAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Load teacher's owned courses and calculate stats
     */
    private void loadTeacherData() {
        // Get all courses
        List<Course> allCourses;
        try {
            allCourses = courseApi.listAll();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load courses from API", e);
            Toast.makeText(this, "Tải khóa học thất bại.", Toast.LENGTH_SHORT).show();
            allCourses = new ArrayList<>();
        }

        // Filter courses owned by this teacher (so sánh theo tên hiện có trong model)
        List<Course> ownedCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (course == null) continue;
            String courseTeacher = course.getTeacher();
            if (courseTeacher != null && courseTeacher.equals(userName)) {
                ownedCourses.add(course);
            }
        }

        // Set course count
        tvOwnedCourseCount.setText("Khóa học sở hữu (" + ownedCourses.size() + " khóa)");

        // Calculate total revenue (price × students)
        double totalRevenue = 0;
        for (Course c : ownedCourses) {
            try {
                totalRevenue += c.getPrice() * c.getStudents();
            } catch (Exception e) {
                Log.w(TAG, "Error calculating revenue for course, skipping. course=" + (c != null ? c.getTitle() : "null"), e);
            }
        }
        NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        tvTotalRevenue.setText("Tổng thu nhập: " + currencyFormat.format(totalRevenue) + " VNĐ");

        // Set courses to adapter
        ownedCourseAdapter.setCourses(ownedCourses);
    }
}
