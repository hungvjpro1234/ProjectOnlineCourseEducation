package com.example.projectonlinecourseeducation.feature.student.fragment;

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
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCartActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.StudentCartAdapter;

import java.util.List;

public class StudentCartFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        if (StudentCartActivity.getInstance().getCartCourses().isEmpty()) {
            // Giỏ hàng trống, inflate layout trống
            return inflater.inflate(R.layout.fragment_student_cart_empty, container, false);
        } else {
            // Có hàng, inflate layout có hàng
            View view = inflater.inflate(R.layout.fragment_student_cart, container, false);

            RecyclerView recyclerView = view.findViewById(R.id.rvCartCourses);
            List<Course> cartList = StudentCartActivity.getInstance().getCartCourses();
            StudentCartAdapter cartAdapter = new StudentCartAdapter(cartList);
            recyclerView.setAdapter(cartAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


            return view;
        }
    }
}
