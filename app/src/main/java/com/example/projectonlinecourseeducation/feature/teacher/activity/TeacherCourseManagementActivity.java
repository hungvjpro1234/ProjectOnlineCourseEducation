package com.example.projectonlinecourseeducation.feature.teacher.activity;

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
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementCourseLessonAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementCourseReviewAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementCourseStudentAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementCourseRequirementAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementCourseSkillAdapter;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity quản lý 1 Course cho Teacher — phiên bản dùng ApiProvider cho Course, Lesson, LessonProgress
 *
 * Chỉnh sửa:
 * - CourseUpdateListener: không bỏ qua khi updatedCourseId == null -> coi là global update -> refresh cả course & students
 * - Thêm Log để debug notify events
 * - Khi nhận course update, cập nhật cả course metadata và fetch student list (đồng bộ số học viên từ CourseStudentApi)
 * - Sửa animation rotate icon: dùng property animation (view.animate().rotation(...)) và đặt rotation ban đầu phù hợp
 */
public class TeacherCourseManagementActivity extends AppCompatActivity {

    private static final String TAG = "TeacherCourseMgmt";

    private Course course;
    private String courseId;

    private ImageButton btnBack;
    private ImageButton btnEdit;

    private ImageView imgCourseThumbnail;
    private TextView tvCourseTitle;
    private TextView tvCategory;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvStudentCount;
    private TextView tvRating;
    private RatingBar rbRating;
    private TextView tvRatingCount;
    private TextView tvLectureCount;
    private TextView tvTotalDuration;
    private TextView tvCreatedAt;

    private RecyclerView rvSkills;
    private LinearLayoutManager skillsLayoutManager;
    private ManagementCourseSkillAdapter managementCourseSkillAdapter;

    private RecyclerView rvRequirements;
    private LinearLayoutManager requirementsLayoutManager;
    private ManagementCourseRequirementAdapter requirementAdapter;

    private LinearLayoutManager studentsLayoutManager;
    private RecyclerView rvStudents;
    private ManagementCourseStudentAdapter studentAdapter;

    private RecyclerView rvLessons;
    private ManagementCourseLessonAdapter lessonAdapter;

    private RecyclerView rvReviews;
    private ManagementCourseReviewAdapter reviewAdapter;

    private ImageView imgStudentExpand;
    private ImageView imgLessonExpand;
    private ImageView imgReviewExpand;

    // NEW: ReviewApi
    private com.example.projectonlinecourseeducation.data.coursereview.ReviewApi reviewApi;

    // Listeners
    private CourseApi.CourseUpdateListener courseUpdateListener;
    private LessonApi.LessonUpdateListener lessonUpdateListener;
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressUpdateListener;
    private CourseStudentApi.StudentUpdateListener courseStudentListener;
    private com.example.projectonlinecourseeducation.data.coursereview.ReviewApi.ReviewUpdateListener reviewUpdateListener; // NEW

    // Executor cho các cuộc gọi blocking (FakeApi hiện tại sync)
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_management);

        courseId = getIntent() != null ? getIntent().getStringExtra("course_id") : null;

        initViews();
        setupListeners();
        setupAdapters();

        // ĐĂNG KÝ listeners trước để không bị bỏ lỡ notify (ví dụ vừa enroll xong)
        registerCourseUpdateListener();
        registerLessonUpdateListener();
        registerLessonProgressListener();
        registerCourseStudentListener();
        registerReviewUpdateListener(); // NEW

        // Sau đó fetch dữ liệu ban đầu
        fetchCourseDetail();
        fetchLessonsFromApi();
        fetchStudentsFromApi();
        fetchReviewsFromApi(); // NEW
    }

    /* -------------------- Fetch / Load -------------------- */

    private void fetchCourseDetail() {
        final CourseApi courseApi = ApiProvider.getCourseApi();
        if (courseApi == null) {
            Toast.makeText(this, "Course API chưa được cấu hình.", Toast.LENGTH_SHORT).show();
            return;
        }
        bgExecutor.execute(() -> {
            try {
                Course c = courseApi.getCourseDetail(courseId);
                if (c == null) c = courseApi.getCourseDetail(null);
                if (c == null) return;
                final Course finalC = c;
                runOnUiThread(() -> {
                    course = finalC;
                    loadCourseData();
                });
            } catch (Exception e) {
                Log.w(TAG, "fetchCourseDetail error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Lấy lessons từ LessonApi và set vào lessonAdapter.
     * Nếu LessonApi chưa có hoặc trả rỗng, giữ lessonAdapter trống.
     */
    private void fetchLessonsFromApi() {
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        if (lessonApi == null) {
            // không có lesson api -> bỏ qua (adapter sẽ rỗng)
            return;
        }

        bgExecutor.execute(() -> {
            try {
                List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
                if (lessons == null) lessons = new ArrayList<>();
                final List<Lesson> finalLessons = lessons;
                runOnUiThread(() -> {
                    lessonAdapter.setLessons(finalLessons);
                    // cập nhật lecture count nếu course metadata chưa đúng (tùy impl CourseApi)
                    if (course != null && finalLessons != null) {
                        tvLectureCount.setText(String.valueOf(finalLessons.size()));
                    }
                });
            } catch (Exception e) {
                Log.w(TAG, "fetchLessonsFromApi error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * NEW: Fetch students từ CourseStudentApi (thực sự) và lấy progress per-student qua
     * LessonProgressApi.getLessonProgress(lessonId, studentId) (default impl fallback nếu provider cũ).
     */
    private void fetchStudentsFromApi() {
        final CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
        final LessonApi lessonApi = ApiProvider.getLessonApi();
        final LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();

        if (csApi == null) {
            // fallback: giữ adapter rỗng và cập nhật count = 0
            runOnUiThread(() -> {
                if (studentAdapter != null) studentAdapter.setStudents(new ArrayList<>());
                tvStudentCount.setText("0");
            });
            return;
        }

        bgExecutor.execute(() -> {
            List<CourseStudent> students = new ArrayList<>();
            try {
                students = csApi.getStudentsForCourse(courseId);
                if (students == null) students = new ArrayList<>();
            } catch (Exception e) {
                Log.w(TAG, "Error while getting students from CourseStudentApi: " + e.getMessage(), e);
            }

            // Lấy danh sách lesson để show tiến độ cho từng student
            List<Lesson> lessons = new ArrayList<>();
            try {
                if (lessonApi != null) {
                    lessons = lessonApi.getLessonsForCourse(courseId);
                    if (lessons == null) lessons = new ArrayList<>();
                }
            } catch (Exception ignored) {}

            final List<ManagementCourseStudentAdapter.StudentProgressItem> items = new ArrayList<>();

            for (CourseStudent student : students) {
                List<ManagementCourseStudentAdapter.LessonProgressDetail> ldetails = new ArrayList<>();

                if (!lessons.isEmpty()) {
                    for (int j = 0; j < lessons.size(); j++) {
                        Lesson lesson = lessons.get(j);
                        int progressPercent = 0;
                        boolean isCompleted = false;
                        try {
                            if (lpApi != null && lesson != null) {
                                // gọi phiên bản mới có studentId (default impl sẽ fallback nếu provider cũ)
                                LessonProgress lp = lpApi.getLessonProgress(lesson.getId(), student != null ? student.getId() : null);
                                if (lp != null) {
                                    progressPercent = lp.getCompletionPercentage();
                                    isCompleted = lp.isCompleted();
                                }
                            }
                        } catch (Exception ignored) {}

                        ldetails.add(new ManagementCourseStudentAdapter.LessonProgressDetail(
                                j + 1,
                                lesson.getTitle() != null ? lesson.getTitle() : ("Bài " + (j + 1)),
                                progressPercent,
                                isCompleted
                        ));
                    }
                }

                ManagementCourseStudentAdapter.StudentProgressItem spi = new ManagementCourseStudentAdapter.StudentProgressItem(
                        student, // CourseStudent object (may be null)
                        computeAggregateProgress(ldetails),
                        countCompleted(ldetails),
                        ldetails.size(),
                        ldetails
                );
                items.add(spi);
            }

            // cập nhật UI: adapter + số học viên
            final int studentCount = students.size();
            runOnUiThread(() -> {
                if (studentAdapter != null) studentAdapter.setStudents(items);
                tvStudentCount.setText(String.valueOf(studentCount));
            });
        });
    }

    private int computeAggregateProgress(List<ManagementCourseStudentAdapter.LessonProgressDetail> details) {
        if (details == null || details.isEmpty()) return 0;
        int sum = 0;
        for (ManagementCourseStudentAdapter.LessonProgressDetail d : details) sum += d.getProgressPercentage();
        return sum / details.size();
    }

    private int countCompleted(List<ManagementCourseStudentAdapter.LessonProgressDetail> details) {
        if (details == null) return 0;
        int cnt = 0;
        for (ManagementCourseStudentAdapter.LessonProgressDetail d : details) if (d.isCompleted()) cnt++;
        return cnt;
    }

    /* -------------------- Init / UI -------------------- */

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        imgCourseThumbnail = findViewById(R.id.imgCourseThumbnail);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvRating = findViewById(R.id.tvRating);
        rbRating = findViewById(R.id.rbRating);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvLectureCount = findViewById(R.id.tvLectureCount);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);

        rvSkills = findViewById(R.id.rvSkills);
        rvRequirements = findViewById(R.id.rvRequirements);
        rvStudents = findViewById(R.id.rvStudents);
        rvLessons = findViewById(R.id.rvLessons);
        rvReviews = findViewById(R.id.rvReviews);

        imgStudentExpand = findViewById(R.id.imgStudentExpand);
        imgLessonExpand = findViewById(R.id.imgLessonExpand);
        imgReviewExpand = findViewById(R.id.imgReviewExpand);

        // IMPORTANT: According to your requirement, initial state is "expanded" -> arrow should point UP.
        // We'll set rotation = 180f as the "up" state. If the layout's drawable points down at rotation=0,
        // setting 180f will visually point it up.
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
                // fallback: lấy id từ object course nếu chưa set courseId
                courseId = course.getId();
            }
            if (courseId == null) {
                Toast.makeText(TeacherCourseManagementActivity.this, "Không có khóa học để chỉnh sửa", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(TeacherCourseManagementActivity.this, TeacherCourseEditActivity.class);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
        });

        if (imgStudentExpand != null) imgStudentExpand.setOnClickListener(v -> toggleExpandable(rvStudents, imgStudentExpand));
        if (imgLessonExpand != null) imgLessonExpand.setOnClickListener(v -> toggleExpandable(rvLessons, imgLessonExpand));
        if (imgReviewExpand != null) imgReviewExpand.setOnClickListener(v -> toggleExpandable(rvReviews, imgReviewExpand));
    }

    private void setupAdapters() {
        studentAdapter = new ManagementCourseStudentAdapter((student) -> {
            // click student - placeholder
        });
        studentsLayoutManager = new LinearLayoutManager(this);
        rvStudents.setLayoutManager(studentsLayoutManager);
        rvStudents.setAdapter(studentAdapter);

        lessonAdapter = new ManagementCourseLessonAdapter(new ManagementCourseLessonAdapter.OnLessonActionListener() {
            @Override
            public void onLessonClick(Lesson lesson) {
                navigateToLessonManage(lesson);
            }
        });
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);

        reviewAdapter = new ManagementCourseReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        managementCourseSkillAdapter = new ManagementCourseSkillAdapter();
        skillsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvSkills.setLayoutManager(skillsLayoutManager);
        rvSkills.setAdapter(managementCourseSkillAdapter);

        requirementAdapter = new ManagementCourseRequirementAdapter();
        requirementsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvRequirements.setLayoutManager(requirementsLayoutManager);
        rvRequirements.setAdapter(requirementAdapter);
    }

    private void loadCourseData() {
        if (course == null) return;

        tvCourseTitle.setText(safe(course.getTitle()));
        tvCategory.setText(safe(course.getCategory()));

        // Format price without decimals (VNĐ)
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            tvPrice.setText(df.format((long) course.getPrice()) + " VNĐ");
        } catch (Exception ignored) {
            tvPrice.setText(String.format("%.0f VNĐ", course.getPrice()));
        }

        tvDescription.setText(safe(course.getDescription()));
        tvStudentCount.setText(String.valueOf(course.getStudents()));
        tvRating.setText(String.valueOf(course.getRating()));

        // RatingBar: show up to 5 stars and set real rating
        try { rbRating.setNumStars(5); } catch (Exception ignored) {}
        try { rbRating.setStepSize(0.5f); } catch (Exception ignored) {}
        try { rbRating.setRating((float) course.getRating()); } catch (Exception ignored) {}
        try { rbRating.getProgressDrawable().setColorFilter(getResources().getColor(R.color.yellow_star), PorterDuff.Mode.SRC_ATOP); } catch (Exception ignored) {}

        tvRatingCount.setText("(" + course.getRatingCount() + " đánh giá)");
        tvLectureCount.setText(String.valueOf(course.getLectures()));
        tvTotalDuration.setText(formatDuration(course.getTotalDurationMinutes()));
        tvCreatedAt.setText(safe(course.getCreatedAt()));

        List<String> skills = course.getSkills();
        if (skills != null) managementCourseSkillAdapter.setSkills(skills);
        else managementCourseSkillAdapter.setSkills(new ArrayList<>());

        List<String> requirements = course.getRequirements();
        if (requirements != null) requirementAdapter.setRequirements(requirements);
        else requirementAdapter.setRequirements(new ArrayList<>());

        // thumbnail load: use ImageLoader helper; fallback to placeholder if no URL / failed
        String thumbUrl = extractThumbnailUrl(course);
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            ImageLoader.getInstance().display(thumbUrl, imgCourseThumbnail, R.drawable.ic_image_placeholder, success -> {
                if (!success) {
                    // nếu cần debug thì bật log; production có thể bỏ
                    Log.w(TAG, "ImageLoader failed to load thumbnail: " + thumbUrl);
                }
            });
        } else {
            imgCourseThumbnail.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void navigateToLessonManage(Lesson lesson) {
        if (lesson == null) return;
        Intent intent = new Intent(this, TeacherLessonManagementActivity.class);
        intent.putExtra("lesson_id", lesson.getId());
        intent.putExtra("course_id", lesson.getCourseId());
        startActivity(intent);
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) return hours + " giờ " + mins + " phút";
        return mins + " phút";
    }

    /* -------------------- Reflection helper -------------------- */

    private String extractThumbnailUrl(Course course) {
        if (course == null) return null;
        String[] candidates = {
                "getThumbnailUrl",
                "getThumbnail",
                "getImageUrl",
                "getImage",
                "getCoverUrl",
                "getCover"
        };
        for (String name : candidates) {
            try {
                Method m = course.getClass().getMethod(name);
                Object v = m.invoke(course);
                if (v != null) return v.toString();
            } catch (NoSuchMethodException ignored) {
                // method not present — thử tiếp
            } catch (Exception e) {
                // unexpected error invoking — log và tiếp tục thử candidates khác
                Log.d(TAG, "extractThumbnailUrl failed for " + name + ": " + e.getMessage());
            }
        }
        return null;
    }

    /* -------------------- Listeners registration -------------------- */

    private void registerCourseUpdateListener() {
        try {
            CourseApi courseApi = ApiProvider.getCourseApi();
            if (courseApi == null) return;

            courseUpdateListener = new CourseApi.CourseUpdateListener() {
                @Override
                public void onCourseUpdated(String updatedCourseId, Course updatedCourse) {
                    Log.d(TAG, "onCourseUpdated called, updatedCourseId=" + updatedCourseId + ", updatedCourse=" + (updatedCourse != null ? updatedCourse.getId() : "null"));

                    // Nếu provider gửi null/empty => coi là global change => refresh.
                    if (updatedCourseId == null || updatedCourseId.isEmpty()) {
                        Log.d(TAG, "CourseUpdate: global update received -> refreshing course details and students.");
                        runOnUiThread(() -> {
                            fetchCourseDetail();
                            fetchStudentsFromApi();
                        });
                        return;
                    }

                    // Nếu not related to our course -> ignore
                    if (course != null && !updatedCourseId.equals(course.getId())) {
                        Log.d(TAG, "CourseUpdate for other course (ours=" + (course != null ? course.getId() : "null") + ") -> ignoring.");
                        return;
                    }
                    if (course == null && courseId != null && !updatedCourseId.equals(courseId)) {
                        Log.d(TAG, "CourseUpdate for other course (expected=" + courseId + ") -> ignoring.");
                        return;
                    }

                    runOnUiThread(() -> {
                        if (updatedCourse == null) {
                            Toast.makeText(TeacherCourseManagementActivity.this, "Khóa học đã bị xóa", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // Cập nhật metadata course
                        course = updatedCourse;
                        loadCourseData();

                        // Đồng thời refresh danh sách học viên (nếu có CourseStudentApi thực sự)
                        fetchStudentsFromApi();
                    });
                }
            };

            courseApi.addCourseUpdateListener(courseUpdateListener);
            Log.d(TAG, "CourseUpdateListener registered.");
        } catch (Exception e) {
            Log.w(TAG, "registerCourseUpdateListener failed: " + e.getMessage(), e);
        }
    }

    private void registerLessonUpdateListener() {
        try {
            LessonApi lessonApi = ApiProvider.getLessonApi();
            if (lessonApi == null) return;

            lessonUpdateListener = new LessonApi.LessonUpdateListener() {
                @Override
                public void onLessonUpdated(String lessonId, Lesson updatedLesson) {
                    Log.d(TAG, "onLessonUpdated: lessonId=" + lessonId);
                    // khi lesson thay đổi, fetch lại toàn bộ lessons cho đơn giản (hoặc cập nhật single item nếu muốn)
                    fetchLessonsFromApi();

                    // nếu backend đẩy duration mới, CourseFakeApiService đã adjustCourseDuration; fetch lại Course để sync metadata
                    fetchCourseDetail();

                    // reload students progress vì lessons hoặc order thay đổi
                    fetchStudentsFromApi();
                }
            };
            lessonApi.addLessonUpdateListener(lessonUpdateListener);
            Log.d(TAG, "LessonUpdateListener registered.");
        } catch (Exception ignored) {}
    }

    private void registerLessonProgressListener() {
        try {
            LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();
            if (lpApi == null) return;

            lessonProgressUpdateListener = new LessonProgressApi.LessonProgressUpdateListener() {
                @Override
                public void onLessonProgressChanged(String lessonId) {
                    Log.d(TAG, "onLessonProgressChanged: lessonId=" + lessonId);
                    // khi progress thay đổi, rebuild student list (hoặc tối ưu chỉ update necessary item)
                    runOnUiThread(() -> fetchStudentsFromApi());
                }
            };
            lpApi.addLessonProgressUpdateListener(lessonProgressUpdateListener);
            Log.d(TAG, "LessonProgressUpdateListener registered.");
        } catch (Exception ignored) {}
    }

    /**
     * Đăng ký listener: lắng nghe thay đổi danh sách students
     *
     * Sửa: nếu updatedCourseId == null/empty => hiểu là global change => refresh.
     * Tránh return sớm khi updatedCourseId == null (provider có thể gửi null để notify).
     */
    private void registerCourseStudentListener() {
        try {
            CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
            if (csApi == null) return;

            courseStudentListener = new CourseStudentApi.StudentUpdateListener() {
                @Override
                public void onStudentsChanged(String updatedCourseId) {
                    Log.d(TAG, "onStudentsChanged called, updatedCourseId=" + updatedCourseId);

                    // Nếu provider gửi a specific courseId: chỉ refresh khi khớp.
                    // Nếu provider gửi null/empty: coi là "global change" -> refresh.
                    if (updatedCourseId != null && !updatedCourseId.isEmpty() && courseId != null && !updatedCourseId.equals(courseId)) {
                        // not related to our course -> ignore
                        Log.d(TAG, "StudentUpdate received for other course: " + updatedCourseId + " (ours=" + courseId + ")");
                        return;
                    }

                    Log.d(TAG, "StudentUpdate relevant -> refreshing students for course: " + courseId);
                    runOnUiThread(() -> fetchStudentsFromApi());
                }
            };

            csApi.addStudentUpdateListener(courseStudentListener);
            Log.d(TAG, "CourseStudentListener registered.");
        } catch (Exception e) {
            Log.w(TAG, "registerCourseStudentListener failed: " + e.getMessage(), e);
        }
    }

    /**
     * NEW: Fetch reviews từ ReviewApi và hiển thị
     */
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

    /**
     * NEW: Đăng ký listener để nhận thông báo khi có review mới
     */
    private void registerReviewUpdateListener() {
        try {
            reviewApi = ApiProvider.getReviewApi();
            if (reviewApi == null) return;

            reviewUpdateListener = new com.example.projectonlinecourseeducation.data.coursereview.ReviewApi.ReviewUpdateListener() {
                @Override
                public void onReviewsChanged(String changedCourseId) {
                    Log.d(TAG, "onReviewsChanged called, changedCourseId=" + changedCourseId);

                    // Nếu không liên quan đến course hiện tại -> ignore
                    if (changedCourseId != null && !changedCourseId.isEmpty() && courseId != null && !changedCourseId.equals(courseId)) {
                        return;
                    }

                    // Refresh reviews
                    runOnUiThread(() -> fetchReviewsFromApi());
                }
            };

            reviewApi.addReviewUpdateListener(reviewUpdateListener);
            Log.d(TAG, "ReviewUpdateListener registered.");
        } catch (Exception e) {
            Log.w(TAG, "registerReviewUpdateListener failed: " + e.getMessage(), e);
        }
    }

    /* -------------------- Expand / Animation -------------------- */

    private void toggleExpandable(View content, ImageView icon) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        if (isVisible) {
            // currently expanded -> collapse content
            content.setVisibility(View.GONE);
            // collapsed state should show arrow pointing DOWN (rotation = 0)
            rotateIconTo(icon, 0f);
        } else {
            // currently collapsed -> expand content
            content.setVisibility(View.VISIBLE);
            // expanded state should show arrow pointing UP (rotation = 180)
            rotateIconTo(icon, 180f);
        }
    }

    /**
     * Use property animation to set absolute rotation so the drawable direction always matches the content state.
     */
    private void rotateIconTo(ImageView icon, float to) {
        if (icon == null) return;
        try {
            // cancel any running animation to avoid visual glitches
            icon.animate().cancel();
            icon.animate().rotation(to).setDuration(200).start();
            // Also set the rotation property to the final value in case animation is interrupted
            icon.setRotation(to);
        } catch (Exception e) {
            Log.w(TAG, "rotateIconTo failed: " + e.getMessage(), e);
            try { icon.setRotation(to); } catch (Exception ignored) {}
        }
    }

    /* -------------------- Cleanup -------------------- */

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
