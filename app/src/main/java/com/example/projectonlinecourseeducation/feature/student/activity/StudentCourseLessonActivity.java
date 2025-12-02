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
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.review.ReviewApi;
import com.example.projectonlinecourseeducation.feature.student.adapter.LessonCardAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductCourseReviewDetailedAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn học bài – hiển thị chi tiết khóa học và danh sách bài học + reviews
 * Bao gồm: tiêu đề, ảnh, đánh giá, giáo viên, ngày cập nhật, nội dung (bài học), reviews, nút hỏi đáp (FAB)
 *
 * BỔ SUNG:
 *  - Bind thêm LessonProgress cho từng bài học (Fake API / Backend) thông qua LessonProgressApi.
 *  - Rule khóa bài: chỉ cho phép học bài i nếu tất cả bài trước đó đã hoàn thành (>= 90%).
 *  - Danh sách bài học dùng StudentLessonCardAdapter hiển thị thanh progress + % hoàn thành.
 *
 * LƯU Ý:
 *  - UI CHỈ gọi qua CourseApi, LessonApi, LessonProgressApi, ReviewApi lấy từ ApiProvider.
 *  - Sau này cắm backend thật chỉ cần set ApiProvider.setXxxApi(...) mà KHÔNG sửa UI.
 */
public class StudentCourseLessonActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private ImageView imgCourseBanner;
    private TextView tvCourseTitle, tvRatingValue, tvRatingCount, tvStudentsCount, tvTeacherName, tvUpdatedDate, tvLectureSummary;
    private RatingBar ratingBar, ratingBarUserInput;
    private RecyclerView rvLessons, rvReviews;
    private FloatingActionButton fabQAndA;
    private TextInputEditText etCommentInput;
    private MaterialButton btnSubmitRating;

    // Adapters
    private LessonCardAdapter lessonAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // API (đều lấy qua ApiProvider – không phụ thuộc Fake hay Remote)
    private CourseApi courseApi;
    private LessonApi lessonApi;
    private ReviewApi reviewApi;
    private LessonProgressApi lessonProgressApi;

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

        // Initialize APIs từ ApiProvider
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        reviewApi = ApiProvider.getReviewApi();
        lessonProgressApi = ApiProvider.getLessonProgressApi();

        // Get intent data
        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");

        if (courseId == null) courseId = "c1";
        if (courseTitle == null) courseTitle = "Khóa học không xác định";

        // Lần đầu vào: load info khóa học + lessons + reviews
        loadCourseData(courseId);
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần quay lại màn (từ LessonVideo back) sẽ REFRESH lại progress + trạng thái khóa bài
        if (courseId != null) {
            List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
            bindLessonsWithProgress(lessons);
        }
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
        ratingBarUserInput = findViewById(R.id.ratingBarUserInput);
        rvLessons = findViewById(R.id.rvLessons);
        rvReviews = findViewById(R.id.rvReviews);
        fabQAndA = findViewById(R.id.fabQAndA);
        etCommentInput = findViewById(R.id.etCommentInput);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
    }

    private void setupRecyclerViews() {
        // Lesson Adapter - dùng StudentLessonCardAdapter (có progress + khóa bài)
        lessonAdapter = new LessonCardAdapter(this);
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
     * Lần đầu vào màn hình: load course detail, lesson list, review list.
     * Progress & trạng thái khóa bài được bind thông qua bindLessonsWithProgress(...).
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

        // ===== Bind Course Lessons + Progress + Rule khóa bài =====
        bindLessonsWithProgress(lessons);

        // ===== Bind Reviews =====
        reviewAdapter.submitList(reviews);
    }

    /**
     * Bind danh sách bài học kèm progress & trạng thái khóa/mở.
     *
     * Rule:
     *  - Bài 1 luôn mở.
     *  - Bài i (i > 1) chỉ mở nếu TẤT CẢ các bài trước đó đã isCompleted (>= 90%).
     *
     * Dữ liệu progress hiện tại được lấy từ LessonProgressApi
     * (FakeApi hiện tại, sau này backend thật cũng implement interface này).
     */
    private void bindLessonsWithProgress(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            lessonAdapter.submitItems(null);
            return;
        }

        List<LessonCardAdapter.LessonItemUiModel> items = new ArrayList<>();

        boolean allPreviousCompleted = true; // trạng thái các bài trước

        for (Lesson lesson : lessons) {
            LessonProgress progress = lessonProgressApi.getLessonProgress(lesson.getId());

            int percent = 0;
            boolean completed = false;

            if (progress != null) {
                percent = progress.getCompletionPercentage();
                completed = progress.isCompleted();
            }

            // Bài hiện tại bị khóa nếu có ÍT NHẤT 1 bài trước đó chưa completed
            boolean isLocked = !allPreviousCompleted;

            items.add(new LessonCardAdapter.LessonItemUiModel(
                    lesson,
                    percent,
                    isLocked
            ));

            // Cập nhật trạng thái cho bài tiếp theo
            allPreviousCompleted = allPreviousCompleted && completed;
        }

        lessonAdapter.submitItems(items);
    }

    private void setupActions() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Q&A FAB button
        fabQAndA.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Phần hỏi đáp đang được phát triển",
                    Toast.LENGTH_SHORT).show();
        });

        // Submit Rating button
        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBarUserInput.getRating();
            String comment = etCommentInput.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao để đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            // Demo: Gửi đánh giá thành công (Fake)
            Toast.makeText(this,
                    "Đánh giá " + (int) rating + " sao đã được gửi thành công!",
                    Toast.LENGTH_SHORT).show();

            // Clear inputs
            ratingBarUserInput.setRating(0);
            etCommentInput.setText("");
        });
    }
}
