package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View; // <--- THÊM IMPORT NÀY (quan trọng)
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.course.CourseFakeApiService;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonFakeApiService;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.LessonEditAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity chỉnh sửa khóa học dành cho teacher.
 * Giữ nguyên logic gốc — chỉ sửa import / cast / kiểm tra kiểu để tránh lỗi biên dịch.
 */
public class TeacherCourseEditActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSave;
    private ImageView imgCourse;
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
    private List<Lesson> lessons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_edit);

        initApis();
        initViews();
        setupListeners();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            loadCourseData();
        }
    }

    private void initApis() {
        courseApi = CourseFakeApiService.getInstance();
        lessonApi = LessonFakeApiService.getInstance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        imgCourse = findViewById(R.id.imgCourseEdit);
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

        lessonAdapter.setOnLessonActionListener(new LessonEditAdapter.OnLessonActionListener() {
            @Override
            public void onEditLesson(Lesson lesson, int position) {
                showLessonDialog(lesson, position);
            }

            @Override
            public void onDeleteLesson(Lesson lesson, int position) {
                confirmDeleteLesson(lesson, position);
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
        ImageLoader.getInstance().display(currentCourse.getImageUrl(), imgCourse, R.drawable.ic_image_placeholder);
        etTitle.setText(currentCourse.getTitle());
        etCategory.setText(currentCourse.getCategory());
        etPrice.setText(String.valueOf((long) currentCourse.getPrice()));
        etDescription.setText(currentCourse.getDescription());

        // Load skills
        populateSkills(currentCourse.getSkills());

        // Load requirements
        populateRequirements(currentCourse.getRequirements());

        // Load lessons
        loadLessons();
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
        lessons = lessonApi.getLessonsForCourse(courseId);
        lessonAdapter.submitList(lessons);
        updateLessonsVisibility();
    }

    private void updateLessonsVisibility() {
        if (lessons == null || lessons.isEmpty()) {
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
        etVideoUrl.setHint("Video URL");
        if (lesson != null) etVideoUrl.setText(lesson.getVideoUrl());
        layout.addView(etVideoUrl);

        EditText etDuration = new EditText(this);
        etDuration.setHint("Thời lượng (vd: 09:30)");
        if (lesson != null) etDuration.setText(lesson.getDuration());
        layout.addView(etDuration);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Mô tả");
        etDescription.setLines(3);
        if (lesson != null) etDescription.setText(lesson.getDescription());
        layout.addView(etDescription);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String title = etLessonTitle.getText().toString().trim();
            String videoUrl = etVideoUrl.getText().toString().trim();
            String duration = etDuration.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty() || videoUrl.isEmpty()) {
                Toast.makeText(TeacherCourseEditActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lesson == null) {
                // Create new lesson
                int newOrder = (lessons == null) ? 1 : (lessons.size() + 1);
                Lesson newLesson = new Lesson(
                        null,
                        courseId,
                        title,
                        description,
                        videoUrl,
                        duration,
                        newOrder
                );
                lessonApi.createLesson(newLesson);
                if (lessons == null) lessons = new ArrayList<>();
                lessons.add(newLesson);
            } else {
                // Update existing lesson (Lesson hiện mutable nên dùng setters)
                lesson.setTitle(title);
                lesson.setVideoUrl(videoUrl);
                lesson.setDuration(duration);
                lesson.setDescription(description);
                lessonApi.updateLesson(lesson.getId(), lesson);
            }

            lessonAdapter.submitList(lessons);
            updateLessonsVisibility();
            Toast.makeText(TeacherCourseEditActivity.this, "Lưu bài học thành công", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void confirmDeleteLesson(Lesson lesson, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài học")
                .setMessage("Bạn chắc chắn muốn xóa bài học này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    lessonApi.deleteLesson(lesson.getId());
                    if (position >= 0 && position < lessons.size()) {
                        lessons.remove(position);
                    } else {
                        // fallback: remove by id
                        for (int i = 0; i < lessons.size(); i++) {
                            if (lessons.get(i).getId().equals(lesson.getId())) {
                                lessons.remove(i);
                                break;
                            }
                        }
                    }
                    lessonAdapter.submitList(lessons);
                    updateLessonsVisibility();
                    Toast.makeText(TeacherCourseEditActivity.this, "Xóa bài học thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void saveCourse() {
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

            // Update course
            currentCourse.setTitle(title);
            currentCourse.setCategory(category);
            currentCourse.setPrice(price);
            currentCourse.setDescription(description);
            currentCourse.setSkills(skills);
            currentCourse.setRequirements(requirements);

            courseApi.updateCourse(courseId, currentCourse);
            Toast.makeText(this, "Lưu khóa học thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
