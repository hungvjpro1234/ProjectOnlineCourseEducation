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
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCoursePurchasedActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.MyCourseAdapter;

import java.util.List;

public class StudentMyCourseFragment extends Fragment {

    private MyCourseApi myCourseApi;
    private MyCourseAdapter adapter;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle s
    ) {
        rootView = inflater.inflate(R.layout.fragment_student_my_course, container, false);

        myCourseApi = ApiProvider.getMyCourseApi();

        RecyclerView rv = rootView.findViewById(R.id.rvMyCourses);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MyCourseAdapter();
        adapter.setMyCourseActionListener(new MyCourseAdapter.MyCourseActionListener() {
            @Override
            public void onItemClicked(Course course) {
                // không làm gì
            }

            @Override
            public void onLearnClicked(Course course) {
                if (course == null) return;
                Intent i = new Intent(requireContext(), StudentCoursePurchasedActivity.class);
                i.putExtra("course_id", course.getId());
                i.putExtra("course_title", course.getTitle());
                startActivity(i);
            }
        });

        rv.setAdapter(adapter);

        // 👉 LOAD DATA BẰNG ASYNC
        loadMyCourses();

        return rootView;
    }

    private void loadMyCourses() {
        AsyncApiHelper.execute(
                // chạy background
                () -> myCourseApi.getMyCourses(),

                // callback main thread
                new AsyncApiHelper.ApiCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> myCourses) {
                        if (!isAdded()) return;

                        if (myCourses == null || myCourses.isEmpty()) {
                            showEmptyView();
                            return;
                        }

                        adapter.submitList(myCourses);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        showEmptyView();
                    }
                }
        );
    }

    private void showEmptyView() {
        if (getView() == null) return;

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            int index = parent.indexOfChild(rootView);
            parent.removeView(rootView);
            LayoutInflater.from(requireContext())
                    .inflate(R.layout.fragment_student_my_course_empty, parent, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.dispose();
        }
    }
}
