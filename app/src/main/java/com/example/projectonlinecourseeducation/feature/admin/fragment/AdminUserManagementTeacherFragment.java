package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageTeacherDetailActivity;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserTeacherAdapter;
import com.example.projectonlinecourseeducation.feature.admin.model.TeacherStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fragment quản lý giảng viên với REAL DATA
 * - Hiển thị danh sách teachers với stats (total courses, total revenue, avg rating)
 * - Sorting: Tên A-Z, Z-A, Avg Rating ↓↑, Total Revenue ↓↑
 * - Filtering: Theo tên
 */
public class AdminUserManagementTeacherFragment extends Fragment {

    private RecyclerView rvTeacherList;
    private EditText etSearchTeacher;
    private Button btnSortTeacher;
    private UserTeacherAdapter adapter;

    private AuthApi authApi;
    private CourseApi courseApi;

    private List<TeacherStats> allTeacherStats = new ArrayList<>();
    private List<TeacherStats> filteredTeacherStats = new ArrayList<>();

    // Sorting state
    private enum SortType {
        NAME_AZ, NAME_ZA, AVG_RATING_DESC, AVG_RATING_ASC, TOTAL_REVENUE_DESC, TOTAL_REVENUE_ASC
    }
    private SortType currentSort = SortType.NAME_AZ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage_user_teacher, container, false);

        initAPIs();
        initViews(view);
        setupAdapter();
        setupListeners();
        loadTeacherData();

        return view;
    }

    private void initAPIs() {
        authApi = ApiProvider.getAuthApi();
        courseApi = ApiProvider.getCourseApi();
    }

    private void initViews(View view) {
        rvTeacherList = view.findViewById(R.id.rvTeacherList);
        etSearchTeacher = view.findViewById(R.id.etSearchTeacher);
        btnSortTeacher = view.findViewById(R.id.btnSortTeacher);
    }

    private void setupAdapter() {
        adapter = new UserTeacherAdapter(teacherStats -> {
            // Chuyển tới activity chi tiết
            Intent intent = new Intent(getContext(), AdminManageTeacherDetailActivity.class);
            intent.putExtra("userId", teacherStats.getUser().getId());
            intent.putExtra("userName", teacherStats.getUser().getName());
            startActivity(intent);
        });

        rvTeacherList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeacherList.setAdapter(adapter);
    }

    private void setupListeners() {
        // Nút sort - hiển thị dialog chọn
        btnSortTeacher.setOnClickListener(v -> showSortDialog());

        // Search/Filter theo tên
        etSearchTeacher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTeachers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Load danh sách teachers và tính stats
     */
    private void loadTeacherData() {
        // Lấy tất cả teachers
        List<User> teachers = authApi.getAllUsersByRole(User.Role.TEACHER);

        // Lấy tất cả courses
        List<Course> allCourses = courseApi.listAll();

        // Tính stats cho mỗi teacher
        allTeacherStats.clear();
        for (User teacher : teachers) {
            String teacherName = teacher.getName();

            // Lấy courses của teacher này
            List<Course> teacherCourses = new ArrayList<>();
            for (Course course : allCourses) {
                if (course.getTeacher() != null && course.getTeacher().equals(teacherName)) {
                    teacherCourses.add(course);
                }
            }

            int totalCourses = teacherCourses.size();

            // Tính tổng revenue (price × students)
            double totalRevenue = 0;
            for (Course c : teacherCourses) {
                totalRevenue += c.getPrice() * c.getStudents();
            }

            // Tính average rating
            double avgRating = 0;
            if (totalCourses > 0) {
                double sumRating = 0;
                for (Course c : teacherCourses) {
                    sumRating += c.getRating();
                }
                avgRating = sumRating / totalCourses;
            }

            // Tạo TeacherStats
            TeacherStats stats = new TeacherStats(teacher, totalCourses, totalRevenue, avgRating);
            allTeacherStats.add(stats);
        }

        // Apply sort và filter
        filteredTeacherStats = new ArrayList<>(allTeacherStats);
        applySorting();
        adapter.setTeachers(filteredTeacherStats);
    }

    /**
     * Filter teachers theo tên
     */
    private void filterTeachers(String query) {
        String lowerQuery = query.trim().toLowerCase(Locale.getDefault());

        if (lowerQuery.isEmpty()) {
            filteredTeacherStats = new ArrayList<>(allTeacherStats);
        } else {
            filteredTeacherStats.clear();
            for (TeacherStats stats : allTeacherStats) {
                String name = stats.getUser().getName().toLowerCase(Locale.getDefault());
                String email = stats.getUser().getEmail().toLowerCase(Locale.getDefault());
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredTeacherStats.add(stats);
                }
            }
        }

        applySorting();
        adapter.setTeachers(filteredTeacherStats);
    }

    /**
     * Hiển thị dialog chọn cách sắp xếp
     */
    private void showSortDialog() {
        String[] options = {
                "Tên A-Z (mặc định)",
                "Tên Z-A",
                "Rating TB ↓ (cao → thấp)",
                "Rating TB ↑ (thấp → cao)",
                "Tổng thu nhập ↓ (cao → thấp)",
                "Tổng thu nhập ↑ (thấp → cao)"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp theo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentSort = SortType.NAME_AZ;
                            break;
                        case 1:
                            currentSort = SortType.NAME_ZA;
                            break;
                        case 2:
                            currentSort = SortType.AVG_RATING_DESC;
                            break;
                        case 3:
                            currentSort = SortType.AVG_RATING_ASC;
                            break;
                        case 4:
                            currentSort = SortType.TOTAL_REVENUE_DESC;
                            break;
                        case 5:
                            currentSort = SortType.TOTAL_REVENUE_ASC;
                            break;
                    }
                    applySorting();
                    adapter.setTeachers(filteredTeacherStats);
                })
                .show();
    }

    /**
     * Apply sorting lên filteredTeacherStats
     */
    private void applySorting() {
        Comparator<TeacherStats> comparator;

        switch (currentSort) {
            case NAME_ZA:
                comparator = (t1, t2) -> t2.getUser().getName().compareToIgnoreCase(t1.getUser().getName());
                break;
            case AVG_RATING_DESC:
                comparator = (t1, t2) -> Double.compare(t2.getAverageRating(), t1.getAverageRating());
                break;
            case AVG_RATING_ASC:
                comparator = (t1, t2) -> Double.compare(t1.getAverageRating(), t2.getAverageRating());
                break;
            case TOTAL_REVENUE_DESC:
                comparator = (t1, t2) -> Double.compare(t2.getTotalRevenue(), t1.getTotalRevenue());
                break;
            case TOTAL_REVENUE_ASC:
                comparator = (t1, t2) -> Double.compare(t1.getTotalRevenue(), t2.getTotalRevenue());
                break;
            case NAME_AZ:
            default:
                comparator = (t1, t2) -> t1.getUser().getName().compareToIgnoreCase(t2.getUser().getName());
                break;
        }

        Collections.sort(filteredTeacherStats, comparator);
    }
}
