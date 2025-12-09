package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.os.Bundle;
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

    private ImageButton btnBack;
    private RecyclerView rvOwnedCourses;
    private android.widget.TextView tvTeacherName;
    private android.widget.TextView tvTotalRevenue;
    private android.widget.TextView tvAverageRating;
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
        loadTeacherData();
    }

    private void initAPIs() {
        courseApi = ApiProvider.getCourseApi();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin teacher", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOwnedCourses = findViewById(R.id.rvOwnedCourses);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvOwnedCourseCount = findViewById(R.id.tvOwnedCourseCount);

        // Set teacher name
        if (userName != null && !userName.isEmpty()) {
            tvTeacherName.setText(userName);
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
        List<Course> allCourses = courseApi.listAll();

        // Filter courses owned by this teacher
        List<Course> ownedCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (course.getTeacher() != null && course.getTeacher().equals(userName)) {
                ownedCourses.add(course);
            }
        }

        // Set course count
        tvOwnedCourseCount.setText("Khóa học sở hữu (" + ownedCourses.size() + " khóa)");

        // Calculate total revenue (price × students)
        double totalRevenue = 0;
        for (Course c : ownedCourses) {
            totalRevenue += c.getPrice() * c.getStudents();
        }
        NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        tvTotalRevenue.setText("Tổng thu nhập: " + currencyFormat.format(totalRevenue) + " VNĐ");

        // Calculate average rating
        double avgRating = 0;
        if (ownedCourses.size() > 0) {
            double sumRating = 0;
            for (Course c : ownedCourses) {
                sumRating += c.getRating();
            }
            avgRating = sumRating / ownedCourses.size();
        }
        tvAverageRating.setText(String.format(Locale.getDefault(),
                "Rating TB: %.1f ⭐", avgRating));

        // Set courses to adapter
        ownedCourseAdapter.setCourses(ownedCourses);
    }
}
