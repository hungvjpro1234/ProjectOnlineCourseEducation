package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserTeacherOwnedCourseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin - Xem chi tiết teacher:
 * - Thông tin cơ bản
 * - Tổng số khóa học
 * - Tổng doanh thu
 * - Danh sách khóa học sở hữu
 *
 * Đồng bộ logic với FakeApi:
 * - students tăng khi recordPurchase()
 * - revenue = price * students
 * - teacher xác định bằng username (unique)
 */
public class AdminManageUserTeacherDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminTeacherDetail";

    // Views
    private ImageButton btnBack;
    private RecyclerView rvOwnedCourses;
    private TextView tvTeacherName;
    private TextView tvTotalRevenue;
    private TextView tvOwnedCourseCount;

    // Adapter & API
    private UserTeacherOwnedCourseAdapter ownedCourseAdapter;
    private CourseApi courseApi;

    // Data from Intent
    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_teacher_detail);

        initApis();
        getIntentData();
        initViews();
        setupAdapter();
        setupListeners();

        if (userName != null && !userName.trim().isEmpty()) {
            loadTeacherData();
        }
    }

    // ---------------------------------------------------------------------
    // Init
    // ---------------------------------------------------------------------

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");

        Log.d(TAG, "Intent: userId=" + userId + ", userName=" + userName);

        if (userName == null || userName.trim().isEmpty()) {
            Toast.makeText(this,
                    "Không tìm thấy thông tin giảng viên.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOwnedCourses = findViewById(R.id.rvOwnedCourses);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvOwnedCourseCount = findViewById(R.id.tvOwnedCourseCount);

        tvTeacherName.setText(userName);
        tvTotalRevenue.setText("0 VNĐ");
        tvOwnedCourseCount.setText("0");
    }

    private void setupAdapter() {
        ownedCourseAdapter = new UserTeacherOwnedCourseAdapter(course -> {
            Toast.makeText(this,
                    "Khóa học: " + course.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });

        rvOwnedCourses.setLayoutManager(new LinearLayoutManager(this));
        rvOwnedCourses.setAdapter(ownedCourseAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------------------
    // Load & Calculate Data
    // ---------------------------------------------------------------------

    /**
     * Load toàn bộ dữ liệu teacher:
     * - Courses owned
     * - Tổng số khóa học
     * - Tổng doanh thu
     */
    private void loadTeacherData() {
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    List<Course> allCourses = courseApi.listAll();
                    List<Course> ownedCourses = new ArrayList<>();

                    for (Course course : allCourses) {
                        if (course == null) continue;

                        if (userName.equals(course.getTeacher())) {
                            ownedCourses.add(course);
                        }
                    }

                    return ownedCourses;
                },
                new AsyncApiHelper.ApiCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> ownedCourses) {
                        // ===== MAIN THREAD =====
                        long totalRevenue = 0;

                        for (Course course : ownedCourses) {
                            long revenue =
                                    (long) (course.getPrice() * course.getStudents());
                            totalRevenue += revenue;
                        }

                        ownedCourseAdapter.setCourses(ownedCourses);
                        tvOwnedCourseCount.setText(
                                "Tổng khóa học sở hữu : " + ownedCourses.size()
                        );
                        tvTotalRevenue.setText(formatCurrencyVND(totalRevenue));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Load courses failed", e);
                        Toast.makeText(
                                AdminManageUserTeacherDetailActivity.this,
                                "Không thể tải danh sách khóa học.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ---------------------------------------------------------------------
    // Utils
    // ---------------------------------------------------------------------

    /**
     * Format tiền VNĐ: 45.600.000 VNĐ
     */
    private String formatCurrencyVND(long amount) {
        return String.format("%,d VNĐ", amount).replace(',', '.');
    }
}
