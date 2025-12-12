package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.core.utils.YouTubeUtils;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.LessonCreateAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.quiz.QuizDraftDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity tạo khóa học mới (Teacher)
 *
 * Sửa: dùng LessonCreateAdapter + hỗ trợ quiz-draft per local lesson position.
 */
public class TeacherCourseCreateActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnCreate;
    private ImageView imgCoursePreview;
    private View uploadPlaceholder;
    private EditText etImageUrl, etTitle, etPrice, etDescription;
    private Button btnConfirmImage, btnPickCategories, btnAddSkill, btnAddRequirement, btnAddLesson;
    private LinearLayout skillsContainer, requirementsContainer;
    private RecyclerView rvLessons;
    private TextView tvNoLessons;
    private ChipGroup chipGroupSelectedCategories;

    private CourseApi courseApi;
    private LessonApi lessonApi;
    private LessonCreateAdapter lessonAdapter;

    private String stagedImageUrl = null;
    private final List<String> stagedCategoryTags = new ArrayList<>();
    private final List<Lesson> localLessons = new ArrayList<>();

    // Map position -> draft Quiz (used in create-flow)
    private final Map<Integer, Quiz> draftQuizzes = new HashMap<>();

    private static final String[] FIXED_CATEGORIES = new String[]{
            "Java","JavaScript","Python","C","C++","C#","PHP","SQL","HTML","CSS","TypeScript",
            "Go","Kotlin","Backend","Frontend","Data / AI","Mobile","System","DevOps","Swift",
            "Dart","Rust","Ruby","R","Lua","MATLAB","Scala","Shell / Bash","Haskell","Elixir","Perl"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_create);

        initApis();
        bindViews();

        // init RecyclerView + Adapter BEFORE setupListeners to avoid NPE
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        lessonAdapter = new LessonCreateAdapter();
        rvLessons.setAdapter(lessonAdapter);
        lessonAdapter.submitList(new ArrayList<>(localLessons));
        refreshLessonsUi();

        // register back-gesture / back-press callback with OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackWithConfirm();
            }
        });

        // Now it's safe to set listeners (they may reference lessonAdapter)
        setupListeners();
    }

    private void initApis() {
        // Sử dụng ApiProvider để lấy api (có thể là Fake hoặc Remote tùy cấu hình ở Application)
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCreate = findViewById(R.id.btnCreate);
        imgCoursePreview = findViewById(R.id.imgCoursePreview);
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder);
        etImageUrl = findViewById(R.id.etImageUrl);
        btnConfirmImage = findViewById(R.id.btnConfirmImage);
        etTitle = findViewById(R.id.etTitle);
        chipGroupSelectedCategories = findViewById(R.id.chipGroupSelectedCategories);
        btnPickCategories = findViewById(R.id.btnPickCategories);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        skillsContainer = findViewById(R.id.skillsContainer);
        requirementsContainer = findViewById(R.id.requirementsContainer);
        btnAddSkill = findViewById(R.id.btnAddSkill);
        btnAddRequirement = findViewById(R.id.btnAddRequirement);
        btnAddLesson = findViewById(R.id.btnAddLesson);
        rvLessons = findViewById(R.id.rvLessons);
        tvNoLessons = findViewById(R.id.tvNoLessons);
    }

    private void setupListeners() {
        // keep UI back button behavior (calls same confirm logic)
        btnBack.setOnClickListener(v -> handleBackWithConfirm());

        btnConfirmImage.setOnClickListener(v -> {
            String url = etImageUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            stagedImageUrl = url;

            // dùng ImageLoader với callback success/fail
            ImageLoader.getInstance().display(url, imgCoursePreview, R.drawable.ic_image_placeholder, success -> {
                if (success) {
                    if (uploadPlaceholder != null) uploadPlaceholder.setVisibility(View.GONE);
                    imgCoursePreview.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Ảnh đã load thành công. Sẽ lưu khi nhấn Tạo.", Toast.LENGTH_SHORT).show();
                } else {
                    // Load lỗi: hiển thị placeholder image (ImageLoader đã set placeholder), ẩn upload placeholder text
                    if (uploadPlaceholder != null) uploadPlaceholder.setVisibility(View.GONE);
                    imgCoursePreview.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Không tải được ảnh, hiển thị ảnh mặc định.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnPickCategories.setOnClickListener(v -> showCategorySelectDialog());

        btnAddSkill.setOnClickListener(v -> showAddSkillDialog());
        btnAddRequirement.setOnClickListener(v -> showAddRequirementDialog());

        btnAddLesson.setOnClickListener(v -> {
            // add new local lesson
            int order = localLessons.size() + 1;
            Lesson l = new Lesson(null, /*courseId*/ null, "Bài " + order, "", /*videoId*/ "", "Đang tính...", order);
            localLessons.add(l);
            lessonAdapter.submitList(new ArrayList<>(localLessons));
            refreshLessonsUi();
        });

        // Set adapter listener
        if (lessonAdapter != null) {
            lessonAdapter.setOnLessonActionListener(new LessonCreateAdapter.OnLessonActionListener() {
                @Override
                public void onEditLesson(Lesson lesson, int position) {
                    showLessonDialog(lesson, position);
                }

                @Override
                public void onDeleteLesson(Lesson lesson, int position) {
                    // use confirm helper
                    DialogConfirmHelper.showConfirmDialog(
                            TeacherCourseCreateActivity.this,
                            "Xóa bài học",
                            "Bạn có chắc muốn xóa bài học này? (Thao tác chỉ áp dụng khi nhấn Tạo)",
                            R.drawable.delete_check,
                            "Xóa",
                            "Hủy",
                            R.color.blue_300,
                            () -> {
                                if (position >= 0 && position < localLessons.size()) {
                                    localLessons.remove(position);
                                } else {
                                    localLessons.remove(lesson);
                                }
                                // also remove any draft quiz for this position and shift drafts if needed
                                // simplest approach: clear all drafts to avoid position mismatch
                                draftQuizzes.clear();
                                lessonAdapter.submitList(new ArrayList<>(localLessons));
                                refreshLessonsUi();
                            }
                    );
                }

                @Override
                public void onEditQuiz(Lesson lesson, int position) {
                    // open QuizDraftDialogFragment for this position
                    int pos = position;
                    String lessonKey = "POS#" + pos; // used by dialog; we will parse back pos
                    QuizDraftDialogFragment f = QuizDraftDialogFragment.newInstanceForPosition(pos, lessonKey);

                    // prefill if draft exists
                    Quiz existingDraft = draftQuizzes.get(pos);
                    if (existingDraft != null) {
                        f.setInitialDraft(existingDraft);
                    }

                    f.setListener((lk, quiz) -> {
                        // lk is "POS#<pos>"
                        int parsedPos = pos;
                        try {
                            if (lk != null && lk.startsWith("POS#")) {
                                String p = lk.substring(4);
                                parsedPos = Integer.parseInt(p);
                            }
                        } catch (Exception ignored) {}

                        draftQuizzes.put(parsedPos, quiz);
                        Toast.makeText(TeacherCourseCreateActivity.this, "Đã lưu nháp quiz cho bài " + (parsedPos + 1), Toast.LENGTH_SHORT).show();
                        // you may want to update UI to indicate quiz exists for this item
                    });

                    f.show(getSupportFragmentManager(), "quiz_draft_" + lessonKey);
                }
            });
        }

        btnCreate.setOnClickListener(v -> {
            // show confirm before creating; include info about draft quizzes (they will be saved)
            String title = "Xác nhận tạo khóa học";
            String baseMsg = "Bạn có chắc muốn tạo khóa học với thông tin hiện tại?";
            String createMsg = buildCreateConfirmMessage(baseMsg);

            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseCreateActivity.this,
                    title,
                    createMsg,
                    R.drawable.save_create_course,
                    "Tạo",
                    "Hủy",
                    R.color.blue_700,
                    () -> {
                        boolean ok = performCreateCourse();
                        if (ok) {
                            DialogConfirmHelper.showSuccessDialog(
                                    TeacherCourseCreateActivity.this,
                                    "Tạo khóa học thành công",
                                    "Khóa học đã được tạo thành công.",
                                    R.drawable.confirm_success,
                                    "Đóng",
                                    () -> finish()
                            );
                        }
                    }
            );
        });
    }

    private void showAddSkillDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Thêm kỹ năng");
        final EditText input = new EditText(this);
        input.setHint("Nhập kỹ năng");
        b.setView(input);
        b.setPositiveButton("Thêm", (d, w) -> {
            String s = input.getText().toString().trim();
            if (!s.isEmpty()) addSkillRow(s);
        });
        b.setNegativeButton("Hủy", (d, w) -> d.cancel());
        b.show();
    }

    private void showAddRequirementDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Thêm yêu cầu");
        final EditText input = new EditText(this);
        input.setHint("Nhập yêu cầu");
        b.setView(input);
        b.setPositiveButton("Thêm", (d, w) -> {
            String s = input.getText().toString().trim();
            if (!s.isEmpty()) addRequirementRow(s);
        });
        b.setNegativeButton("Hủy", (d, w) -> d.cancel());
        b.show();
    }

    private void addSkillRow(String skill) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        EditText et = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        et.setLayoutParams(lp);
        et.setText(skill);
        et.setHint("Kỹ năng");

        Button btnDel = new Button(this);
        btnDel.setText("X");
        btnDel.setOnClickListener(v -> {
            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseCreateActivity.this,
                    "Xóa kỹ năng",
                    "Bạn có chắc muốn xóa kỹ năng này?",
                    R.drawable.delete_check,
                    "Xóa",
                    "Hủy",
                    R.color.blue_300,
                    () -> skillsContainer.removeView(row)
            );
        });

        row.addView(et);
        row.addView(btnDel);
        skillsContainer.addView(row);
    }

    private void addRequirementRow(String req) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        EditText et = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        et.setLayoutParams(lp);
        et.setText(req);
        et.setHint("Yêu cầu");

        Button btnDel = new Button(this);
        btnDel.setText("X");
        btnDel.setOnClickListener(v -> {
            DialogConfirmHelper.showConfirmDialog(
                    TeacherCourseCreateActivity.this,
                    "Xóa yêu cầu",
                    "Bạn có chắc muốn xóa yêu cầu này?",
                    R.drawable.delete_check,
                    "Xóa",
                    "Hủy",
                    R.color.blue_300,
                    () -> requirementsContainer.removeView(row)
            );
        });

        row.addView(et);
        row.addView(btnDel);
        requirementsContainer.addView(row);
    }

    private void showCategorySelectDialog() {
        boolean[] checked = new boolean[FIXED_CATEGORIES.length];
        for (int i = 0; i < FIXED_CATEGORIES.length; i++) {
            checked[i] = stagedCategoryTags.contains(FIXED_CATEGORIES[i]);
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Chọn danh mục (có thể chọn nhiều)");
        b.setMultiChoiceItems(FIXED_CATEGORIES, checked, (dialog, which, isChecked) -> checked[which] = isChecked);

        b.setPositiveButton("Xác nhận", (d, w) -> {
            stagedCategoryTags.clear();
            for (int i = 0; i < FIXED_CATEGORIES.length; i++) {
                if (checked[i]) stagedCategoryTags.add(FIXED_CATEGORIES[i]);
            }
            renderSelectedCategoryChips();
            Toast.makeText(this, "Danh mục đã chọn (chưa lưu).", Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Hủy", (d, w) -> d.cancel());
        b.show();
    }

    private void renderSelectedCategoryChips() {
        chipGroupSelectedCategories.removeAllViews();
        for (String tag : stagedCategoryTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                // confirm before removing category
                DialogConfirmHelper.showConfirmDialog(
                        TeacherCourseCreateActivity.this,
                        "Xóa danh mục",
                        "Bạn có chắc muốn xóa danh mục này?",
                        R.drawable.delete_check,
                        "Xóa",
                        "Hủy",
                        R.color.blue_300,
                        () -> {
                            stagedCategoryTags.remove(tag);
                            chipGroupSelectedCategories.removeView(chip);
                        }
                );
            });
            chipGroupSelectedCategories.addView(chip);
        }
    }

    private void showLessonDialog(Lesson lesson, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(lesson == null ? "Tạo bài học" : "Chỉnh sửa bài học");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        final EditText etTitleLocal = new EditText(this);
        etTitleLocal.setHint("Tên bài học");
        if (lesson != null) etTitleLocal.setText(lesson.getTitle());
        layout.addView(etTitleLocal);

        final EditText etVideo = new EditText(this);
        etVideo.setHint("Video URL hoặc ID");
        if (lesson != null) etVideo.setText(lesson.getVideoUrl());
        layout.addView(etVideo);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Mô tả (tùy chọn)");
        etDesc.setLines(3);
        if (lesson != null) etDesc.setText(lesson.getDescription());
        layout.addView(etDesc);

        TextView tvNote = new TextView(this);
        tvNote.setText("Thời lượng sẽ được tính tự động sau khi tạo bài (backend/fake API sẽ cập nhật).");
        layout.addView(tvNote);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (d, w) -> {
            String title = etTitleLocal.getText().toString().trim();
            String videoInput = etVideo.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (title.isEmpty() || videoInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề và video", Toast.LENGTH_SHORT).show();
                return;
            }

            String videoId = YouTubeUtils.extractVideoId(videoInput);
            if (videoId == null) {
                Toast.makeText(this, "URL/ID video không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson == null) {
                int order = localLessons.size() + 1;
                Lesson l = new Lesson(null, /*courseId*/ null, title, desc, videoId, "Đang tính...", order);
                localLessons.add(l);
            } else {
                // edit local
                lesson.setTitle(title);
                lesson.setVideoUrl(videoId);
                lesson.setDescription(desc);
                lesson.setDuration("Đang tính...");
            }
            lessonAdapter.submitList(new ArrayList<>(localLessons));
            refreshLessonsUi();
        });

        builder.setNegativeButton("Hủy", (d, w) -> d.cancel());
        builder.show();
    }

    private void refreshLessonsUi() {
        if (localLessons.isEmpty()) {
            rvLessons.setVisibility(View.GONE);
            tvNoLessons.setVisibility(View.VISIBLE);
        } else {
            rvLessons.setVisibility(View.VISIBLE);
            tvNoLessons.setVisibility(View.GONE);
        }
    }

    private boolean performCreateCourse() {
        String title = etTitle.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Tên khóa học không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (stagedCategoryTags.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // collect skills
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < skillsContainer.getChildCount(); i++) {
            View row = skillsContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) row).getChildCount(); j++) {
                    View child = ((LinearLayout) row).getChildAt(j);
                    if (child instanceof EditText) {
                        String t = ((EditText) child).getText().toString().trim();
                        if (!t.isEmpty()) skills.add(t);
                    }
                }
            }
        }

        // collect requirements
        List<String> requirements = new ArrayList<>();
        for (int i = 0; i < requirementsContainer.getChildCount(); i++) {
            View row = requirementsContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) row).getChildCount(); j++) {
                    View child = ((LinearLayout) row).getChildAt(j);
                    if (child instanceof EditText) {
                        String t = ((EditText) child).getText().toString().trim();
                        if (!t.isEmpty()) requirements.add(t);
                    }
                }
            }
        }

        // Lấy tên giáo viên từ Auth API (nếu có)
        String teacherName = "TÊN_GIÁO_VIÊN_PLACEHOLDER";
        try {
            AuthApi a = ApiProvider.getAuthApi();
            if (a != null) {
                User u = a.getCurrentUser();
                if (u != null && u.getName() != null && !u.getName().trim().isEmpty()) {
                    teacherName = u.getName();
                }
            }
        } catch (Exception ignored) {}

        // Khởi tạo Course
        Course newCourse = new Course(
                null, // id - createCourse assigns it
                title,
                teacherName,
                stagedImageUrl != null ? stagedImageUrl : "",
                String.join(", ", stagedCategoryTags),
                0, // lecture count: backend will compute
                0,
                0.0,
                price,
                desc,
                "",
                0,
                0,
                skills,
                requirements
        );

        // Create course via CourseApi
        Course created = courseApi.createCourse(newCourse);
        if (created == null || created.getId() == null) {
            Toast.makeText(this, "Tạo khóa học thất bại", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Create lessons for this course via LessonApi and persist draft quizzes (if any)
        for (int i = 0; i < localLessons.size(); i++) {
            Lesson l = localLessons.get(i);
            l.setCourseId(created.getId());
            l.setOrder(i + 1);
            Lesson createdLesson = lessonApi.createLesson(l);
            if (createdLesson != null && createdLesson.getId() != null) {
                l.setId(createdLesson.getId());
                l.setDuration(createdLesson.getDuration());
                // if there is a draft quiz for this position -> persist it
                Quiz draft = draftQuizzes.get(i);
                if (draft != null) {
                    // attach lessonId and call createQuiz
                    Quiz toSave = new Quiz(null, createdLesson.getId(), draft.getTitle(), draft.getQuestions());
                    try {
                        ApiProvider.getLessonQuizApi().createQuiz(toSave);
                    } catch (Throwable ignored) {}
                }
            }
        }

        Toast.makeText(this, "Tạo khóa học thành công", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void handleBackWithConfirm() {
        if (hasUnsavedChanges()) {
            // Build message including info about draft quizzes (they will be lost if leaving)
            String title = "Thoát mà chưa lưu";
            String msg = buildLeaveConfirmMessage();

            DialogConfirmHelper.showConfirmDialog(
                    this,
                    title,
                    msg,
                    R.drawable.back_warning_purple,
                    "Rời đi",
                    "Ở lại",
                    R.color.purple_500,
                    () -> finish()
            );
        } else {
            finish();
        }
    }

    private boolean hasUnsavedChanges() {
        if (stagedImageUrl != null && !stagedImageUrl.isEmpty()) return true;
        if (!stagedCategoryTags.isEmpty()) return true;
        if (!localLessons.isEmpty()) return true;
        if (!draftQuizzes.isEmpty()) return true; // drafts present
        if (!etTitle.getText().toString().trim().isEmpty()) return true;
        if (!etPrice.getText().toString().trim().isEmpty()) return true;
        if (!etDescription.getText().toString().trim().isEmpty()) return true;
        if (skillsContainer.getChildCount() > 0) return true;
        if (requirementsContainer.getChildCount() > 0) return true;
        return false;
    }

    /**
     * Xây dựng nội dung thông báo khi rời đi mà chưa lưu.
     * Nếu có draft quizzes, liệt kê số lượng và các vị trí bài học (1-based) có nháp.
     * Thông báo rõ là nháp quiz sẽ bị mất nếu rời đi.
     */
    /**
     * Xây dựng nội dung thông báo khi rời đi mà chưa lưu.
     * Nếu có draft quizzes, chỉ hiển thị 1 câu cảnh báo ngắn gọn (không liệt kê chi tiết).
     */
    private String buildLeaveConfirmMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Có thay đổi chưa được lưu. Rời đi sẽ không lưu những thay đổi này.");
        return sb.toString();
    }

    /**
     * Xây dựng nội dung thông báo khi xác nhận tạo khóa học.
     * Nếu có draft quizzes, chỉ thêm 1 câu ngắn gọn báo rằng nháp sẽ được lưu.
     */
    private String buildCreateConfirmMessage(String baseMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append(baseMsg);
        return sb.toString();
    }
}
