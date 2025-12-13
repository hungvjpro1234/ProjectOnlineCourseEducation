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
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageUserTeacherDetailActivity;
import com.example.projectonlinecourseeducation.feature.admin.adapter.UserTeacherAdapter;
import com.example.projectonlinecourseeducation.feature.admin.model.TeacherStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminUserManagementTeacherFragment extends Fragment {

    private RecyclerView rvTeacherList;
    private EditText etSearchTeacher;
    private Button btnSortTeacher;
    private UserTeacherAdapter adapter;

    private AuthApi authApi;
    private CourseApi courseApi;

    private List<TeacherStats> allTeacherStats = new ArrayList<>();
    private List<TeacherStats> filteredTeacherStats = new ArrayList<>();

    private enum SortType {
        NAME_AZ,
        NAME_ZA,
        TOTAL_REVENUE_DESC,
        TOTAL_REVENUE_ASC
    }

    private SortType currentSort = SortType.NAME_AZ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage_user_teacher, container, false);

        authApi = ApiProvider.getAuthApi();
        courseApi = ApiProvider.getCourseApi();

        rvTeacherList = view.findViewById(R.id.rvTeacherList);
        etSearchTeacher = view.findViewById(R.id.etSearchTeacher);
        btnSortTeacher = view.findViewById(R.id.btnSortTeacher);

        adapter = new UserTeacherAdapter(stats -> {
            Intent intent = new Intent(getContext(), AdminManageUserTeacherDetailActivity.class);
            intent.putExtra("userId", stats.getUser().getId());
            intent.putExtra("userName", stats.getUser().getName());
            startActivity(intent);
        });

        rvTeacherList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeacherList.setAdapter(adapter);

        btnSortTeacher.setOnClickListener(v -> showSortDialog());

        etSearchTeacher.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTeachers(s.toString());
            }
        });

        loadTeacherData();
        return view;
    }

    private void loadTeacherData() {
        List<User> teachers = authApi.getAllUsersByRole(User.Role.TEACHER);
        List<Course> allCourses = courseApi.listAll();

        allTeacherStats.clear();

        for (User teacher : teachers) {
            List<Course> teacherCourses = new ArrayList<>();
            for (Course c : allCourses) {
                if (c.getTeacher() != null && c.getTeacher().equals(teacher.getName())) {
                    teacherCourses.add(c);
                }
            }

            int totalCourses = teacherCourses.size();
            double totalRevenue = 0;
            for (Course c : teacherCourses) {
                totalRevenue += c.getPrice() * c.getStudents();
            }

            allTeacherStats.add(new TeacherStats(teacher, totalCourses, totalRevenue));
        }

        filteredTeacherStats = new ArrayList<>(allTeacherStats);
        applySorting();
        adapter.setTeachers(filteredTeacherStats);
    }

    private void filterTeachers(String query) {
        String q = query.toLowerCase(Locale.getDefault()).trim();
        filteredTeacherStats.clear();

        if (q.isEmpty()) {
            filteredTeacherStats.addAll(allTeacherStats);
        } else {
            for (TeacherStats s : allTeacherStats) {
                if (s.getUser().getName().toLowerCase().contains(q)
                        || s.getUser().getEmail().toLowerCase().contains(q)) {
                    filteredTeacherStats.add(s);
                }
            }
        }

        applySorting();
        adapter.setTeachers(filteredTeacherStats);
    }

    private void showSortDialog() {
        String[] options = {
                "Tên A-Z",
                "Tên Z-A",
                "Tổng thu nhập ↓",
                "Tổng thu nhập ↑"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp theo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSort = SortType.NAME_AZ; break;
                        case 1: currentSort = SortType.NAME_ZA; break;
                        case 2: currentSort = SortType.TOTAL_REVENUE_DESC; break;
                        case 3: currentSort = SortType.TOTAL_REVENUE_ASC; break;
                    }
                    applySorting();
                    adapter.setTeachers(filteredTeacherStats);
                })
                .show();
    }

    private void applySorting() {
        Comparator<TeacherStats> comparator;

        switch (currentSort) {
            case NAME_ZA:
                comparator = (a, b) -> b.getUser().getName().compareToIgnoreCase(a.getUser().getName());
                break;
            case TOTAL_REVENUE_DESC:
                comparator = (a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue());
                break;
            case TOTAL_REVENUE_ASC:
                comparator = (a, b) -> Double.compare(a.getTotalRevenue(), b.getTotalRevenue());
                break;
            case NAME_AZ:
            default:
                comparator = (a, b) -> a.getUser().getName().compareToIgnoreCase(b.getUser().getName());
        }

        Collections.sort(filteredTeacherStats, comparator);
    }
}
