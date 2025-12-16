package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.course.CourseStudentApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;


// ===== IMPORT CÁC ADAPTER MỚI CHO ADMIN =====
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseLessonAdapter;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseReviewAdapter;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseStudentAdapter;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseRequirementAdapter;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseSkillAdapter;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity quản lý chi tiết khóa học cho Admin
 * ✨ ĐÃ REFACTOR: Sử dụng toàn bộ adapter mới với UI đẹp hơn
 *
 * Thay đổi:
 * - AdminCourseLessonAdapter (thay ManagementCourseLessonAdapter)
 * - AdminCourseStudentAdapter (thay ManagementCourseStudentAdapter)
 * - AdminCourseReviewAdapter (thay ManagementCourseReviewAdapter)
 * - AdminCourseSkillAdapter (thay ManagementCourseSkillAdapter)
 * - AdminCourseRequirementAdapter (thay ManagementCourseRequirementAdapter)
 */
public class AdminManageCourseDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminCourseMgmt";

    private Course course;
    private String courseId;

    private ImageButton btnBack;
    private ImageButton btnEdit;
    private ImageView imgCourseThumbnail;
    private TextView tvCourseTitle;
    private TextView tvTeacherName;
    private TextView tvCategory;
    private TextView tvPrice;
    private TextView tvTotalRevenue;
    private TextView tvDescription;
    private TextView tvStudentCount;
    private TextView tvRating;
    private RatingBar rbRating;
    private TextView tvRatingCount;
    private TextView tvLectureCount;
    private TextView tvTotalDuration;
    private TextView tvCreatedAt;
    private TextView tvCartCount; // NEW: Số lượng user có course trong giỏ

    private RecyclerView rvSkills;
    private AdminCourseSkillAdapter skillAdapter;

    private RecyclerView rvRequirements;
    private AdminCourseRequirementAdapter requirementAdapter;

    private RecyclerView rvStudents;
    private AdminCourseStudentAdapter studentAdapter;

    private RecyclerView rvLessons;
    private AdminCourseLessonAdapter lessonAdapter;

    private RecyclerView rvReviews;
    private AdminCourseReviewAdapter reviewAdapter;

    private ImageView imgStudentExpand;
    private ImageView imgLessonExpand;
    private ImageView imgReviewExpand;

    private com.example.projectonlinecourseeducation.data.coursereview.ReviewApi reviewApi;
    private com.example.projectonlinecourseeducation.data.cart.CartApi cartApi; // NEW

    // Listeners
    private CourseApi.CourseUpdateListener courseUpdateListener;
    private LessonApi.LessonUpdateListener lessonUpdateListener;
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressUpdateListener;
    private CourseStudentApi.StudentUpdateListener courseStudentListener;
    private com.example.projectonlinecourseeducation.data.coursereview.ReviewApi.ReviewUpdateListener reviewUpdateListener;
    private com.example.projectonlinecourseeducation.data.cart.CartApi.CartUpdateListener cartUpdateListener; // NEW


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_course_detail);

        // ✅ FIX: Đổi key từ "course_id" thành "courseId" để khớp với Adapter
        courseId = getIntent() != null ? getIntent().getStringExtra("courseId") : null;

        if (courseId == null || courseId.trim().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupAdapters();

        registerCourseUpdateListener();
        registerLessonUpdateListener();
        registerLessonProgressListener();
        registerCourseStudentListener();
        registerReviewUpdateListener();
        registerCartUpdateListener(); // NEW

        fetchCourseDetail();
        fetchLessonsFromApi();
        fetchStudentsFromApi();
        fetchReviewsFromApi();
        fetchCartCountFromApi(); // NEW: Fetch cart count
    }

    /* ==================== FETCH / LOAD ==================== */

    /**
     * NEW: Đếm số lượng users đang có course này trong giỏ hàng
     */
    private void fetchCartCountFromApi() {
        final com.example.projectonlinecourseeducation.data.cart.CartApi cartApi =
                ApiProvider.getCartApi();

        if (tvCartCount == null) {
            Log.w(TAG, "tvCartCount is null");
            return;
        }

        if (cartApi == null || courseId == null) {
            tvCartCount.setText("0");
            return;
        }

        AsyncApiHelper.execute(
                () -> {
                    int count = 0;

                    try {
                        // Lấy tất cả users từ AuthApi
                        com.example.projectonlinecourseeducation.data.auth.AuthApi authApi =
                                ApiProvider.getAuthApi();

                        if (authApi != null) {
                            java.util.List<com.example.projectonlinecourseeducation.core.model.user.User> allUsers =
                                    new java.util.ArrayList<>();

                            // Lấy students
                            try {
                                java.util.List<com.example.projectonlinecourseeducation.core.model.user.User> students =
                                        authApi.getAllUsersByRole(
                                                com.example.projectonlinecourseeducation.core.model.user.User.Role.STUDENT
                                        );
                                if (students != null) allUsers.addAll(students);
                            } catch (Exception e) {
                                Log.w(TAG, "Error getting students: " + e.getMessage());
                            }

                            // Lấy teachers
                            try {
                                java.util.List<com.example.projectonlinecourseeducation.core.model.user.User> teachers =
                                        authApi.getAllUsersByRole(
                                                com.example.projectonlinecourseeducation.core.model.user.User.Role.TEACHER
                                        );
                                if (teachers != null) allUsers.addAll(teachers);
                            } catch (Exception e) {
                                Log.w(TAG, "Error getting teachers: " + e.getMessage());
                            }

                            Log.d(TAG, "Total users to check cart: " + allUsers.size());

                            // Đếm số user có course trong cart
                            for (com.example.projectonlinecourseeducation.core.model.user.User user : allUsers) {
                                if (user == null || user.getId() == null) continue;

                                try {
                                    java.util.List<com.example.projectonlinecourseeducation.core.model.course.Course> userCart =
                                            cartApi.getCartCoursesForUser(user.getId());

                                    if (userCart != null) {
                                        for (com.example.projectonlinecourseeducation.core.model.course.Course c : userCart) {
                                            if (c != null && courseId.equals(c.getId())) {
                                                count++;
                                                Log.d(TAG, "Found course in cart of user: " + user.getName());
                                                break; // mỗi user chỉ tính 1 lần
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Error checking cart for user " + user.getId() + ": " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "fetchCartCountFromApi error: " + e.getMessage(), e);
                    }

                    return count;
                },
                new AsyncApiHelper.ApiCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        Log.d(TAG, "Cart count result: " + count);
                        tvCartCount.setText(String.valueOf(count));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "fetchCartCountFromApi error (async): " + e.getMessage(), e);
                        tvCartCount.setText("0");
                    }
                }
        );
    }


    private void fetchCourseDetail() {
        final CourseApi courseApi = ApiProvider.getCourseApi();
        if (courseApi == null) {
            Toast.makeText(this, "Course API chưa được cấu hình.", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncApiHelper.execute(
                () -> courseApi.getCourseDetail(courseId),
                new AsyncApiHelper.ApiCallback<Course>() {
                    @Override
                    public void onSuccess(Course c) {
                        if (c == null) {
                            Toast.makeText(AdminManageCourseDetailActivity.this,
                                    "Không tìm thấy khóa học với ID: " + courseId,
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        course = c;
                        loadCourseData();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AdminManageCourseDetailActivity.this,
                                "Lỗi khi tải thông tin khóa học: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void fetchLessonsFromApi() {
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        if (lessonApi == null) return;

        AsyncApiHelper.execute(
                () -> lessonApi.getLessonsForCourse(courseId),
                new AsyncApiHelper.ApiCallback<List<Lesson>>() {
                    @Override
                    public void onSuccess(List<Lesson> lessons) {
                        if (lessons == null) lessons = new ArrayList<>();
                        lessonAdapter.setLessons(lessons);
                        if (course != null) {
                            tvLectureCount.setText(String.valueOf(lessons.size()));
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "fetchLessonsFromApi error: " + e.getMessage(), e);
                    }
                }
        );
    }

    private void fetchStudentsFromApi() {
        final CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        final LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();

        if (csApi == null) {
            if (studentAdapter != null) studentAdapter.setStudents(new ArrayList<>());
            tvStudentCount.setText("0");
            if (course != null) {
                updateTotalRevenue(0);
            }
            return;
        }

        AsyncApiHelper.execute(
                () -> {
                    List<CourseStudent> students = new ArrayList<>();
                    try {
                        students = csApi.getStudentsForCourse(courseId);
                        if (students == null) students = new ArrayList<>();
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting students: " + e.getMessage(), e);
                    }

                    List<Lesson> lessons = new ArrayList<>();
                    try {
                        if (lessonApi != null) {
                            lessons = lessonApi.getLessonsForCourse(courseId);
                            if (lessons == null) lessons = new ArrayList<>();
                        }
                    } catch (Exception ignored) {}

                    final List<AdminCourseStudentAdapter.StudentProgressItem> items = new ArrayList<>();
                    for (CourseStudent student : students) {
                        List<AdminCourseStudentAdapter.LessonProgressDetail> ldetails = new ArrayList<>();
                        if (!lessons.isEmpty()) {
                            for (int j = 0; j < lessons.size(); j++) {
                                Lesson lesson = lessons.get(j);
                                int progressPercent = 0;
                                boolean isCompleted = false;
                                try {
                                    if (lpApi != null && lesson != null) {
                                        LessonProgress lp = lpApi.getLessonProgress(
                                                lesson.getId(),
                                                student != null ? student.getId() : null
                                        );
                                        if (lp != null) {
                                            progressPercent = lp.getCompletionPercentage();
                                            isCompleted = lp.isCompleted();
                                        }
                                    }
                                } catch (Exception ignored) {}

                                ldetails.add(new AdminCourseStudentAdapter.LessonProgressDetail(
                                        j + 1,
                                        lesson.getTitle() != null ? lesson.getTitle() : ("Bài " + (j + 1)),
                                        progressPercent,
                                        isCompleted
                                ));
                            }
                        }

                        AdminCourseStudentAdapter.StudentProgressItem spi =
                                new AdminCourseStudentAdapter.StudentProgressItem(
                                        student,
                                        computeAggregateProgress(ldetails),
                                        countCompleted(ldetails),
                                        ldetails.size(),
                                        ldetails
                                );
                        items.add(spi);
                    }

                    return new StudentFetchResult(items, students.size());
                },
                new AsyncApiHelper.ApiCallback<StudentFetchResult>() {
                    @Override
                    public void onSuccess(StudentFetchResult result) {
                        if (studentAdapter != null) {
                            studentAdapter.setStudents(result.items);
                        }
                        tvStudentCount.setText(String.valueOf(result.studentCount));

                        if (course != null) {
                            double revenue = course.getPrice() * result.studentCount;
                            updateTotalRevenue(revenue);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "fetchStudentsFromApi error: " + e.getMessage(), e);
                    }
                }
        );
    }
    private static class StudentFetchResult {
        List<AdminCourseStudentAdapter.StudentProgressItem> items;
        int studentCount;

        StudentFetchResult(
                List<AdminCourseStudentAdapter.StudentProgressItem> items,
                int studentCount
        ) {
            this.items = items;
            this.studentCount = studentCount;
        }
    }


    private int computeAggregateProgress(List<AdminCourseStudentAdapter.LessonProgressDetail> details) {
        if (details == null || details.isEmpty()) return 0;
        int sum = 0;
        for (AdminCourseStudentAdapter.LessonProgressDetail d : details)
            sum += d.getProgressPercentage();
        return sum / details.size();
    }

    private int countCompleted(List<AdminCourseStudentAdapter.LessonProgressDetail> details) {
        if (details == null) return 0;
        int cnt = 0;
        for (AdminCourseStudentAdapter.LessonProgressDetail d : details)
            if (d.isCompleted()) cnt++;
        return cnt;
    }

    private void fetchReviewsFromApi() {
        reviewApi = ApiProvider.getReviewApi();
        if (reviewApi == null) {
            runOnUiThread(() -> {
                if (reviewAdapter != null) reviewAdapter.setReviews(new ArrayList<>());
            });
            return;
        }

        AsyncApiHelper.execute(
                () -> reviewApi.getReviewsForCourse(courseId),
                new AsyncApiHelper.ApiCallback<List<CourseReview>>() {
                    @Override
                    public void onSuccess(List<CourseReview> reviews) {
                        if (reviews == null) reviews = new ArrayList<>();
                        reviewAdapter.setReviews(reviews);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "fetchReviewsFromApi error: " + e.getMessage(), e);
                    }
                }
        );
    }

    /* ==================== XÓA REVIEW: DIALOG + HÀM XÓA ==================== */

    /**
     * Hiển thị dialog xác nhận xoá review và thực hiện xoá nếu xác nhận.
     * Phù hợp với nhiều tên phương thức xóa khác nhau trong ReviewApi (deleteReview, removeReview, delete, ...)
     */
    private void showDeleteReviewConfirmDialog(CourseReview review) {
        if (review == null) return;

        String reviewerName = null;
        try {
            reviewerName = review.getStudentName();
        } catch (Exception ignored) {}

        if (reviewerName == null) {
            try {
                // thử getter khác nếu có
                reviewerName = review.getStudentName();
            } catch (Exception ignored) {
            }
        }
        if (reviewerName == null) reviewerName = "Người dùng";

        String message = "Bạn có chắc muốn xóa đánh giá của " + reviewerName + "?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage(message)
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dialog.dismiss();
                    // Thực hiện xóa trên background
                    AsyncApiHelper.execute(
                            () -> {
                                boolean deleted = false;

                                try {
                                    if (reviewApi == null) {
                                        reviewApi = ApiProvider.getReviewApi();
                                    }
                                    if (reviewApi == null) {
                                        throw new IllegalStateException("Review API is null");
                                    }

                                    String reviewId = null;
                                    try {
                                        reviewId = review.getId();
                                    } catch (Exception ignored) {}

                                    // Thử method deleteReview(String)
                                    try {
                                        Method m = reviewApi.getClass()
                                                .getMethod("deleteReview", String.class);
                                        Object res = m.invoke(reviewApi, reviewId);

                                        if (res == null) {
                                            deleted = true; // void method
                                        } else if (res instanceof Boolean) {
                                            deleted = (Boolean) res;
                                        } else {
                                            deleted = true;
                                        }
                                    } catch (NoSuchMethodException ignored) {
                                        // Thử các tên method khác
                                        String[] altNames = {
                                                "removeReviewById",
                                                "removeReview",
                                                "delete",
                                                "deleteById"
                                        };

                                        for (String name : altNames) {
                                            if (deleted) break;
                                            try {
                                                Method m2 = reviewApi.getClass()
                                                        .getMethod(name, String.class);
                                                Object res2 = m2.invoke(reviewApi, reviewId);

                                                if (res2 == null) {
                                                    deleted = true;
                                                } else if (res2 instanceof Boolean) {
                                                    deleted = (Boolean) res2;
                                                } else {
                                                    deleted = true;
                                                }
                                            } catch (NoSuchMethodException ignored2) {
                                                // tiếp tục thử
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Error deleting review (reflection): " + e.getMessage(), e);
                                    deleted = false;
                                }

                                return deleted;
                            },
                            new AsyncApiHelper.ApiCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean deleted) {
                                    if (deleted) {
                                        Toast.makeText(AdminManageCourseDetailActivity.this,
                                                "Xóa đánh giá thành công",
                                                Toast.LENGTH_SHORT).show();
                                        fetchReviewsFromApi();
                                    } else {
                                        Toast.makeText(AdminManageCourseDetailActivity.this,
                                                "Không thể xóa đánh giá. Thử lại sau.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(AdminManageCourseDetailActivity.this,
                                            "Lỗi khi xóa đánh giá: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                })
                .setCancelable(true)
                .show();
    }

    /* ==================== INIT / UI ==================== */

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        imgCourseThumbnail = findViewById(R.id.imgCourseThumbnail);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvDescription = findViewById(R.id.tvDescription);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvRating = findViewById(R.id.tvRating);
        rbRating = findViewById(R.id.rbRating);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvLectureCount = findViewById(R.id.tvLectureCount);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvCartCount = findViewById(R.id.tvCartCount); // NEW

        rvSkills = findViewById(R.id.rvSkills);
        rvRequirements = findViewById(R.id.rvRequirements);
        rvStudents = findViewById(R.id.rvStudents);
        rvLessons = findViewById(R.id.rvLessons);
        rvReviews = findViewById(R.id.rvReviews);

        imgStudentExpand = findViewById(R.id.imgStudentExpand);
        imgLessonExpand = findViewById(R.id.imgLessonExpand);
        imgReviewExpand = findViewById(R.id.imgReviewExpand);

        try {
            if (imgStudentExpand != null) imgStudentExpand.setRotation(180f);
            if (imgLessonExpand != null) imgLessonExpand.setRotation(180f);
            if (imgReviewExpand != null) imgReviewExpand.setRotation(180f);
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (courseId == null && course != null) {
                courseId = course.getId();
            }
            if (courseId == null) {
                Toast.makeText(AdminManageCourseDetailActivity.this,
                        "Không có khóa học để chỉnh sửa", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(AdminManageCourseDetailActivity.this,
                    "Chức năng chỉnh sửa đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        if (imgStudentExpand != null)
            imgStudentExpand.setOnClickListener(v -> toggleExpandable(rvStudents, imgStudentExpand));
        if (imgLessonExpand != null)
            imgLessonExpand.setOnClickListener(v -> toggleExpandable(rvLessons, imgLessonExpand));
        if (imgReviewExpand != null)
            imgReviewExpand.setOnClickListener(v -> toggleExpandable(rvReviews, imgReviewExpand));
    }

    private void setupAdapters() {
        // ===== ADMIN STUDENT ADAPTER (MỚI) =====
        studentAdapter = new AdminCourseStudentAdapter(student -> {
            // Click student - có thể mở detail nếu cần
        });
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(studentAdapter);

        // ===== ADMIN LESSON ADAPTER (MỚI) =====
        lessonAdapter = new AdminCourseLessonAdapter(lesson -> {
            // Open AdminLessonDetailActivity
            Intent intent = new Intent(AdminManageCourseDetailActivity.this,
                    AdminLessonDetailActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            startActivity(intent);
        });
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);

        // ===== ADMIN REVIEW ADAPTER (MỚI) =====
        reviewAdapter = new AdminCourseReviewAdapter(
                // Click review
                review -> {
                    // Click thường - không làm gì hoặc xem detail
                },
                // Long click review - DELETE
                review -> {
                    showDeleteReviewConfirmDialog(review);
                    return true;
                }
        );
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        // ===== ADMIN SKILL ADAPTER (MỚI) =====
        skillAdapter = new AdminCourseSkillAdapter();
        rvSkills.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvSkills.setAdapter(skillAdapter);

        // ===== ADMIN REQUIREMENT ADAPTER (MỚI) =====
        requirementAdapter = new AdminCourseRequirementAdapter();
        rvRequirements.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvRequirements.setAdapter(requirementAdapter);
    }

    private void loadCourseData() {
        if (course == null) return;

        Log.d(TAG, "loadCourseData: " + course.getTitle() + " (ID: " + course.getId() + ")");

        tvCourseTitle.setText(safe(course.getTitle()));
        tvTeacherName.setText(safe(course.getTeacher()));
        tvCategory.setText(safe(course.getCategory()));

        try {
            DecimalFormat df = new DecimalFormat("#,###");
            tvPrice.setText(df.format((long) course.getPrice()) + " VNĐ");
        } catch (Exception ignored) {
            tvPrice.setText(String.format("%.0f VNĐ", course.getPrice()));
        }

        tvDescription.setText(safe(course.getDescription()));
        tvStudentCount.setText(String.valueOf(course.getStudents()));

        double revenue = course.getPrice() * course.getStudents();
        updateTotalRevenue(revenue);

        tvRating.setText(String.format("%.1f", course.getRating()));

        try {
            rbRating.setNumStars(5);
            rbRating.setStepSize(0.5f);
            rbRating.setRating((float) course.getRating());
            rbRating.getProgressDrawable().setColorFilter(
                    getResources().getColor(R.color.yellow_star), PorterDuff.Mode.SRC_ATOP);
        } catch (Exception ignored) {}

        tvRatingCount.setText("(" + course.getRatingCount() + " đánh giá)");
        tvLectureCount.setText(String.valueOf(course.getLectures()));
        tvTotalDuration.setText(formatDuration(course.getTotalDurationMinutes()));
        tvCreatedAt.setText(safe(course.getCreatedAt()));

        List<String> skills = course.getSkills();
        if (skills != null)
            skillAdapter.setSkills(skills);
        else
            skillAdapter.setSkills(new ArrayList<>());

        List<String> requirements = course.getRequirements();
        if (requirements != null)
            requirementAdapter.setRequirements(requirements);
        else
            requirementAdapter.setRequirements(new ArrayList<>());

        String thumbUrl = extractThumbnailUrl(course);
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            ImageLoader.getInstance().display(thumbUrl, imgCourseThumbnail,
                    R.drawable.ic_image_placeholder, success -> {
                        if (!success) {
                            Log.w(TAG, "ImageLoader failed: " + thumbUrl);
                        }
                    });
        } else {
            imgCourseThumbnail.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private void updateTotalRevenue(double revenue) {
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            tvTotalRevenue.setText(df.format((long) revenue) + " VNĐ");
        } catch (Exception ignored) {
            tvTotalRevenue.setText(String.format("%.0f VNĐ", revenue));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0)
            return hours + " giờ " + mins + " phút";
        return mins + " phút";
    }

    private String extractThumbnailUrl(Course course) {
        if (course == null) return null;
        String[] candidates = {
                "getThumbnailUrl", "getThumbnail", "getImageUrl",
                "getImage", "getCoverUrl", "getCover"
        };
        for (String name : candidates) {
            try {
                Method m = course.getClass().getMethod(name);
                Object v = m.invoke(course);
                if (v != null) return v.toString();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                Log.d(TAG, "extractThumbnailUrl failed for " + name + ": " + e.getMessage());
            }
        }
        return null;
    }

    /* ==================== LISTENERS ==================== */

    private void registerCourseUpdateListener() {
        try {
            CourseApi courseApi = ApiProvider.getCourseApi();
            if (courseApi == null) return;

            courseUpdateListener = (updatedCourseId, updatedCourse) -> {
                Log.d(TAG, "onCourseUpdated: " + updatedCourseId);

                if (updatedCourseId == null || updatedCourseId.isEmpty()) {
                    runOnUiThread(() -> {
                        fetchCourseDetail();
                        fetchStudentsFromApi();
                    });
                    return;
                }

                if (course != null && !updatedCourseId.equals(course.getId())) {
                    return;
                }

                if (course == null && courseId != null && !updatedCourseId.equals(courseId)) {
                    return;
                }

                runOnUiThread(() -> {
                    if (updatedCourse == null) {
                        Toast.makeText(AdminManageCourseDetailActivity.this,
                                "Khóa học đã bị xóa", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    course = updatedCourse;
                    loadCourseData();
                    fetchStudentsFromApi();
                });
            };
            courseApi.addCourseUpdateListener(courseUpdateListener);
        } catch (Exception e) {
            Log.w(TAG, "registerCourseUpdateListener failed: " + e.getMessage(), e);
        }
    }

    private void registerLessonUpdateListener() {
        try {
            LessonApi lessonApi = ApiProvider.getLessonApi();
            if (lessonApi == null) return;

            lessonUpdateListener = (lessonId, updatedLesson) -> {
                fetchLessonsFromApi();
                fetchCourseDetail();
                fetchStudentsFromApi();
            };
            lessonApi.addLessonUpdateListener(lessonUpdateListener);
        } catch (Exception ignored) {}
    }

    private void registerLessonProgressListener() {
        try {
            LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();
            if (lpApi == null) return;

            lessonProgressUpdateListener = lessonId ->
                    runOnUiThread(() -> fetchStudentsFromApi());
            lpApi.addLessonProgressUpdateListener(lessonProgressUpdateListener);
        } catch (Exception ignored) {}
    }

    private void registerCourseStudentListener() {
        try {
            CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
            if (csApi == null) return;

            courseStudentListener = updatedCourseId -> {
                if (updatedCourseId != null && !updatedCourseId.isEmpty()
                        && courseId != null && !updatedCourseId.equals(courseId)) {
                    return;
                }
                runOnUiThread(() -> fetchStudentsFromApi());
            };
            csApi.addStudentUpdateListener(courseStudentListener);
        } catch (Exception e) {
            Log.w(TAG, "registerCourseStudentListener failed: " + e.getMessage(), e);
        }
    }

    private void registerReviewUpdateListener() {
        try {
            reviewApi = ApiProvider.getReviewApi();
            if (reviewApi == null) return;

            reviewUpdateListener = changedCourseId -> {
                if (changedCourseId != null && !changedCourseId.isEmpty()
                        && courseId != null && !changedCourseId.equals(courseId)) {
                    return;
                }
                runOnUiThread(() -> fetchReviewsFromApi());
            };
            reviewApi.addReviewUpdateListener(reviewUpdateListener);
        } catch (Exception e) {
            Log.w(TAG, "registerReviewUpdateListener failed: " + e.getMessage(), e);
        }
    }

    /**
     * NEW: Đăng ký listener để tự động refresh cart count khi có thay đổi
     */
    private void registerCartUpdateListener() {
        try {
            cartApi = ApiProvider.getCartApi();
            if (cartApi == null) return;

            cartUpdateListener = () -> {
                // Khi cart thay đổi (add/remove), refresh cart count
                Log.d(TAG, "Cart changed, refreshing cart count");
                runOnUiThread(() -> fetchCartCountFromApi());
            };
            cartApi.addCartUpdateListener(cartUpdateListener);
        } catch (Exception e) {
            Log.w(TAG, "registerCartUpdateListener failed: " + e.getMessage(), e);
        }
    }

    /* ==================== EXPAND / ANIMATION ==================== */

    private void toggleExpandable(View content, ImageView icon) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        if (isVisible) {
            content.setVisibility(View.GONE);
            rotateIconTo(icon, 0f);
        } else {
            content.setVisibility(View.VISIBLE);
            rotateIconTo(icon, 180f);
        }
    }

    private void rotateIconTo(ImageView icon, float to) {
        if (icon == null) return;
        try {
            icon.animate().cancel();
            icon.animate().rotation(to).setDuration(200).start();
            icon.setRotation(to);
        } catch (Exception e) {
            Log.w(TAG, "rotateIconTo failed: " + e.getMessage(), e);
            try {
                icon.setRotation(to);
            } catch (Exception ignored) {}
        }
    }

    /* ==================== CLEANUP ==================== */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (courseUpdateListener != null) {
                CourseApi courseApi = ApiProvider.getCourseApi();
                if (courseApi != null) courseApi.removeCourseUpdateListener(courseUpdateListener);
            }
        } catch (Exception ignored) {}

        try {
            LessonApi lessonApi = ApiProvider.getLessonApi();
            if (lessonUpdateListener != null && lessonApi != null) {
                lessonApi.removeLessonUpdateListener(lessonUpdateListener);
            }
        } catch (Exception ignored) {}

        try {
            LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();
            if (lessonProgressUpdateListener != null && lpApi != null) {
                lpApi.removeLessonProgressUpdateListener(lessonProgressUpdateListener);
            }
        } catch (Exception ignored) {}

        try {
            CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
            if (courseStudentListener != null && csApi != null) {
                csApi.removeStudentUpdateListener(courseStudentListener);
            }
        } catch (Exception ignored) {}

        try {
            if (reviewUpdateListener != null && reviewApi != null) {
                reviewApi.removeReviewUpdateListener(reviewUpdateListener);
            }
        } catch (Exception ignored) {}

        try {
            if (cartUpdateListener != null && cartApi != null) {
                cartApi.removeCartUpdateListener(cartUpdateListener);
            }
        } catch (Exception ignored) {}

    }
}
