package com.example.projectonlinecourseeducation.feature.teacher.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherCourseEditActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherCourseCreateActivity;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.HomeCourseAdapter;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TeacherHomeFragment extends Fragment {

    private RecyclerView rvCourses;
    private FloatingActionButton fabCreateCourse;
    private HomeCourseAdapter adapter;
    private CourseApi courseApi;
    private AuthApi authApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_home, container, false);

        bindViews(view);
        setupAPIs();
        setupRecyclerView();
        setupActions();
        loadCourses();

        return view;
    }

    private void bindViews(View view) {
        rvCourses = view.findViewById(R.id.rvCourses);
        fabCreateCourse = view.findViewById(R.id.fabCreateCourse);
    }

    private void setupAPIs() {
        courseApi = ApiProvider.getCourseApi();
        authApi = ApiProvider.getAuthApi();
    }

    private void setupRecyclerView() {
        adapter = new HomeCourseAdapter();
        adapter.setOnCourseActionListener(new HomeCourseAdapter.OnCourseActionListener() {
            @Override
            public void onEditCourse(Course course) {
                // Chuyển tới activity chỉnh sửa khóa học
                Intent intent = new Intent(getActivity(), TeacherCourseEditActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteCourse(Course course) {
                confirmDeleteCourse(course);
            }
        });

        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCourses.setAdapter(adapter);
    }

    private void setupActions() {
        fabCreateCourse.setOnClickListener(v -> {
            // Chuyển tới activity tạo khóa học mới
            Intent intent = new Intent(getActivity(), TeacherCourseCreateActivity.class);
            startActivity(intent);
        });
    }

    private void loadCourses() {
        User currentUser = authApi.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Không tìm thấy user", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách khóa học do teacher này tạo
        List<Course> courses = courseApi.getCoursesByTeacher(currentUser.getName());
        adapter.submitList(courses);
    }

    private void confirmDeleteCourse(Course course) {
        if (getContext() == null) return;

        DialogConfirmHelper.showConfirmDialog(
                getContext(),
                "Xóa khóa học",
                "Bạn chắc chắn muốn xóa khóa học \"" + course.getTitle() + "\"?",
                R.drawable.delete,     // icon delete.png của bạn
                "Xóa",
                "Hủy",
                R.color.error_red,    // màu nút Xóa
                () -> {
                    courseApi.deleteCourse(course.getId());
                    Toast.makeText(getContext(), "Xóa khóa học thành công", Toast.LENGTH_SHORT).show();
                    loadCourses();
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload courses khi quay lại fragment (sau khi edit)
        loadCourses();
    }
}