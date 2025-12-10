package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminPreviewLessonAdapter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity xem preview khóa học mới (chưa được duyệt)
 * Admin có thể xem đầy đủ thông tin và play video từng lesson
 */
public class AdminCoursePreviewActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgCourse;
    private TextView tvTitle, tvTeacher, tvCategory, tvPrice;
    private TextView tvLectures, tvDuration, tvCreatedAt;
    private TextView tvDescription;
    private LinearLayout skillsContainer, requirementsContainer;
    private RecyclerView rvLessons;
    private TextView tvNoLessons;

    private CourseApi courseApi;
    private LessonApi lessonApi;
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    private String courseId;
    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_course_preview);

        initViews();
        initApis();
        setupListeners();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            loadCourseData();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy courseId", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCourse = findViewById(R.id.imgCourse);
        tvTitle = findViewById(R.id.tvTitle);
        tvTeacher = findViewById(R.id.tvTeacher);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvLectures = findViewById(R.id.tvLectures);
        tvDuration = findViewById(R.id.tvDuration);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvDescription = findViewById(R.id.tvDescription);
        skillsContainer = findViewById(R.id.skillsContainer);
        requirementsContainer = findViewById(R.id.requirementsContainer);
        rvLessons = findViewById(R.id.rvLessons);
        tvNoLessons = findViewById(R.id.tvNoLessons);
    }

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCourseData() {
        bgExecutor.execute(() -> {
            try {
                course = courseApi.getCourseDetail(courseId);

                if (course == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);

                runOnUiThread(() -> {
                    displayCourseInfo(course);
                    displayLessons(lessons);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayCourseInfo(Course course) {
        // Image
        ImageLoader.getInstance().display(
                course.getImageUrl(),
                imgCourse,
                R.drawable.ic_image_placeholder
        );

        // Basic info
        tvTitle.setText(course.getTitle());
        tvTeacher.setText("👨‍🏫 " + course.getTeacher());
        tvCategory.setText("📚 " + course.getCategory());

        // Price
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            tvPrice.setText(df.format((long) course.getPrice()) + " VNĐ");
        } catch (Exception e) {
            tvPrice.setText(String.format("%.0f VNĐ", course.getPrice()));
        }

        // Stats
        tvLectures.setText(course.getLectures() + " bài học");
        tvDuration.setText(formatDuration(course.getTotalDurationMinutes()));
        tvCreatedAt.setText("Tạo: " + course.getCreatedAt());

        // Description
        tvDescription.setText(course.getDescription());

        // Skills
        displaySkills(course.getSkills());

        // Requirements
        displayRequirements(course.getRequirements());
    }

    private void displaySkills(List<String> skills) {
        skillsContainer.removeAllViews();

        if (skills == null || skills.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có kỹ năng");
            tvEmpty.setTextColor(0xFF9E9E9E);
            tvEmpty.setTextSize(14);
            skillsContainer.addView(tvEmpty);
            return;
        }

        for (String skill : skills) {
            TextView tv = new TextView(this);
            tv.setText("✓ " + skill);
            tv.setTextSize(14);
            tv.setTextColor(0xFF212121);
            tv.setPadding(0, 8, 0, 8);
            skillsContainer.addView(tv);
        }
    }

    private void displayRequirements(List<String> requirements) {
        requirementsContainer.removeAllViews();

        if (requirements == null || requirements.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có yêu cầu");
            tvEmpty.setTextColor(0xFF9E9E9E);
            tvEmpty.setTextSize(14);
            requirementsContainer.addView(tvEmpty);
            return;
        }

        for (String req : requirements) {
            TextView tv = new TextView(this);
            tv.setText("• " + req);
            tv.setTextSize(14);
            tv.setTextColor(0xFF212121);
            tv.setPadding(0, 8, 0, 8);
            requirementsContainer.addView(tv);
        }
    }

    private void displayLessons(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            rvLessons.setVisibility(View.GONE);
            tvNoLessons.setVisibility(View.VISIBLE);
            return;
        }

        rvLessons.setVisibility(View.VISIBLE);
        tvNoLessons.setVisibility(View.GONE);

        AdminPreviewLessonAdapter adapter = new AdminPreviewLessonAdapter(lesson -> {
            // Click lesson -> open video preview
            Intent intent = new Intent(this, AdminLessonVideoPreviewActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("course_title", course.getTitle());
            startActivity(intent);
        });

        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(adapter);
        adapter.setLessons(lessons);
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " phút";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours + " giờ " + mins + " phút";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}