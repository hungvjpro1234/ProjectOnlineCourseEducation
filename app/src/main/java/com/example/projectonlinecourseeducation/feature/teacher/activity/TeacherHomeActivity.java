// app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/TeacherHomeActivity.java
package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectonlinecourseeducation.R;

public class TeacherHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_home);
    }
}
