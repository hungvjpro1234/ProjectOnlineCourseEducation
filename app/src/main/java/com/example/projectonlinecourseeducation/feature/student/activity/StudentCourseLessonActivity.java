package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.model.CourseReview;
import com.example.projectonlinecourseeducation.core.model.Lesson;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.review.ReviewApi;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductLessonInfoAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductCourseReviewDetailedAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

/**
 * Màn học bài – hiển thị chi tiết khóa học và danh sách bài học + reviews
 * Bao gồm: tiêu đề, ảnh, đánh giá, giáo viên, ngày cập nhật, nội dung (bài học), reviews, nút hỏi đáp (FAB)
 */
public class StudentCourseLessonActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private ImageView imgCourseBanner;
    private TextView tvCourseTitle, tvRatingValue, tvRatingCount, tvStudentsCount, tvTeacherName, tvUpdatedDate, tvLectureSummary;
    private RatingBar ratingBar;
    private RecyclerView rvLessons, rvReviews;
    private FloatingActionButton fabQAndA;

    // Adapters
    private ProductLessonInfoAdapter lessonAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // API
    private CourseApi courseApi;
    private LessonApi lessonApi;
    private ReviewApi reviewApi;

    // Data
    private String courseId;
    private String courseTitle;
    private Course currentCourse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_lesson);

        bindViews();
        setupRecyclerViews();

        // Initialize APIs
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        reviewApi = ApiProvider.getReviewApi();

        // Get intent data
        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");

        if (courseId == null) courseId = "c1";
        if (courseTitle == null) courseTitle = "Khóa học không xác định";

        loadCourseData(courseId);
        setupActions();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCourseBanner = findViewById(R.id.imgCourseBanner);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvUpdatedDate = findViewById(R.id.tvUpdatedDate);
        tvLectureSummary = findViewById(R.id.tvLectureSummary);
        ratingBar = findViewById(R.id.ratingBar);
        rvLessons = findViewById(R.id.rvLessons);
        rvReviews = findViewById(R.id.rvReviews);
        fabQAndA = findViewById(R.id.fabQAndA);
    }

    private void setupRecyclerViews() {
        // Lesson Adapter
        lessonAdapter = new ProductLessonInfoAdapter();
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
        rvLessons.setNestedScrollingEnabled(false);

        // Review Adapter
        reviewAdapter = new ProductCourseReviewDetailedAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);
    }

    /**
     * Load course data từ API và bind vào UI
     */
    private void loadCourseData(String id) {
        // Lấy chi tiết khóa học
        currentCourse = courseApi.getCourseDetail(id);
        if (currentCourse == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy danh sách bài học
        List<Lesson> lessons = lessonApi.getLessonsForCourse(id);

        // Lấy danh sách reviews
        List<CourseReview> reviews = reviewApi.getReviewsForCourse(id);

        // ===== Bind Course Information =====
        // Banner image
        ImageLoader.getInstance().display(
                currentCourse.getImageUrl(),
                imgCourseBanner,
                R.drawable.ic_image_placeholder
        );

        // Course title
        tvCourseTitle.setText(currentCourse.getTitle());

        // Rating
        float rating = (float) currentCourse.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        tvRatingCount.setText("(" + currentCourse.getRatingCount() + " đánh giá)");

        // Students count
        tvStudentsCount.setText("👥 " + currentCourse.getStudents() + " học viên");

        // Teacher name
        tvTeacherName.setText("👨‍🏫 " + currentCourse.getTeacher());

        // Updated date
        tvUpdatedDate.setText("📅 Cập nhật: " + currentCourse.getCreatedAt());

        // Lecture Summary (số bài + thời lượng)
        String time;
        if (currentCourse.getTotalDurationMinutes() >= 60) {
            int h = currentCourse.getTotalDurationMinutes() / 60;
            int m = currentCourse.getTotalDurationMinutes() % 60;
            time = h + " giờ " + (m > 0 ? m + " phút" : "");
        } else {
            time = currentCourse.getTotalDurationMinutes() + " phút";
        }
        tvLectureSummary.setText("📖 " + currentCourse.getLectures() + " bài • " + time);

        // ===== Bind Course Lessons =====
        lessonAdapter.submitList(lessons);

        // ===== Bind Reviews =====
        reviewAdapter.submitList(reviews);
    }

    private void setupActions() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Q&A FAB button
        fabQAndA.setOnClickListener(v -> {
            // Tạm thời chỉ hiển thị toast
            // Trong tương lai sẽ chuyển sang màn hỏi đáp
            Toast.makeText(this,
                    "Phần hỏi đáp đang được phát triển",
                    Toast.LENGTH_SHORT).show();
        });
    }
}