// File: TeacherManagementFragment.java
package com.example.projectonlinecourseeducation.feature.teacher.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherCourseManagementActivity;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementAdapter;

import java.util.ArrayList;
import java.util.List;

public class TeacherManagementFragment extends Fragment {

    private RecyclerView rvTeacherCourses;
    private ManagementAdapter adapter;
    private CourseApi courseApi;

    // listener để lắng nghe thay đổi course từ FakeApi/RemoteApi
    private final CourseApi.CourseUpdateListener courseUpdateListener = new CourseApi.CourseUpdateListener() {
        @Override
        public void onCourseUpdated(String courseId, Course updatedCourse) {
            // course may be updated (updatedCourse != null) or deleted (updatedCourse == null)
            if (!isAdded()) return; // fragment not attached
            requireActivity().runOnUiThread(() -> {
                if (updatedCourse == null) {
                    // deleted
                    adapter.removeCourseById(courseId);
                } else {
                    // updated or new
                    adapter.updateOrInsertCourse(updatedCourse);
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_management, container, false);

        initViews(view);
        setupAdapter();
        loadTeacherCourses();

        return view;
    }

    private void initViews(View view) {
        rvTeacherCourses = view.findViewById(R.id.rvTeacherCourses);
    }

    private void setupAdapter() {
        adapter = new ManagementAdapter(new ManagementAdapter.OnCourseActionListener() {
            @Override
            public void onCourseClick(Course course) {
                navigateToCourseDetail(course);
            }
        });

        rvTeacherCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeacherCourses.setAdapter(adapter);
    }

    private void loadTeacherCourses() {
        courseApi = ApiProvider.getCourseApi();

        if (courseApi == null) {
            adapter.setCourses(new ArrayList<>());
            return;
        }

        // FIX: Get current teacher's name
        String teacherName = null;

        if (getArguments() != null) {
            teacherName = getArguments().getString("teacher_name", null);
        }

        if (teacherName == null) {
            com.example.projectonlinecourseeducation.core.model.user.User currentUser =
                    ApiProvider.getAuthApi() != null
                            ? ApiProvider.getAuthApi().getCurrentUser()
                            : null;
            if (currentUser != null) {
                teacherName = currentUser.getName();
            } else {
                teacherName = "";
            }
        }

        final String finalTeacherName = teacherName;

        // ✅ BỌC ASYNCAPHELPER TẠI ĐÂY
        com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper.execute(
                () -> courseApi.getCoursesByTeacher(finalTeacherName),
                new com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper.ApiCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> result) {
                        if (!isAdded()) return;

                        if (result == null) result = new ArrayList<>();
                        adapter.setCourses(result);

                        // Register listener sau khi load thành công
                        courseApi.addCourseUpdateListener(courseUpdateListener);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        adapter.setCourses(new ArrayList<>());
                    }
                }
        );
    }

    private void navigateToCourseDetail(Course course) {
        Intent intent = new Intent(requireActivity(), TeacherCourseManagementActivity.class);
        intent.putExtra("course_id", course.getId());
        requireActivity().startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy đăng ký listener để tránh leak
        if (courseApi != null) courseApi.removeCourseUpdateListener(courseUpdateListener);
    }
}
