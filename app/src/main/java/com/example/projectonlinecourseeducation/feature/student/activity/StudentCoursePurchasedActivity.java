package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.coursereview.ReviewApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
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
 *  - Thêm course-level progress bar tổng hợp từ LessonProgress
 *  - Dùng LessonProgressApi để lấy progress từng bài (single source of truth)
 *  - Khi listener notify, UI được cập nhật thông qua bindLessonsWithProgress() -> updateCourseProgress()
 *  - Bổ sung hasQuiz flag cho LessonItemUiModel (adapter sẽ hiển thị nút "Làm quiz")
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

    // NEW: Course-level progress views
    private ProgressBar progressCourseBar;
    private TextView tvCourseProgressPercent;

    // Adapters
    private LessonCardAdapter lessonAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // API (đều lấy qua ApiProvider – không phụ thuộc Fake hay Remote)
    private CourseApi courseApi;
    private LessonApi lessonApi;
    private ReviewApi reviewApi;
    private LessonProgressApi lessonProgressApi;
    private LessonQuizApi lessonQuizApi; // NEW: để kiểm tra hasQuiz per lesson

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
        setContentView(R.layout.activity_student_course_purchased);

        bindViews();
        setupRecyclerViews();

        // Initialize APIs từ ApiProvider
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        reviewApi = ApiProvider.getReviewApi();
        lessonProgressApi = ApiProvider.getLessonProgressApi();
        lessonQuizApi = ApiProvider.getLessonQuizApi(); // NEW

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

            // === NEW: initial sync immediately after re-registering listener ===
            // This ensures we pick up any notifications/changes that happened while this Activity was stopped.
            // We call the listener handler with null so it will refresh course lessons/progress.
            lessonProgressListener.onLessonProgressChanged(null);
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

        // NEW: course progress views
        progressCourseBar = findViewById(R.id.progressCourseBar);
        tvCourseProgressPercent = findViewById(R.id.tvCourseProgressPercent);
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

        // NEW: update course progress now (bindLessonsWithProgress also calls it)
        updateCourseProgress(lessons);

        reviewAdapter.submitList(reviews);
    }

    /**
     * Bind lessons -> build UI models with per-student progress + quiz state.
     *
     * Fix: ensure unlocking is strictly sequential:
     *  - To unlock lesson N+1, lesson N must have video completed AND (if lesson N has quiz) quiz must be passed.
     *
     * This method now:
     *  - queries LessonProgress per student
     *  - queries LessonQuizApi to detect hasQuiz and attempts to see if passed
     *  - passes completed + quizPassed into LessonItemUiModel so adapter can show correct buttons
     */
    private void bindLessonsWithProgress(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            lessonAdapter.submitItems(null);
            // also set course progress to 0
            updateCourseProgress(null);
            return;
        }

        List<LessonCardAdapter.LessonItemUiModel> items = new ArrayList<>();

        // allPreviousUnlocked: true nếu tất cả lesson trước đó đã hoàn thành *và* (nếu có quiz) đã pass
        boolean allPreviousUnlocked = true;

        // Lấy current user để query progress per-student
        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        String studentId = currentUser != null ? currentUser.getId() : null;

        for (Lesson lesson : lessons) {
            // Lấy progress per-student
            LessonProgress progress = null;
            try {
                progress = lessonProgressApi.getLessonProgress(lesson.getId(), studentId);
            } catch (Exception ignored) {}

            int percent = 0;
            boolean completed = false;

            if (progress != null) {
                percent = progress.getCompletionPercentage();
                completed = progress.isCompleted();
            }

            // Check if lesson has quiz
            boolean hasQuiz = false;
            boolean quizPassed = false;
            try {
                if (lessonQuizApi != null) {
                    hasQuiz = lessonQuizApi.getQuizForLesson(lesson.getId()) != null;
                    if (hasQuiz) {
                        // check attempts for this student; if any attempt.passed == true => quizPassed
                        List<com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt> attempts =
                                lessonQuizApi.getAttemptsForLesson(lesson.getId(), studentId);
                        if (attempts != null) {
                            for (com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt a : attempts) {
                                if (a != null && a.isPassed()) {
                                    quizPassed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) { }

            // determine lock state for THIS lesson:
            // unlocked only when all previous lessons are unlocked
            boolean isLocked = !allPreviousUnlocked;

            // create UI model with completed + quizPassed so adapter can enable quiz button correctly
            items.add(new LessonCardAdapter.LessonItemUiModel(
                    lesson,
                    percent,
                    isLocked,
                    hasQuiz,
                    completed,
                    quizPassed
            ));

            // Update allPreviousUnlocked for next lesson:
            // current lesson counts as "unlocked" for the next one only if:
            // - video completed AND
            // - if it has a quiz, quizPassed must be true
            boolean thisLessonUnlocksNext = completed && (!hasQuiz || quizPassed);
            allPreviousUnlocked = allPreviousUnlocked && thisLessonUnlocksNext;
        }

        lessonAdapter.submitItems(items);

        // Update tổng tiến độ khi lessons + progress đã được bind
        updateCourseProgress(lessons);
    }

    /**
     * Tính tổng tiến độ khóa học dựa trên lesson progress.
     * - Nếu mọi lesson có totalSecond > 0 => weighted-by-duration
     * - Ngược lại => dùng trung bình completionPercentage
     *
     * FIXED: Truyền studentId để lấy đúng progress của current user
     */
    private void updateCourseProgress(List<Lesson> lessons) {
        runOnUiThread(() -> {
            if (lessons == null || lessons.isEmpty()) {
                progressCourseBar.setProgress(0);
                tvCourseProgressPercent.setText("0%");
                return;
            }

            // Lấy current user để query progress per-student
            User currentUser = SessionManager.getInstance(this).getCurrentUser();
            String studentId = currentUser != null ? currentUser.getId() : null;

            boolean allHaveDuration = true;
            for (Lesson l : lessons) {
                LessonProgress p = lessonProgressApi.getLessonProgress(l.getId(), studentId);
                if (p == null || p.getTotalSecond() <= 0f) {
                    allHaveDuration = false;
                    break;
                }
            }

            int percent = 0;

            if (allHaveDuration) {
                // weighted by duration
                double totalSecondsSum = 0.0;
                double watchedSecondsSum = 0.0;
                for (Lesson l : lessons) {
                    LessonProgress p = lessonProgressApi.getLessonProgress(l.getId(), studentId);
                    if (p != null) {
                        double t = p.getTotalSecond();
                        double c = Math.min(p.getCurrentSecond(), t);
                        totalSecondsSum += t;
                        watchedSecondsSum += c;
                    }
                }
                if (totalSecondsSum > 0) {
                    percent = (int) Math.round((watchedSecondsSum / totalSecondsSum) * 100.0);
                } else {
                    percent = 0;
                }
            } else {
                // fallback: average of completionPercentage
                int sumPerc = 0;
                int count = 0;
                for (Lesson l : lessons) {
                    LessonProgress p = lessonProgressApi.getLessonProgress(l.getId(), studentId);
                    int cp = 0;
                    if (p != null) cp = p.getCompletionPercentage();
                    sumPerc += cp;
                    count++;
                }
                if (count > 0) {
                    percent = Math.round((float) sumPerc / (float) count);
                } else {
                    percent = 0;
                }
            }

            percent = Math.max(0, Math.min(100, percent));
            progressCourseBar.setProgress(percent);
            tvCourseProgressPercent.setText(percent + "%");
        });
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
