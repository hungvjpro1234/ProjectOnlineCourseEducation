package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageUserStudentDetailActivity;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserStudentAdapter;
import com.example.projectonlinecourseeducation.feature.admin.model.StudentStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fragment quản lý học viên với REAL DATA
 * - Hiển thị danh sách students với stats (total spent, courses purchased, cart items)
 * - Sorting: Tên A-Z, Z-A, Tổng tiền ↓↑
 * - Filtering: Theo tên
 */
public class AdminUserManagementStudentFragment extends Fragment {

    private RecyclerView rvStudentList;
    private EditText etSearchStudent;
    private Button btnSortStudent;
    private UserStudentAdapter adapter;

    private AuthApi authApi;
    private CartApi cartApi;
    private MyCourseApi myCourseApi;

    private List<StudentStats> allStudentStats = new ArrayList<>();
    private List<StudentStats> filteredStudentStats = new ArrayList<>();

    // Sorting state
    private enum SortType {
        NAME_AZ, NAME_ZA, TOTAL_SPENT_DESC, TOTAL_SPENT_ASC
    }
    private SortType currentSort = SortType.NAME_AZ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage_user_student, container, false);

        initAPIs();
        initViews(view);
        setupAdapter();
        setupListeners();
        loadStudentData();

        return view;
    }

    private void initAPIs() {
        authApi = ApiProvider.getAuthApi();
        cartApi = ApiProvider.getCartApi();
        myCourseApi = ApiProvider.getMyCourseApi();
    }

    private void initViews(View view) {
        rvStudentList = view.findViewById(R.id.rvStudentList);
        etSearchStudent = view.findViewById(R.id.etSearchStudent);
        btnSortStudent = view.findViewById(R.id.btnSortStudent);
    }

    private void setupAdapter() {
        adapter = new UserStudentAdapter(studentStats -> {
            // Chuyển tới activity chi tiết
            Intent intent = new Intent(getContext(), AdminManageUserStudentDetailActivity.class);
            intent.putExtra("userId", studentStats.getUser().getId());
            intent.putExtra("userName", studentStats.getUser().getName());
            startActivity(intent);
        });

        rvStudentList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStudentList.setAdapter(adapter);
    }

    private void setupListeners() {
        // Nút sort - hiển thị dialog chọn
        btnSortStudent.setOnClickListener(v -> showSortDialog());

        // Search/Filter theo tên
        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Load danh sách students và tính stats
     */
    private void loadStudentData() {
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====

                    List<StudentStats> result = new ArrayList<>();

                    List<User> students =
                            authApi.getAllUsersByRole(User.Role.STUDENT);

                    for (User student : students) {
                        String userId = student.getId();

                        // Courses đã mua
                        List<Course> purchasedCourses =
                                myCourseApi.getMyCoursesForUser(userId);

                        int coursesPurchased = purchasedCourses.size();

                        double totalSpent = 0;
                        for (Course c : purchasedCourses) {
                            totalSpent += c.getPrice();
                        }

                        // Cart
                        List<Course> cartCourses =
                                cartApi.getCartCoursesForUser(userId);

                        int cartItems = cartCourses.size();

                        result.add(
                                new StudentStats(
                                        student,
                                        totalSpent,
                                        coursesPurchased,
                                        cartItems
                                )
                        );
                    }

                    return result;
                },
                new AsyncApiHelper.ApiCallback<List<StudentStats>>() {
                    @Override
                    public void onSuccess(List<StudentStats> stats) {
                        // ===== MAIN THREAD =====

                        allStudentStats.clear();
                        allStudentStats.addAll(stats);

                        filteredStudentStats = new ArrayList<>(allStudentStats);
                        applySorting();
                        adapter.setStudents(filteredStudentStats);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }


    /**
     * Filter students theo tên
     */
    private void filterStudents(String query) {
        String lowerQuery = query.trim().toLowerCase(Locale.getDefault());

        if (lowerQuery.isEmpty()) {
            filteredStudentStats = new ArrayList<>(allStudentStats);
        } else {
            filteredStudentStats.clear();
            for (StudentStats stats : allStudentStats) {
                String name = stats.getUser().getName().toLowerCase(Locale.getDefault());
                String email = stats.getUser().getEmail().toLowerCase(Locale.getDefault());
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredStudentStats.add(stats);
                }
            }
        }

        applySorting();
        adapter.setStudents(filteredStudentStats);
    }

    /**
     * Hiển thị dialog chọn cách sắp xếp
     */
    private void showSortDialog() {
        String[] options = {
                "Tên A-Z (mặc định)",
                "Tên Z-A",
                "Tổng tiền ↓ (cao → thấp)",
                "Tổng tiền ↑ (thấp → cao)"
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
                            currentSort = SortType.TOTAL_SPENT_DESC;
                            break;
                        case 3:
                            currentSort = SortType.TOTAL_SPENT_ASC;
                            break;
                    }
                    applySorting();
                    adapter.setStudents(filteredStudentStats);
                })
                .show();
    }

    /**
     * Apply sorting lên filteredStudentStats
     */
    private void applySorting() {
        Comparator<StudentStats> comparator;

        switch (currentSort) {
            case NAME_ZA:
                comparator = (s1, s2) -> s2.getUser().getName().compareToIgnoreCase(s1.getUser().getName());
                break;
            case TOTAL_SPENT_DESC:
                comparator = (s1, s2) -> Double.compare(s2.getTotalSpent(), s1.getTotalSpent());
                break;
            case TOTAL_SPENT_ASC:
                comparator = (s1, s2) -> Double.compare(s1.getTotalSpent(), s2.getTotalSpent());
                break;
            case NAME_AZ:
            default:
                comparator = (s1, s2) -> s1.getUser().getName().compareToIgnoreCase(s2.getUser().getName());
                break;
        }

        Collections.sort(filteredStudentStats, comparator);
    }
}