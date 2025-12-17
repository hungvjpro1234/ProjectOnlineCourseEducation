package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
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
 *  - Sửa hành vi back: luôn chuyển về StudentHomeActivity và mở tab MyCourse
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

        // Bắt back-press hệ thống để hành vi giống nút back trên UI
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHomeMyCourses();
            }
        });
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
                        // ✅ FIX: Wrap với AsyncApiHelper để tránh sync call
                        AsyncApiHelper.execute(
                                () -> lessonApi.getLessonsForCourse(courseId),
                                new AsyncApiHelper.ApiCallback<List<Lesson>>() {
                                    @Override
                                    public void onSuccess(List<Lesson> lessons) {
                                        bindLessonsWithProgress(lessons);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Silent fail - listener callback
                                    }
                                }
                        );
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

                    // ✅ FIX: Wrap với AsyncApiHelper để tránh sync call
                    AsyncApiHelper.execute(
                            () -> reviewApi.getReviewsForCourse(courseId),
                            new AsyncApiHelper.ApiCallback<List<CourseReview>>() {
                                @Override
                                public void onSuccess(List<CourseReview> reviews) {
                                    reviewAdapter.submitList(reviews);
                                }

                                @Override
                                public void onError(Exception e) {
                                    // Silent fail - listener callback
                                }
                            }
                    );
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
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    Course course = courseApi.getCourseDetail(id);
                    if (course == null) return null;

                    List<Lesson> lessons = lessonApi.getLessonsForCourse(id);
                    List<CourseReview> reviews = reviewApi.getReviewsForCourse(id);

                    return new PurchasedCourseResult(course, lessons, reviews);
                },
                new AsyncApiHelper.ApiCallback<PurchasedCourseResult>() {
                    @Override
                    public void onSuccess(PurchasedCourseResult result) {
                        // ===== MAIN THREAD =====
                        if (result == null) {
                            Toast.makeText(
                                    StudentCoursePurchasedActivity.this,
                                    "Không tìm thấy khóa học",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                            return;
                        }

                        currentCourse = result.course;

                        bindCourseMeta(result.course);
                        bindLessonsWithProgress(result.lessons);
                        updateCourseProgress(result.lessons);
                        reviewAdapter.submitList(result.reviews);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                StudentCoursePurchasedActivity.this,
                                "Lỗi tải dữ liệu khóa học",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void bindCourseMeta(Course course) {
        ImageLoader.getInstance().display(
                course.getImageUrl(),
                imgCourseBanner,
                R.drawable.ic_image_placeholder
        );

        tvCourseTitle.setText(course.getTitle());

        float rating = (float) course.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        tvRatingCount.setText("(" + course.getRatingCount() + " đánh giá)");

        tvStudentsCount.setText("👥 " + course.getStudents() + " học viên");
        tvTeacherName.setText("👨‍🏫 " + course.getTeacher());
        tvUpdatedDate.setText("📅 Cập nhật: " + course.getCreatedAt());

        String time;
        if (course.getTotalDurationMinutes() >= 60) {
            int h = course.getTotalDurationMinutes() / 60;
            int m = course.getTotalDurationMinutes() % 60;
            time = h + " giờ " + (m > 0 ? m + " phút" : "");
        } else {
            time = course.getTotalDurationMinutes() + " phút";
        }
        tvLectureSummary.setText("📖 " + course.getLectures() + " bài • " + time);
    }

    static class PurchasedCourseResult {
        Course course;
        List<Lesson> lessons;
        List<CourseReview> reviews;

        PurchasedCourseResult(
                Course course,
                List<Lesson> lessons,
                List<CourseReview> reviews
        ) {
            this.course = course;
            this.lessons = lessons;
            this.reviews = reviews;
        }
    }



    /**
     * ✅ REFACTORED: Async wrapper để load tất cả data trước khi bind UI
     *
     * Chiến lược:
     * 1. Load ALL lesson progress + quiz data trong background thread
     * 2. Build UI models trên main thread với data đã có
     * 3. Tránh multiple sync calls trong loops
     */
    private void bindLessonsWithProgress(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            lessonAdapter.submitItems(null);
            updateCourseProgress(null);
            return;
        }

        // ✅ FIX: Wrap toàn bộ data loading với AsyncApiHelper
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    // Load ALL data cần thiết CHO TẤT CẢ lessons
                    User currentUser = SessionManager.getInstance(this).getCurrentUser();
                    String studentId = currentUser != null ? currentUser.getId() : null;

                    List<LessonDataForUI> lessonsData = new ArrayList<>();

                    for (Lesson lesson : lessons) {
                        // Load progress
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

                        // Load quiz data
                        boolean hasQuiz = false;
                        boolean quizPassed = false;
                        try {
                            if (lessonQuizApi != null) {
                                hasQuiz = lessonQuizApi.getQuizForLesson(lesson.getId()) != null;
                                if (hasQuiz) {
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

                        lessonsData.add(new LessonDataForUI(lesson, percent, completed, hasQuiz, quizPassed));
                    }

                    return lessonsData;
                },
                new AsyncApiHelper.ApiCallback<List<LessonDataForUI>>() {
                    @Override
                    public void onSuccess(List<LessonDataForUI> lessonsData) {
                        // ===== MAIN THREAD =====
                        // Build UI models từ pre-loaded data
                        buildLessonUiModels(lessonsData, lessons);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Fallback: show lessons without progress
                        lessonAdapter.submitItems(null);
                    }
                }
        );
    }

    /**
     * Helper class chứa data đã load cho mỗi lesson
     */
    private static class LessonDataForUI {
        Lesson lesson;
        int percent;
        boolean completed;
        boolean hasQuiz;
        boolean quizPassed;

        LessonDataForUI(Lesson lesson, int percent, boolean completed, boolean hasQuiz, boolean quizPassed) {
            this.lesson = lesson;
            this.percent = percent;
            this.completed = completed;
            this.hasQuiz = hasQuiz;
            this.quizPassed = quizPassed;
        }
    }

    /**
     * Build UI models từ pre-loaded data (runs on main thread)
     */
    private void buildLessonUiModels(List<LessonDataForUI> lessonsData, List<Lesson> lessons) {
        List<LessonCardAdapter.LessonItemUiModel> items = new ArrayList<>();

        // allPreviousUnlocked: true nếu tất cả lesson trước đó đã hoàn thành *và* (nếu có quiz) đã pass
        boolean allPreviousUnlocked = true;

        for (LessonDataForUI data : lessonsData) {

            // determine lock state for THIS lesson:
            // unlocked only when all previous lessons are unlocked
            boolean isLocked = !allPreviousUnlocked;

            // create UI model with completed + quizPassed so adapter can enable quiz button correctly
            items.add(new LessonCardAdapter.LessonItemUiModel(
                    data.lesson,
                    data.percent,
                    isLocked,
                    data.hasQuiz,
                    data.completed,
                    data.quizPassed
            ));

            // Update allPreviousUnlocked for next lesson:
            // current lesson counts as "unlocked" for the next one only if:
            // - video completed AND
            // - if it has a quiz, quizPassed must be true
            boolean thisLessonUnlocksNext = data.completed && (!data.hasQuiz || data.quizPassed);
            allPreviousUnlocked = allPreviousUnlocked && thisLessonUnlocksNext;
        }

        lessonAdapter.submitItems(items);

        // Update tổng tiến độ khi lessons + progress đã được bind
        updateCourseProgress(lessons);
    }

    /**
     * ✅ REFACTORED: Async calculation of course progress
     *
     * Tính tổng tiến độ khóa học dựa trên lesson progress:
     * - Nếu mọi lesson có totalSecond > 0 => weighted-by-duration
     * - Ngược lại => dùng trung bình completionPercentage
     */
    private void updateCourseProgress(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            runOnUiThread(() -> {
                progressCourseBar.setProgress(0);
                tvCourseProgressPercent.setText("0%");
            });
            return;
        }

        // ✅ FIX: Wrap data loading với AsyncApiHelper
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    User currentUser = SessionManager.getInstance(this).getCurrentUser();
                    String studentId = currentUser != null ? currentUser.getId() : null;

                    // Load ALL progress cho tất cả lessons
                    List<LessonProgress> allProgress = new ArrayList<>();
                    for (Lesson l : lessons) {
                        LessonProgress p = lessonProgressApi.getLessonProgress(l.getId(), studentId);
                        allProgress.add(p); // add null if not found
                    }

                    // Calculate progress
                    boolean allHaveDuration = true;
                    for (LessonProgress p : allProgress) {
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
                        for (LessonProgress p : allProgress) {
                            if (p != null) {
                                double t = p.getTotalSecond();
                                double c = Math.min(p.getCurrentSecond(), t);
                                totalSecondsSum += t;
                                watchedSecondsSum += c;
                            }
                        }
                        if (totalSecondsSum > 0) {
                            percent = (int) Math.round((watchedSecondsSum / totalSecondsSum) * 100.0);
                        }
                    } else {
                        // fallback: average of completionPercentage
                        int sumPerc = 0;
                        int count = 0;
                        for (LessonProgress p : allProgress) {
                            int cp = 0;
                            if (p != null) cp = p.getCompletionPercentage();
                            sumPerc += cp;
                            count++;
                        }
                        if (count > 0) {
                            percent = Math.round((float) sumPerc / (float) count);
                        }
                    }

                    return Math.max(0, Math.min(100, percent));
                },
                new AsyncApiHelper.ApiCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer percent) {
                        // ===== MAIN THREAD =====
                        progressCourseBar.setProgress(percent);
                        tvCourseProgressPercent.setText(percent + "%");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Fallback: set 0%
                        progressCourseBar.setProgress(0);
                        tvCourseProgressPercent.setText("0%");
                    }
                }
        );
    }

    private void setupActions() {
        // Thay finish() bằng điều hướng tới StudentHomeActivity mở tab My Course
        btnBack.setOnClickListener(v -> navigateToHomeMyCourses());

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

            // ✅ FIX: Wrap với AsyncApiHelper để tránh sync call
            final String finalStudentName = studentName;
            final float finalRating = rating;

            AsyncApiHelper.execute(
                    () -> reviewApi.addReviewToCourse(courseId, finalStudentName, finalRating, comment),
                    new AsyncApiHelper.ApiCallback<CourseReview>() {
                        @Override
                        public void onSuccess(CourseReview newReview) {
                            if (newReview != null) {
                                // UX: clear inputs + show toast
                                ratingBarUserInput.setRating(0);
                                etCommentInput.setText("");

                                Toast.makeText(StudentCoursePurchasedActivity.this,
                                        "Đánh giá " + (int) finalRating + " sao đã được gửi thành công!",
                                        Toast.LENGTH_SHORT).show();

                                // 🔔 TẠO THÔNG BÁO CHO TEACHER khi student review course
                                createNotificationForTeacher(newReview, finalRating, finalStudentName);

                            } else {
                                Toast.makeText(StudentCoursePurchasedActivity.this,
                                        "Lỗi khi gửi đánh giá. Vui lòng thử lại.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(StudentCoursePurchasedActivity.this,
                                    "Lỗi khi gửi đánh giá. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    /**
     * Điều hướng về StudentHomeActivity và mở tab MyCourse
     */
    private void navigateToHomeMyCourses() {
        Intent intent = new Intent(this, StudentHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("open_my_course", true);
        startActivity(intent);
        finish();
    }

    /**
     * ✅ REFACTORED: Async notification creation
     */
    private void createNotificationForTeacher(CourseReview newReview, float rating, String studentName) {
        if (currentCourse == null) return;

        // ✅ FIX: Wrap với AsyncApiHelper (best-effort notification)
        AsyncApiHelper.execute(
                () -> {
                    com.example.projectonlinecourseeducation.data.notification.NotificationApi notificationApi =
                            ApiProvider.getNotificationApi();
                    String teacherId = ((com.example.projectonlinecourseeducation.data.notification.NotificationFakeApiService) notificationApi)
                            .getTeacherIdByName(currentCourse.getTeacher());

                    notificationApi.createStudentCourseReviewNotification(
                            teacherId,
                            studentName,
                            courseId,
                            currentCourse.getTitle(),
                            newReview.getId(),
                            rating
                    );
                    return null;
                },
                new AsyncApiHelper.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Silent success
                    }

                    @Override
                    public void onError(Exception e) {
                        // Silent fail - notification is best-effort
                    }
                }
        );
    }
}
