package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;

/**
 * Màn học bài – hiện tại chỉ demo:
 * Hiển thị tên khóa học.
 */
public class StudentCourseLessonActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDemo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_lesson);

        btnBack = findViewById(R.id.btnBack);
        tvDemo = findViewById(R.id.tvDemo);

        String title = getIntent().getStringExtra("course_title");
        if (title == null) title = "Khóa học không xác định";

        tvDemo.setText("Đây là khóa học: " + title);

        btnBack.setOnClickListener(v -> finish());
    }
}
