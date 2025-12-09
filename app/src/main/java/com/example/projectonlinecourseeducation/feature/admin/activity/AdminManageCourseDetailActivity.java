package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 *
 * Lưu ý: Không chỉnh sửa API — nếu AuthApi không có getAllUsers(), dùng reflection để phát hiện
 * và tránh lỗi biên dịch / runtime.
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

    // Listeners
    private CourseApi.CourseUpdateListener courseUpdateListener;
    private LessonApi.LessonUpdateListener lessonUpdateListener;
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressUpdateListener;
    private CourseStudentApi.StudentUpdateListener courseStudentListener;
    private com.example.projectonlinecourseeducation.data.coursereview.ReviewApi.ReviewUpdateListener reviewUpdateListener;

    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

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

        fetchCourseDetail();
        fetchLessonsFromApi();
        fetchStudentsFromApi();
        fetchReviewsFromApi();
        fetchCartCountFromApi(); // NEW: Fetch cart count
    }

    /* ==================== FETCH / LOAD ==================== */

    /**
     * NEW: Đếm số lượng users đang có course này trong giỏ hàng
     * LƯU Ý: Không giả định AuthApi có getAllUsers() — sử dụng reflection để kiểm tra.
     */
    private void fetchCartCountFromApi() {
        final com.example.projectonlinecourseeducation.data.cart.CartApi cartApi =
                ApiProvider.getCartApi();

        // đảm bảo tvCartCount không null
        if (tvCartCount == null) {
            Log.w(TAG, "tvCartCount is null when fetching cart count");
        }

        if (cartApi == null || courseId == null) {
            runOnUiThread(() -> {
                if (tvCartCount != null) tvCartCount.setText("0");
            });
            return;
        }

        bgExecutor.execute(() -> {
            int count = 0;
            try {
                // Lấy AuthApi
                com.example.projectonlinecourseeducation.data.auth.AuthApi authApi =
                        ApiProvider.getAuthApi();

                if (authApi == null) {
                    Log.w(TAG, "AuthApi is null — cannot fetch users for cart count");
                } else {
                    // DÙNG REFLECTION: gọi getAllUsers nếu tồn tại
                    try {
                        Method getAllUsersMethod = null;
                        try {
                            getAllUsersMethod = authApi.getClass().getMethod("getAllUsers");
                        } catch (NoSuchMethodException nsme) {
                            // phương thức không tồn tại
                            getAllUsersMethod = null;
                        }

                        List<com.example.projectonlinecourseeducation.core.model.user.User> allUsers = null;

                        if (getAllUsersMethod != null) {
                            Object res = null;
                            try {
                                res = getAllUsersMethod.invoke(authApi);
                            } catch (IllegalAccessException | InvocationTargetException ite) {
                                Log.w(TAG, "Error invoking AuthApi.getAllUsers(): " + ite.getMessage(), ite);
                            }
                            if (res instanceof List) {
                                //noinspection unchecked
                                allUsers = (List<com.example.projectonlinecourseeducation.core.model.user.User>) res;
                            }
                        } else {
                            // Nếu phương thức không tồn tại, log ra để dev biết.
                            Log.w(TAG, "AuthApi.getAllUsers() method not found via reflection");
                        }

                        if (allUsers == null || allUsers.isEmpty()) {
                            Log.d(TAG, "No users returned from AuthApi (or method absent)");
                        } else {
                            for (com.example.projectonlinecourseeducation.core.model.user.User user : allUsers) {
                                if (user == null || user.getId() == null) continue;

                                List<com.example.projectonlinecourseeducation.core.model.course.Course> userCart =
                                        cartApi.getCartCoursesForUser(user.getId());
                                if (userCart == null || userCart.isEmpty()) continue;

                                for (com.example.projectonlinecourseeducation.core.model.course.Course c : userCart) {
                                    if (c != null && courseId.equals(c.getId())) {
                                        count++;
                                        break; // mỗi user chỉ đếm 1 lần
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error while iterating users for cart count: " + e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "fetchCartCountFromApi error: " + e.getMessage(), e);
            }

            final int finalCount = count;
            runOnUiThread(() -> {
                if (tvCartCount != null) tvCartCount.setText(String.valueOf(finalCount));
            });
        });
    }

    private void fetchCourseDetail() {
        final CourseApi courseApi = ApiProvider.getCourseApi();
        if (courseApi == null) {
            Toast.makeText(this, "Course API chưa được cấu hình.", Toast.LENGTH_SHORT).show();
            return;
        }

        bgExecutor.execute(() -> {
            try {
                Course c = courseApi.getCourseDetail(courseId);

                if (c == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminManageCourseDetailActivity.this,
                                "Không tìm thấy khóa học với ID: " + courseId,
                                Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                final Course finalC = c;
                runOnUiThread(() -> {
                    course = finalC;
                    loadCourseData();
                });
            } catch (Exception e) {
                Log.w(TAG, "fetchCourseDetail error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(AdminManageCourseDetailActivity.this,
                            "Lỗi khi tải thông tin khóa học: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchLessonsFromApi() {
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        if (lessonApi == null) return;

        bgExecutor.execute(() -> {
            try {
                List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
                if (lessons == null) lessons = new ArrayList<>();

                final List<Lesson> finalLessons = lessons;
                runOnUiThread(() -> {
                    if (lessonAdapter != null) lessonAdapter.setLessons(finalLessons);
                    if (course != null) {
                        tvLectureCount.setText(String.valueOf(finalLessons.size()));
                    }
                });
            } catch (Exception e) {
                Log.w(TAG, "fetchLessonsFromApi error: " + e.getMessage(), e);
            }
        });
    }

    private void fetchStudentsFromApi() {
        final CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        final LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();

        if (csApi == null) {
            runOnUiThread(() -> {
                if (studentAdapter != null) studentAdapter.setStudents(new ArrayList<>());
                if (tvStudentCount != null) tvStudentCount.setText("0");
                if (course != null) {
                    updateTotalRevenue(0);
                }
            });
            return;
        }

        bgExecutor.execute(() -> {
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

            final int studentCount = students.size();
            runOnUiThread(() -> {
                if (studentAdapter != null) studentAdapter.setStudents(items);
                if (tvStudentCount != null) tvStudentCount.setText(String.valueOf(studentCount));

                if (course != null) {
                    double revenue = course.getPrice() * studentCount;
                    updateTotalRevenue(revenue);
                }
            });
        });
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

        bgExecutor.execute(() -> {
            try {
                List<com.example.projectonlinecourseeducation.core.model.course.CourseReview> reviews =
                        reviewApi.getReviewsForCourse(courseId);
                if (reviews == null) reviews = new ArrayList<>();

                final List<com.example.projectonlinecourseeducation.core.model.course.CourseReview> finalReviews = reviews;
                runOnUiThread(() -> {
                    if (reviewAdapter != null) reviewAdapter.setReviews(finalReviews);
                });
            } catch (Exception e) {
                Log.w(TAG, "fetchReviewsFromApi error: " + e.getMessage(), e);
            }
        });
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

        // default text so UI doesn't show null while loading
        if (tvCartCount != null) tvCartCount.setText("0");

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
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnEdit != null) btnEdit.setOnClickListener(v -> {
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
            Toast.makeText(AdminManageCourseDetailActivity.this,
                    "Xem bài học: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);

        // ===== ADMIN REVIEW ADAPTER (MỚI) =====
        reviewAdapter = new AdminCourseReviewAdapter(review -> {
            // Click review - có thể mở detail nếu cần
        });
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
        if (tvStudentCount != null) tvStudentCount.setText(String.valueOf(course.getStudents()));

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
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}
