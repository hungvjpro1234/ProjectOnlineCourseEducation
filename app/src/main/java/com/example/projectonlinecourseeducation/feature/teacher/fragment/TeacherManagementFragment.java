package com.example.projectonlinecourseeducation.feature.teacher.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectonlinecourseeducation.R;

public class TeacherManagementFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_management, container, false);

        TextView tvPlaceholder = view.findViewById(R.id.tvPlaceholder);
        tvPlaceholder.setText("Đây là Teacher Management Fragment");

        return view;
    }
}