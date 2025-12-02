package com.example.projectonlinecourseeducation.feature.student.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCourseLessonActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.MyCourseAdapter;

import java.util.List;

public class StudentMyCourseFragment extends Fragment {

    private MyCourseApi myCourseApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        myCourseApi = ApiProvider.getMyCourseApi();

        List<Course> myCourses = myCourseApi.getMyCourses();
        if (myCourses == null || myCourses.isEmpty()) {
            // Trang My Course rỗng
            return inflater.inflate(R.layout.fragment_student_my_course_empty, container, false);
        }

        View view = inflater.inflate(R.layout.fragment_student_my_course, container, false);
        RecyclerView rv = view.findViewById(R.id.rvMyCourses);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        MyCourseAdapter adapter = new MyCourseAdapter();
        adapter.submitList(myCourses);
        adapter.setMyCourseActionListener(new MyCourseAdapter.MyCourseActionListener() {
            @Override
            public void onItemClicked(Course course) {
                // Bỏ sự kiện click vào item, không làm gì cả
            }

            @Override
            public void onLearnClicked(Course course) {
                if (course == null) return;
                // 👉 Học ngay: chuyển sang màn danh sách Lesson của khóa học
                Intent i = new Intent(requireContext(), StudentCourseLessonActivity.class);
                i.putExtra("course_id", course.getId());
                i.putExtra("course_title", course.getTitle());
                startActivity(i);
            }
        });

        rv.setAdapter(adapter);
        return view;
    }
}