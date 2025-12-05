package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View; // <--- THÊM IMPORT NÀY (quan trọng)
import android.widget.Button;
import android.widget.EditText;
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
import com.example.projectonlinecourseeducation.core.utils.YouTubeUtils; // <-- dùng YouTubeUtils thay VideoDurationHelper
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.LessonEditAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity chỉnh sửa khóa học dành cho teacher.
 *
 * Sửa logic:
 * - Tách biệt "staged" changes (local) và "persist" changes (gọi API) chỉ khi user bấm Save.
 * - Activity chỉ giao tiếp với LessonApi (interface). Không gọi các phương thức dev-specific.
 * - Đăng ký listener qua LessonApi để nhận update (duration được cập nhật).
 */
public class TeacherCourseEditActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSave;
    private ImageView imgCourse;
    private ImageButton btnEditImageUrl;
    private EditText etTitle, etCategory, etPrice, etDescription;
    private LinearLayout skillsContainer, requirementsContainer;
    private Button btnAddSkill, btnAddRequirement, btnAddLesson;
    private RecyclerView rvLessons;
    private TextView tvNoLessons;

    private CourseApi courseApi;
    private LessonApi lessonApi;

    private Course currentCourse;
    private String courseId;
    private LessonEditAdapter lessonAdapter;

    // --- Local staged state (chưa persist) ---
    private String stagedImageUrl = null;        // url user confirmed to preview (but not saved)
    private List<String> stagedCategoryTags = new ArrayList<>(); // multi-tag staged
    private List<Lesson> localLessons = new ArrayList<>(); // deep-ish copy used for UI + edit
    private Map<String, Lesson> originalLessonsMap = new HashMap<>(); // original snapshot to detect deletes/updates

    // Fixed category list (the one you requested). You can extend later.
    private static final String[] FIXED_CATEGORIES = new String[]{
            "Java","JavaScript","Python","C","C++","C#","PHP","SQL","HTML","CSS","TypeScript",
            "Go","Kotlin","Backend","Frontend","Data / AI","Mobile","System","DevOps","Swift",
            "Dart","Rust","Ruby","R","Lua","MATLAB","Scala","Shell / Bash","Haskell","Elixir","Perl"
    };

    // Listener via LessonApi interface (Activity registers on create, removes on destroy)
    private final LessonApi.LessonUpdateListener lessonUpdateListener = new LessonApi.LessonUpdateListener() {
        @Override
        public void onLessonUpdated(String lessonId, Lesson updatedLesson) {
            // Ensure update affects current course
            if (updatedLesson == null) return;
            if (courseId == null) return;
            if (!courseId.equals(updatedLesson.getCourseId())) return;

            // Update localLessons on UI thread
            runOnUiThread(() -> {
                boolean found = false;
                for (int i = 0; i < localLessons.size(); i++) {
                    Lesson l = localLessons.get(i);
                    if (l.getId() != null && l.getId().equals(lessonId)) {
                        // update fields that may have changed (e.g. duration)
                        l.setTitle(updatedLesson.getTitle());
                        l.setDescription(updatedLesson.getDescription());
                        l.setVideoUrl(updatedLesson.getVideoUrl());
                        l.setDuration(updatedLesson.getDuration());
                        l.setOrder(updatedLesson.getOrder());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // If local list doesn't contain it (maybe created by API), add it and reindex
                    localLessons.add(updatedLesson);
                }
                // Reindex orders to keep UI consistent
                for (int i = 0; i < localLessons.size(); i++) {
                    localLessons.get(i).setOrder(i + 1);
                }
                lessonAdapter.submitList(new ArrayList<>(localLessons));
                updateLessonsVisibility();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_edit);

        initApis();
        initViews();
        setupListeners();

        // Register listener on LessonApi (UI doesn't care whether the provider is fake or remote)
        try {
            lessonApi.addLessonUpdateListener(lessonUpdateListener);
        } catch (Throwable ignored) {}

        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            loadCourseData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener
        try {
            lessonApi.removeLessonUpdateListener(lessonUpdateListener);
        } catch (Throwable ignored) {}
    }

    /**
     * Helper: attempt to detect debug build at runtime without referencing BuildConfig symbol
     * directly. This avoids "Cannot resolve symbol BuildConfig" in weird module setups.
     *
     * It tries to load {package}.BuildConfig and read the static boolean DEBUG field.
     * If anything fails, returns false.
     */
    private boolean isDebugBuild() {
        try {
            Class<?> bc = Class.forName(getPackageName() + ".BuildConfig");
            return bc.getField("DEBUG").getBoolean(null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void initApis() {
        // CHANGED: use ApiProvider so we can swap implementations later (fake vs remote)
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        imgCourse = findViewById(R.id.imgCourseEdit);
        btnEditImageUrl = findViewById(R.id.btnEditImageUrl);
        etTitle = findViewById(R.id.etTitle);
        etCategory = findViewById(R.id.etCategory);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        skillsContainer = findViewById(R.id.skillsContainer);
        requirementsContainer = findViewById(R.id.requirementsContainer);
        btnAddSkill = findViewById(R.id.btnAddSkill);
        btnAddRequirement = findViewById(R.id.btnAddRequirement);
        btnAddLesson = findViewById(R.id.btnAddLesson);
        rvLessons = findViewById(R.id.rvLessons);
        tvNoLessons = findViewById(R.id.tvNoLessons);

        // Setup lessons RecyclerView
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        lessonAdapter = new LessonEditAdapter();
        rvLessons.setAdapter(lessonAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveCourse());
        btnAddSkill.setOnClickListener(v -> showAddSkillDialog());
        btnAddRequirement.setOnClickListener(v -> showAddRequirementDialog());
        btnAddLesson.setOnClickListener(v -> showLessonDialog(null, -1));

        // Edit image overlay
        btnEditImageUrl.setOnClickListener(v -> showEditImageUrlDialog());

        // Category click -> open checklist
        etCategory.setOnClickListener(v -> showCategorySelectDialog());

        lessonAdapter.setOnLessonActionListener(new LessonEditAdapter.OnLessonActionListener() {
            @Override
            public void onEditLesson(Lesson lesson, int position) {
                showLessonDialog(lesson, position);
            }

            @Override
            public void onDeleteLesson(Lesson lesson, int position) {
                confirmDeleteLessonLocal(lesson, position);
            }
        });
    }

    private void loadCourseData() {
        currentCourse = courseApi.getCourseDetail(courseId);
        if (currentCourse == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load course basic info
        // Use placeholder if image invalid — ImageLoader returns placeholder if URL broken (we set placeholder first)
        ImageLoader.getInstance().display(currentCourse.getImageUrl(), imgCourse, R.drawable.ic_image_placeholder);
        // stagedImageUrl null ban đầu (người dùng chưa confirm edit). Khi Save, sẽ set vào currentCourse.
        stagedImageUrl = null;

        etTitle.setText(currentCourse.getTitle());
        etCategory.setText(currentCourse.getCategory());
        // initialize stagedCategoryTags from existing course category split by ','
        stagedCategoryTags.clear();
        if (currentCourse.getCategory() != null && !currentCourse.getCategory().trim().isEmpty()) {
            String[] parts = currentCourse.getCategory().split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty()) stagedCategoryTags.add(p.trim());
            }
        }

        etPrice.setText(String.valueOf((long) currentCourse.getPrice()));
        etDescription.setText(currentCourse.getDescription());

        // Load skills & requirements into UI (these are EditText rows)
        populateSkills(currentCourse.getSkills());
        populateRequirements(currentCourse.getRequirements());

        // Load lessons — BUT create local copy; DO NOT modify global API until Save
        List<Lesson> loaded = lessonApi.getLessonsForCourse(courseId);
        localLessons.clear();
        originalLessonsMap.clear();
        if (loaded != null) {
            for (Lesson l : loaded) {
                // create a shallow copy of lesson object (we want local mutability)
                Lesson copy = new Lesson(
                        l.getId(),
                        l.getCourseId(),
                        l.getTitle(),
                        l.getDescription(),
                        l.getVideoUrl(),
                        l.getDuration(),
                        l.getOrder()
                );
                localLessons.add(copy);
                if (copy.getId() != null) originalLessonsMap.put(copy.getId(), copy);
            }
        }
        lessonAdapter.submitList(new ArrayList<>(localLessons));
        updateLessonsVisibility();
    }

    private void populateSkills(List<String> skills) {
        skillsContainer.removeAllViews();
        if (skills != null) {
            for (String skill : skills) {
                addSkillView(skill);
            }
        }
    }

    private void populateRequirements(List<String> requirements) {
        requirementsContainer.removeAllViews();
        if (requirements != null) {
            for (String req : requirements) {
                addRequirementView(req);
            }
        }
    }

    private void addSkillView(String skill) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 8, 0, 8);

        EditText et = new EditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        et.setText(skill);
        et.setHint("Kỹ năng");

        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setText("X");
        btn.setOnClickListener(v -> skillsContainer.removeView(itemLayout));

        itemLayout.addView(et);
        itemLayout.addView(btn);
        skillsContainer.addView(itemLayout);
    }

    private void addRequirementView(String requirement) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 8, 0, 8);

        EditText et = new EditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        et.setText(requirement);
        et.setHint("Yêu cầu");

        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setText("X");
        btn.setOnClickListener(v -> requirementsContainer.removeView(itemLayout));

        itemLayout.addView(et);
        itemLayout.addView(btn);
        requirementsContainer.addView(itemLayout);
    }

    private void loadLessons() {
        // localLessons already loaded in loadCourseData()
        lessonAdapter.submitList(new ArrayList<>(localLessons));
        updateLessonsVisibility();
    }

    private void updateLessonsVisibility() {
        if (localLessons == null || localLessons.isEmpty()) {
            rvLessons.setVisibility(View.GONE);
            tvNoLessons.setVisibility(View.VISIBLE);
        } else {
            rvLessons.setVisibility(View.VISIBLE);
            tvNoLessons.setVisibility(View.GONE);
        }
    }

    private void showAddSkillDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm kỹ năng");

        EditText input = new EditText(this);
        input.setHint("Nhập kỹ năng");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String skill = input.getText().toString().trim();
            if (!skill.isEmpty()) {
                addSkillView(skill);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showAddRequirementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm yêu cầu");

        EditText input = new EditText(this);
        input.setHint("Nhập yêu cầu");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String req = input.getText().toString().trim();
            if (!req.isEmpty()) {
                addRequirementView(req);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Show dialog to input image URL. When user confirms -> we preview it using ImageLoader
     * but we DO NOT persist to courseApi until main Save clicked.
     */
    private void showEditImageUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa ảnh (nhập URL)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("https://...");
        builder.setView(input);

        builder.setPositiveButton("Xác nhận & Xem thử", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                // Stage the image url and preview it
                stagedImageUrl = url;
                ImageLoader.getInstance().display(url, imgCourse, R.drawable.ic_image_placeholder);
                Toast.makeText(this, "Ảnh đã được tải xem trước. Bấm Lưu để lưu thay đổi.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "URL rỗng", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Multi-select category dialog from fixed list.
     * Staged selection applied to etCategory.text but not persisted until Save.
     */
    private void showCategorySelectDialog() {
        boolean[] checked = new boolean[FIXED_CATEGORIES.length];
        // pre-check stagedCategoryTags
        for (int i = 0; i < FIXED_CATEGORIES.length; i++) {
            checked[i] = stagedCategoryTags.contains(FIXED_CATEGORIES[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục (có thể chọn nhiều)");

        builder.setMultiChoiceItems(FIXED_CATEGORIES, checked, (dialog, which, isChecked) -> {
            checked[which] = isChecked;
        });

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            stagedCategoryTags.clear();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < FIXED_CATEGORIES.length; i++) {
                if (checked[i]) {
                    stagedCategoryTags.add(FIXED_CATEGORIES[i]);
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(FIXED_CATEGORIES[i]);
                }
            }
            etCategory.setText(sb.toString());
            Toast.makeText(this, "Danh mục đã được chọn (chưa lưu).", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Show dialog to create or edit a lesson.
     * IMPORTANT: changes are applied to localLessons only (no API call here).
     *
     * NOTE: Duration input removed — duration will be computed by the backend / fake API.
     * We set a placeholder "Đang tính..." locally and rely on LessonApi implementation
     * to compute and push the real duration via LessonUpdateListener.
     */
    private void showLessonDialog(Lesson lesson, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(lesson == null ? "Tạo bài học mới" : "Chỉnh sửa bài học");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etLessonTitle = new EditText(this);
        etLessonTitle.setHint("Tên bài học");
        if (lesson != null) etLessonTitle.setText(lesson.getTitle());
        layout.addView(etLessonTitle);

        EditText etVideoUrl = new EditText(this);
        etVideoUrl.setHint("Video URL (videoId hoặc link)");
        if (lesson != null) etVideoUrl.setText(lesson.getVideoUrl());
        layout.addView(etVideoUrl);

        // Remove editable duration input (we compute it automatically)
        // Instead, show a small helper TextView to inform user.
        TextView tvDurationNote = new TextView(this);
        tvDurationNote.setText("Thời lượng sẽ được tính tự động sau khi lưu (backend sẽ trả).");
        tvDurationNote.setPadding(0, 12, 0, 12);
        layout.addView(tvDurationNote);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Mô tả");
        etDescription.setLines(3);
        if (lesson != null) etDescription.setText(lesson.getDescription());
        layout.addView(etDescription);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String title = etLessonTitle.getText().toString().trim();
            String videoInput = etVideoUrl.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty() || videoInput.isEmpty()) {
                Toast.makeText(TeacherCourseEditActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // CHUẨN HÓA: extract videoId từ input (id hoặc url)
            String videoId = YouTubeUtils.extractVideoId(videoInput);
            if (videoId == null) {
                Toast.makeText(TeacherCourseEditActivity.this, "URL/ID video không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson == null) {
                // Create new local lesson (id null => indicates new)
                int newOrder = (localLessons == null) ? 1 : (localLessons.size() + 1);
                Lesson newLesson = new Lesson(
                        null, // id null until persisted
                        courseId,
                        title,
                        description,
                        videoId, // store videoId (not full url)
                        "Đang tính...", // placeholder duration — will be updated by API later
                        newOrder
                );
                if (localLessons == null) localLessons = new ArrayList<>();
                localLessons.add(newLesson);
            } else {
                // Update existing local lesson (mutable)
                lesson.setTitle(title);
                lesson.setVideoUrl(videoId); // update to videoId
                // don't allow manual duration change — reset to placeholder to trigger recompute later
                lesson.setDuration("Đang tính...");
                lesson.setDescription(description);
                // order kept as current position — we will reindex on Save
            }

            // Update adapter immediately so user sees the placeholder state
            lessonAdapter.submitList(new ArrayList<>(localLessons));
            updateLessonsVisibility();
            Toast.makeText(TeacherCourseEditActivity.this,
                    "Đã lưu cục bộ bài học (thời lượng sẽ được tính tự động sau khi lưu lên server).",
                    Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Xóa lesson ở local (không gọi lessonApi.deleteLesson ở đây).
     * Việc xóa sẽ được sync lên API khi bấm Save.
     */
    private void confirmDeleteLessonLocal(Lesson lesson, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài học")
                .setMessage("Bạn chắc chắn muốn xóa bài học này? Lưu ý: thao tác sẽ được áp dụng sau khi bấm Lưu.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (position >= 0 && position < localLessons.size()) {
                        localLessons.remove(position);
                    } else {
                        // fallback: remove by id or reference
                        for (int i = 0; i < localLessons.size(); i++) {
                            Lesson l = localLessons.get(i);
                            if ((l.getId() != null && l.getId().equals(lesson.getId())) || l == lesson) {
                                localLessons.remove(i);
                                break;
                            }
                        }
                    }
                    lessonAdapter.submitList(new ArrayList<>(localLessons));
                    updateLessonsVisibility();
                    Toast.makeText(TeacherCourseEditActivity.this, "Đã xóa cục bộ. Bấm Lưu để áp dụng.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Save tất cả thay đổi: course fields, staged image, staged categories, skills/requirements,
     * và sync lesson changes (create/update/delete + reorder).
     */
    private void saveCourse() {
        String title = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim(); // staged text already set
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            // Collect skills
            List<String> skills = new ArrayList<>();
            for (int i = 0; i < skillsContainer.getChildCount(); i++) {
                View child = skillsContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    for (int j = 0; j < row.getChildCount(); j++) {
                        View subChild = row.getChildAt(j);
                        if (subChild instanceof EditText) {
                            String text = ((EditText) subChild).getText().toString().trim();
                            if (!text.isEmpty()) {
                                skills.add(text);
                            }
                        }
                    }
                }
            }

            // Collect requirements
            List<String> requirements = new ArrayList<>();
            for (int i = 0; i < requirementsContainer.getChildCount(); i++) {
                View child = requirementsContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    for (int j = 0; j < row.getChildCount(); j++) {
                        View subChild = row.getChildAt(j);
                        if (subChild instanceof EditText) {
                            String text = ((EditText) subChild).getText().toString().trim();
                            if (!text.isEmpty()) {
                                requirements.add(text);
                            }
                        }
                    }
                }
            }

            // -------------- Update Course object (in-memory) ----------------
            currentCourse.setTitle(title);
            // Category: use stagedCategoryTags (more reliable than free text)
            String joinedCategories = String.join(", ", stagedCategoryTags);
            currentCourse.setCategory(joinedCategories);
            currentCourse.setPrice(price);
            currentCourse.setDescription(description);
            currentCourse.setSkills(skills);
            currentCourse.setRequirements(requirements);

            // If user staged a new image url, persist it now
            if (stagedImageUrl != null && !stagedImageUrl.trim().isEmpty()) {
                currentCourse.setImageUrl(stagedImageUrl);
            }

            // -------------- Sync Lessons ----------------
            // 1) Reindex localLessons order sequentially 1..n
            for (int i = 0; i < localLessons.size(); i++) {
                localLessons.get(i).setOrder(i + 1);
            }

            // 2) Build maps to detect create/update/delete
            // originalLessonsMap contains snapshot from loadCourseData (ids present)
            Set<String> originalIds = new HashSet<>(originalLessonsMap.keySet());
            Set<String> newIds = new HashSet<>();
            List<Lesson> toCreate = new ArrayList<>();
            List<Lesson> toUpdate = new ArrayList<>();

            for (Lesson l : localLessons) {
                if (l.getId() == null || l.getId().trim().isEmpty()) {
                    // new lesson -> create
                    toCreate.add(l);
                } else {
                    newIds.add(l.getId());
                    // we consider update for any lesson that exists in original (simple approach)
                    if (originalLessonsMap.containsKey(l.getId())) {
                        toUpdate.add(l);
                    } else {
                        // Id present but not in original snapshot -> also treat as create for safety
                        toCreate.add(l);
                    }
                }
            }

            // Deleted ids = originalIds - newIds
            Set<String> toDeleteIds = new HashSet<>(originalIds);
            toDeleteIds.removeAll(newIds);

            // 3) Persist changes to API (course + lessons)
            // Note: Course update first so course exists (in case createLesson requires course)
            courseApi.updateCourse(courseId, currentCourse);

            // Create new lessons via lessonApi.createLesson -> important: lessonApi will assign id if needed
            for (Lesson c : toCreate) {
                Lesson created = lessonApi.createLesson(c);
                // update local object id if API returned assigned id
                if (created != null && created.getId() != null) {
                    c.setId(created.getId());
                }
            }

            // Update existing lessons
            for (Lesson u : toUpdate) {
                lessonApi.updateLesson(u.getId(), u);
            }

            // Delete lessons removed
            for (String delId : toDeleteIds) {
                lessonApi.deleteLesson(delId);
            }

            Toast.makeText(this, "Lưu khóa học thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
