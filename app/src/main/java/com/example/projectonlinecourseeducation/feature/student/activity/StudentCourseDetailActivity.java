// app/src/main/java/com/example/projectonlinecourseeducation/feature/student/activity/StudentCourseDetailActivity.java
package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;

public class StudentCourseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_detail);

        TextView tv = findViewById(R.id.tvDetailTitle);
        String title = getIntent().getStringExtra("course_title");
        if (title == null) title = "";
        tv.setText("Đây là khóa học: " + title);
    }
}
