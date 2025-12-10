package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.core.utils.YouTubeUtils;
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
 * Các điểm chính trong bản này:
 * - Tách staged state (local) và persist (gọi API) chỉ khi user xác nhận Lưu.
 * - Dùng OnBackPressedCallback để xử lý back gesture theo AndroidX.
 * - Dùng DialogConfirmHelper cho tất cả dialog confirm (back/save/delete).
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
    private String stagedImageUrl = null; // url user confirmed to preview (but not saved)
    private final List<String> stagedCategoryTags = new ArrayList<>(); // multi-tag staged (final is fine)
    private List<Lesson> localLessons = new ArrayList<>(); // deep-ish copy used for UI + edit
    private Map<String, Lesson> originalLessonsMap = new HashMap<>(); // original snapshot to detect deletes/updates

    // Fixed category list
    private static final String[] FIXED_CATEGORIES = new String[]{
            "Java","JavaScript","Python","C","C++","C#","PHP","SQL","HTML","CSS","TypeScript",
            "Go","Kotlin","Backend","Frontend","Data / AI","Mobile","System","DevOps","Swift",
            "Dart","Rust","Ruby","R","Lua","MATLAB","Scala","Shell / Bash","Haskell","Elixir","Perl"
    };

    // Listener via LessonApi interface (Activity registers on create, removes on destroy)
    private final LessonApi.LessonUpdateListener lessonUpdateListener = new LessonApi.LessonUpdateListener() {
        @Override
        public void onLessonUpdated(String lessonId, Lesson updatedLesson) {
            if (updatedLesson == null) return;
            if (courseId == null) return;
            if (!courseId.equals(updatedLesson.getCourseId())) return;

            runOnUiThread(() -> {
                boolean found = false;
                for (int i = 0; i < localLessons.size(); i++) {
                    Lesson l = localLessons.get(i);
                    if (l.getId() != null && l.getId().equals(lessonId)) {
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
                    localLessons.add(updatedLesson);
                }
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

        // Register back-gesture aware callback (AndroidX)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackWithConfirm();
            }
        });

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
        try {
            lessonApi.removeLessonUpdateListener(lessonUpdateListener);
        } catch (Throwable ignored) {}
    }

    private void initApis() {
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

        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        lessonAdapter = new LessonEditAdapter();
        rvLessons.setAdapter(lessonAdapter);
    }

    private void setupListeners() {
        // UI back button
        btnBack.setOnClickListener(v -> handleBackWithConfirm());

        // Save button shows confirm first
        btnSave.setOnClickListener(v -> {
            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseEditActivity.this,
                    "Xác nhận lưu",
                    "Bạn chắc chắn muốn lưu các thay đổi cho khóa học?",
                    R.drawable.save_check,
                    "Lưu",
                    "Hủy",
                    R.color.blue_700,
                    () -> performSaveCourse()
            );
        });

        btnAddSkill.setOnClickListener(v -> showAddSkillDialog());
        btnAddRequirement.setOnClickListener(v -> showAddRequirementDialog());
        btnAddLesson.setOnClickListener(v -> showLessonDialog(null, -1));

        btnEditImageUrl.setOnClickListener(v -> showEditImageUrlDialog());

        etCategory.setOnClickListener(v -> showCategorySelectDialog());

        lessonAdapter.setOnLessonActionListener(new LessonEditAdapter.OnLessonActionListener() {
            @Override
            public void onEditLesson(Lesson lesson, int position) {
                showLessonDialog(lesson, position);
            }

            @Override
            public void onDeleteLesson(Lesson lesson, int position) {
                DialogConfirmHelper.showConfirmDialog(
                        TeacherCourseEditActivity.this,
                        "Xóa bài học",
                        "Bạn chắc chắn muốn xóa bài học này? Lưu ý: thao tác sẽ được áp dụng sau khi bấm Lưu.",
                        R.drawable.delete_check,
                        "Xóa",
                        "Hủy",
                        R.color.blue_text_primary,
                        () -> {
                            if (position >= 0 && position < localLessons.size()) {
                                localLessons.remove(position);
                            } else {
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
                        }
                );
            }
        });
    }

    /**
     * Xử lý khi user bấm back (UI hoặc gesture). Nếu có thay đổi chưa lưu -> show confirm.
     * Nếu xác nhận, finish(); nếu không thì ở lại.
     */
    private void handleBackWithConfirm() {
        if (hasUnsavedChanges()) {
            DialogConfirmHelper.showConfirmDialog(
                    this,
                    "Thoát mà không lưu?",
                    "Bạn có thay đổi chưa lưu. Nếu thoát, các thay đổi sẽ không được lưu lại.",
                    R.drawable.back_warning_purple,
                    "Thoát",
                    "Hủy",
                    R.color.purple_500,
                    () -> {
                        // user confirmed discard -> finish
                        finish();
                    }
            );
        } else {
            finish();
        }
    }

    private void loadCourseData() {
        currentCourse = courseApi.getCourseDetail(courseId);
        if (currentCourse == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageLoader.getInstance().display(currentCourse.getImageUrl(), imgCourse, R.drawable.ic_image_placeholder);
        stagedImageUrl = null;

        etTitle.setText(currentCourse.getTitle());
        etCategory.setText(currentCourse.getCategory());
        stagedCategoryTags.clear();
        if (currentCourse.getCategory() != null && !currentCourse.getCategory().trim().isEmpty()) {
            String[] parts = currentCourse.getCategory().split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty()) stagedCategoryTags.add(p.trim());
            }
        }

        etPrice.setText(String.valueOf((long) currentCourse.getPrice()));
        etDescription.setText(currentCourse.getDescription());

        populateSkills(currentCourse.getSkills());
        populateRequirements(currentCourse.getRequirements());

        List<Lesson> loaded = lessonApi.getLessonsForCourse(courseId);
        localLessons.clear();
        originalLessonsMap.clear();
        if (loaded != null) {
            for (Lesson l : loaded) {
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

        // Confirm before removing skill row
        btn.setOnClickListener(v -> {
            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseEditActivity.this,
                    "Xóa kỹ năng",
                    "Bạn có chắc chắn muốn xóa kỹ năng này?",
                    R.drawable.delete_check,
                    "Xóa",
                    "Hủy",
                    R.color.blue_text_primary,
                    () -> skillsContainer.removeView(itemLayout)
            );
        });

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

        // Confirm before removing requirement row
        btn.setOnClickListener(v -> {
            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseEditActivity.this,
                    "Xóa yêu cầu",
                    "Bạn có chắc chắn muốn xóa yêu cầu này?",
                    R.drawable.delete_check,
                    "Xóa",
                    "Hủy",
                    R.color.blue_text_primary,
                    () -> requirementsContainer.removeView(itemLayout)
            );
        });

        itemLayout.addView(et);
        itemLayout.addView(btn);
        requirementsContainer.addView(itemLayout);
    }

    private void loadLessons() {
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
     * but we DO NOT persist to courseApi until main Save confirmed.
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
     * Changes are applied to localLessons only (no API call here).
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

            String videoId = YouTubeUtils.extractVideoId(videoInput);
            if (videoId == null) {
                Toast.makeText(TeacherCourseEditActivity.this, "URL/ID video không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson == null) {
                int newOrder = (localLessons == null) ? 1 : (localLessons.size() + 1);
                Lesson newLesson = new Lesson(
                        null, // id null until persisted
                        courseId,
                        title,
                        description,
                        videoId,
                        "Đang tính...", // placeholder
                        newOrder
                );
                if (localLessons == null) localLessons = new ArrayList<>();
                localLessons.add(newLesson);
            } else {
                lesson.setTitle(title);
                lesson.setVideoUrl(videoId);
                lesson.setDuration("Đang tính...");
                lesson.setDescription(description);
            }

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
     * Convenience confirm wrapper if needed elsewhere.
     */
    private void confirmDeleteLessonLocal(Lesson lesson, int position) {
        DialogConfirmHelper.showConfirmDialog(
                this,
                "Xóa bài học",
                "Bạn chắc chắn muốn xóa bài học này? Lưu ý: thao tác sẽ được áp dụng sau khi bấm Lưu.",
                R.drawable.delete_check,
                "Xóa",
                "Hủy",
                R.color.blue_text_primary,
                () -> {
                    if (position >= 0 && position < localLessons.size()) {
                        localLessons.remove(position);
                    } else {
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
                }
        );
    }

    /**
     * Kiểm tra thay đổi chưa lưu (so sánh cơ bản các trường được edit)
     */
    private boolean hasUnsavedChanges() {
        if (currentCourse == null) return false;

        if (!safeString(etTitle.getText().toString()).equals(safeString(currentCourse.getTitle()))) return true;

        String stagedCategoryJoined = String.join(", ", stagedCategoryTags);
        if (!safeString(stagedCategoryJoined).equals(safeString(currentCourse.getCategory()))) return true;

        if (!safeString(etDescription.getText().toString()).equals(safeString(currentCourse.getDescription()))) return true;

        try {
            double enteredPrice = Double.parseDouble(etPrice.getText().toString().trim());
            if (enteredPrice != currentCourse.getPrice()) return true;
        } catch (Exception ignored) {}

        if (stagedImageUrl != null && !stagedImageUrl.equals(currentCourse.getImageUrl())) return true;

        List<String> curSkills = currentCourse.getSkills() != null ? currentCourse.getSkills() : new ArrayList<>();
        List<String> curReqs = currentCourse.getRequirements() != null ? currentCourse.getRequirements() : new ArrayList<>();

        List<String> uiSkills = new ArrayList<>();
        for (int i = 0; i < skillsContainer.getChildCount(); i++) {
            View child = skillsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View subChild = row.getChildAt(j);
                    if (subChild instanceof EditText) {
                        String text = ((EditText) subChild).getText().toString().trim();
                        if (!text.isEmpty()) uiSkills.add(text);
                    }
                }
            }
        }
        if (uiSkills.size() != curSkills.size()) return true;
        for (int i = 0; i < uiSkills.size(); i++) {
            if (!uiSkills.get(i).equals(curSkills.get(i))) return true;
        }

        List<String> uiReqs = new ArrayList<>();
        for (int i = 0; i < requirementsContainer.getChildCount(); i++) {
            View child = requirementsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View subChild = row.getChildAt(j);
                    if (subChild instanceof EditText) {
                        String text = ((EditText) subChild).getText().toString().trim();
                        if (!text.isEmpty()) uiReqs.add(text);
                    }
                }
            }
        }
        if (uiReqs.size() != curReqs.size()) return true;
        for (int i = 0; i < uiReqs.size(); i++) {
            if (!uiReqs.get(i).equals(curReqs.get(i))) return true;
        }

        Set<String> origIds = new HashSet<>(originalLessonsMap.keySet());
        Set<String> curIds = new HashSet<>();
        for (Lesson l : localLessons) {
            if (l.getId() != null) curIds.add(l.getId());
            else curIds.add("NEW#" + l.getOrder() + "#" + l.getTitle());
        }
        if (!origIds.equals(curIds)) return true;

        return false;
    }

    private String safeString(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Tách ra thực hiện lưu (gọi API). Được gọi khi người dùng confirm ở dialog.
     */
    private void performSaveCourse() {
        String title = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

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

            // CRITICAL FIX: Create a NEW Course object instead of modifying currentCourse
            // This prevents changes from being visible immediately before admin approval
            Course updatedCourse = new Course(
                currentCourse.getId(),
                title,
                currentCourse.getTeacher(),
                (stagedImageUrl != null && !stagedImageUrl.trim().isEmpty()) ? stagedImageUrl : currentCourse.getImageUrl(),
                String.join(", ", stagedCategoryTags),
                currentCourse.getLectures(),
                currentCourse.getStudents(),
                currentCourse.getRating(),
                price,
                description,
                currentCourse.getCreatedAt(),
                currentCourse.getRatingCount(),
                currentCourse.getTotalDurationMinutes(),
                skills,
                requirements
            );

            for (int i = 0; i < localLessons.size(); i++) {
                localLessons.get(i).setOrder(i + 1);
            }

            Set<String> originalIds = new HashSet<>(originalLessonsMap.keySet());
            Set<String> newIds = new HashSet<>();
            List<Lesson> toCreate = new ArrayList<>();
            List<Lesson> toUpdate = new ArrayList<>();

            for (Lesson l : localLessons) {
                if (l.getId() == null || l.getId().trim().isEmpty()) {
                    toCreate.add(l);
                } else {
                    newIds.add(l.getId());
                    if (originalLessonsMap.containsKey(l.getId())) {
                        toUpdate.add(l);
                    } else {
                        toCreate.add(l);
                    }
                }
            }

            Set<String> toDeleteIds = new HashSet<>(originalIds);
            toDeleteIds.removeAll(newIds);

            // Update course with the NEW object (not the original)
            courseApi.updateCourse(courseId, updatedCourse);

            // Create new lessons
            for (Lesson c : toCreate) {
                Lesson created = lessonApi.createLesson(c);
                if (created != null && created.getId() != null) {
                    c.setId(created.getId());
                }
            }

            // Update existing lessons
            for (Lesson u : toUpdate) {
                lessonApi.updateLesson(u.getId(), u);
            }

            // Delete removed lessons
            for (String delId : toDeleteIds) {
                lessonApi.deleteLesson(delId);
            }

            // NOTE: removed the success dialog. Show a short toast and finish instead.
            Toast.makeText(this, "Lưu khóa học thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
