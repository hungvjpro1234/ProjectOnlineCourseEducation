package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.auth.activity.MainActivity2;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentCartFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentHomeFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentMyCourseFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentNotificationFragment;
import com.example.projectonlinecourseeducation.feature.student.fragment.StudentUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentHomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private Button btnLogout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);

        // TODO: lấy tên user thực tế, tạm thời demo:
        tvGreeting.setText("Xin chào, Student");

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                f = new StudentHomeFragment();
            } else if (id == R.id.nav_cart) {
                f = new StudentCartFragment();
            } else if (id == R.id.nav_mycourse) {
                f = new StudentMyCourseFragment();
            } else if (id == R.id.nav_notification) {
                f = new StudentNotificationFragment();
            } else { // R.id.nav_user
                f = new StudentUserFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.student_fragment_container, f)
                    .commit();
            return true;
        });

        // mặc định mở Home
        // Nếu được truyền flag open_cart từ StudentCourseDetailActivity thì mở tab Giỏ hàng
        boolean openCart = getIntent().getBooleanExtra("open_cart", false);
        bottomNav.setSelectedItemId(openCart ? R.id.nav_cart : R.id.nav_home);
    }
}
