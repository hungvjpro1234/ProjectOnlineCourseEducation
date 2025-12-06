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
 *
 * CHANGES:
 *  - Đăng ký LessonProgressUpdateListener & ReviewUpdateListener & CourseUpdateListener trong onStart() và hủy trong onStop().
 *  - Khi thêm review, không tự fetch lại reviews hay tính rating thủ công — chờ ReviewApi/ CourseApi notify.
 *  - Giữ UX: vẫn clear input và show toast ngay khi addReviewToCourse() thành công.
 */
public class StudentCoursePurchasedActivity extends AppCompatActivity {

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

    // Listeners
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressListener;
    private ReviewApi.ReviewUpdateListener reviewUpdateListener;
    private CourseApi.CourseUpdateListener courseUpdateListener;

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
    protected void onStart() {
        super.onStart();

        // LessonProgress listener (cập nhật progress từng bài)
        if (lessonProgressApi != null && lessonProgressListener == null) {
            lessonProgressListener = new LessonProgressApi.LessonProgressUpdateListener() {
                @Override
                public void onLessonProgressChanged(String lessonId) {
                    if (courseId == null) return;

                    boolean belongsToCurrentCourse = false;
                    if (lessonId == null || lessonId.isEmpty()) {
                        belongsToCurrentCourse = true;
                    } else if (lessonId.startsWith(courseId + "_")) {
                        belongsToCurrentCourse = true;
                    }

                    if (belongsToCurrentCourse) {
                        runOnUiThread(() -> {
                            List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
                            bindLessonsWithProgress(lessons);
                        });
                    }
                }
            };
            lessonProgressApi.addLessonProgressUpdateListener(lessonProgressListener);
        }

        // Review listener: reload reviews when there is change from backend/fake
        if (reviewApi != null && reviewUpdateListener == null) {
            reviewUpdateListener = new ReviewApi.ReviewUpdateListener() {
                @Override
                public void onReviewsChanged(String changedCourseId) {
                    if (changedCourseId == null || !changedCourseId.equals(courseId)) return;
                    runOnUiThread(() -> {
                        List<CourseReview> reviews = reviewApi.getReviewsForCourse(courseId);
                        reviewAdapter.submitList(reviews);
                    });
                }
            };
            reviewApi.addReviewUpdateListener(reviewUpdateListener);
        }

        // Course listener: update course meta (rating, student count, price, etc.) when backend notifies
        if (courseApi != null && courseUpdateListener == null) {
            courseUpdateListener = new CourseApi.CourseUpdateListener() {
                @Override
                public void onCourseUpdated(String id, Course updatedCourse) {
                    if (id == null || !id.equals(courseId)) return;
                    if (updatedCourse == null) return;
                    runOnUiThread(() -> {
                        currentCourse = updatedCourse;
                        // update rating & counts
                        float rating = (float) currentCourse.getRating();
                        ratingBar.setRating(rating);
                        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
                        tvRatingCount.setText("(" + currentCourse.getRatingCount() + " đánh giá)");

                        tvStudentsCount.setText("👥 " + currentCourse.getStudents() + " học viên");
                    });
                }
            };
            courseApi.addCourseUpdateListener(courseUpdateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký để tránh leak
        if (lessonProgressApi != null && lessonProgressListener != null) {
            lessonProgressApi.removeLessonProgressUpdateListener(lessonProgressListener);
            lessonProgressListener = null;
        }
        if (reviewApi != null && reviewUpdateListener != null) {
            reviewApi.removeReviewUpdateListener(reviewUpdateListener);
            reviewUpdateListener = null;
        }
        if (courseApi != null && courseUpdateListener != null) {
            courseApi.removeCourseUpdateListener(courseUpdateListener);
            courseUpdateListener = null;
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
        lessonAdapter = new LessonCardAdapter(this);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
        rvLessons.setNestedScrollingEnabled(false);

        reviewAdapter = new ProductCourseReviewDetailedAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);
    }

    private void loadCourseData(String id) {
        currentCourse = courseApi.getCourseDetail(id);
        if (currentCourse == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<Lesson> lessons = lessonApi.getLessonsForCourse(id);
        List<CourseReview> reviews = reviewApi.getReviewsForCourse(id);

        ImageLoader.getInstance().display(
                currentCourse.getImageUrl(),
                imgCourseBanner,
                R.drawable.ic_image_placeholder
        );

        tvCourseTitle.setText(currentCourse.getTitle());

        float rating = (float) currentCourse.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        tvRatingCount.setText("(" + currentCourse.getRatingCount() + " đánh giá)");

        tvStudentsCount.setText("👥 " + currentCourse.getStudents() + " học viên");
        tvTeacherName.setText("👨‍🏫 " + currentCourse.getTeacher());
        tvUpdatedDate.setText("📅 Cập nhật: " + currentCourse.getCreatedAt());

        String time;
        if (currentCourse.getTotalDurationMinutes() >= 60) {
            int h = currentCourse.getTotalDurationMinutes() / 60;
            int m = currentCourse.getTotalDurationMinutes() % 60;
            time = h + " giờ " + (m > 0 ? m + " phút" : "");
        } else {
            time = currentCourse.getTotalDurationMinutes() + " phút";
        }
        tvLectureSummary.setText("📖 " + currentCourse.getLectures() + " bài • " + time);

        bindLessonsWithProgress(lessons);

        reviewAdapter.submitList(reviews);
    }

    private void bindLessonsWithProgress(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            lessonAdapter.submitItems(null);
            return;
        }

        List<LessonCardAdapter.LessonItemUiModel> items = new ArrayList<>();

        boolean allPreviousCompleted = true;

        for (Lesson lesson : lessons) {
            LessonProgress progress = lessonProgressApi.getLessonProgress(lesson.getId());

            int percent = 0;
            boolean completed = false;

            if (progress != null) {
                percent = progress.getCompletionPercentage();
                completed = progress.isCompleted();
            }

            boolean isLocked = !allPreviousCompleted;

            items.add(new LessonCardAdapter.LessonItemUiModel(
                    lesson,
                    percent,
                    isLocked
            ));

            allPreviousCompleted = allPreviousCompleted && completed;
        }

        lessonAdapter.submitItems(items);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        fabQAndA.setOnClickListener(v -> Toast.makeText(this, "Phần hỏi đáp đang được phát triển", Toast.LENGTH_SHORT).show());

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

            String studentName = "Học viên";
            try {
                com.example.projectonlinecourseeducation.core.model.user.User currentUser =
                        ApiProvider.getAuthApi().getCurrentUser();
                if (currentUser != null && currentUser.getName() != null) {
                    studentName = currentUser.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Gọi API để lưu review (không fetch lại thủ công sau khi thành công)
            CourseReview newReview = reviewApi.addReviewToCourse(
                    courseId,
                    studentName,
                    rating,
                    comment
            );

            if (newReview != null) {
                // UX: clear inputs + show toast. Actual list & course rating will be updated
                // by ReviewApi/ CourseApi listeners when backend/fake notify.
                ratingBarUserInput.setRating(0);
                etCommentInput.setText("");

                Toast.makeText(this,
                        "Đánh giá " + (int) rating + " sao đã được gửi thành công!",
                        Toast.LENGTH_SHORT).show();

                // OPTIONAL: if you want optimistic update, you could append to adapter here,
                // but to avoid duplication we rely on the listener notify path.

            } else {
                Toast.makeText(this, "Lỗi khi gửi đánh giá. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
