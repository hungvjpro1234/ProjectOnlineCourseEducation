package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminCoursePreviewActivity;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminLessonVideoPreviewActivity;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminPendingCourseAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment phê duyệt khóa học cho Admin
 *
 * 3 LOẠI PHÊ DUYỆT:
 * 1. INITIAL - Khóa học mới (có preview activity đầy đủ)
 * 2. EDIT - Chỉnh sửa (dialog chi tiết với lesson tracking)
 * 3. DELETE - Yêu cầu xóa (dialog đơn giản)
 */
public class AdminCourseApprovalFragment extends Fragment {

    private static final String TAG = "AdminApproval";

    // UI Components
    private TabLayout tabLayout;
    private RecyclerView rvPendingCourses;
    private View emptyState;
    private TextView tvEmptyMessage;

    // Data
    private CourseApi courseApi;
    private LessonApi lessonApi;
    private AdminPendingCourseAdapter adapter;
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    // Current filter
    private ApprovalType currentType = ApprovalType.INITIAL;

    // Listener
    private CourseApi.CourseUpdateListener courseUpdateListener;

    enum ApprovalType {
        INITIAL,  // Khóa học mới
        EDIT,     // Chỉnh sửa
        DELETE    // Xóa
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_course_approval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initApis();
        setupTabs();
        setupRecyclerView();
        registerCourseListener();

        // Load initial data
        loadPendingCourses(currentType);
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        rvPendingCourses = view.findViewById(R.id.rvPendingCourses);
        emptyState = view.findViewById(R.id.emptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
    }

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Khóa học mới").setIcon(R.drawable.ic_new_course));
        tabLayout.addTab(tabLayout.newTab().setText("Chỉnh sửa").setIcon(R.drawable.ic_edit_pending));
        tabLayout.addTab(tabLayout.newTab().setText("Yêu cầu xóa").setIcon(R.drawable.ic_delete_request));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentType = ApprovalType.INITIAL;
                        break;
                    case 1:
                        currentType = ApprovalType.EDIT;
                        break;
                    case 2:
                        currentType = ApprovalType.DELETE;
                        break;
                }
                loadPendingCourses(currentType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminPendingCourseAdapter(
                // onApprove
                this::handleApproveCourse,
                // onReject
                this::handleRejectCourse,
                // onViewChanges
                this::handleViewChanges,
                // onPreview (NEW for INITIAL)
                this::handlePreviewCourse
        );

        rvPendingCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingCourses.setAdapter(adapter);
    }

    private void registerCourseListener() {
        if (courseApi == null) return;

        courseUpdateListener = (courseId, updatedCourse) -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> loadPendingCourses(currentType));
            }
        };

        try {
            courseApi.addCourseUpdateListener(courseUpdateListener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register listener", e);
        }
    }

    private void loadPendingCourses(ApprovalType type) {
        if (courseApi == null) {
            showEmpty("Lỗi: CourseApi không khả dụng");
            return;
        }

        bgExecutor.execute(() -> {
            try {
                List<Course> allPending = courseApi.getPendingCourses();
                List<Course> filtered = filterByType(allPending, type);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (filtered.isEmpty()) {
                            showEmpty(getEmptyMessage(type));
                        } else {
                            showCourses(filtered, type);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading pending courses", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            showEmpty("Lỗi: " + e.getMessage()));
                }
            }
        });
    }

    private List<Course> filterByType(List<Course> allPending, ApprovalType type) {
        List<Course> result = new ArrayList<>();
        if (allPending == null) return result;

        for (Course course : allPending) {
            switch (type) {
                case INITIAL:
                    if (!course.isInitialApproved()) {
                        result.add(course);
                    }
                    break;

                case EDIT:
                    if (course.isInitialApproved() && !course.isEditApproved() && !course.isDeleteRequested()) {
                        result.add(course);
                    }
                    break;

                case DELETE:
                    if (course.isInitialApproved() && course.isDeleteRequested()) {
                        result.add(course);
                    }
                    break;
            }
        }

        return result;
    }

    private String getEmptyMessage(ApprovalType type) {
        switch (type) {
            case INITIAL:
                return "Không có khóa học mới nào đang chờ duyệt";
            case EDIT:
                return "Không có chỉnh sửa nào đang chờ duyệt";
            case DELETE:
                return "Không có yêu cầu xóa nào đang chờ duyệt";
            default:
                return "Không có dữ liệu";
        }
    }

    private void showEmpty(String message) {
        rvPendingCourses.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    private void showCourses(List<Course> courses, ApprovalType type) {
        emptyState.setVisibility(View.GONE);
        rvPendingCourses.setVisibility(View.VISIBLE);
        adapter.setType(AdminPendingCourseAdapter.ApprovalType.valueOf(type.name()));
        adapter.setCourses(courses);
    }

    // ==================== PREVIEW (NEW) ====================

    private void handlePreviewCourse(Course course) {
        // Open full preview activity
        Intent intent = new Intent(getActivity(), AdminCoursePreviewActivity.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
    }

    // ==================== APPROVAL ACTIONS ====================

    private void handleApproveCourse(Course course) {
        if (courseApi == null || course == null) return;

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận phê duyệt")
                .setMessage(getApproveMessage(course, currentType))
                .setPositiveButton("Phê duyệt", (dialog, which) -> performApprove(course))
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_check_circle)
                .show();
    }

    private void handleRejectCourse(Course course) {
        if (courseApi == null || course == null) return;

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận từ chối")
                .setMessage(getRejectMessage(course, currentType))
                .setPositiveButton("Từ chối", (dialog, which) -> performReject(course))
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_cancel)
                .show();
    }

    private void handleViewChanges(Course course) {
        if (currentType == ApprovalType.EDIT) {
            showEditComparisonDialog(course);
        } else {
            // Fallback to simple comparison
            showComparisonDialog(course);
        }
    }

    private String getApproveMessage(Course course, ApprovalType type) {
        switch (type) {
            case INITIAL:
                return "Phê duyệt khóa học mới:\n\"" + course.getTitle() + "\"\n\n" +
                        "Khóa học sẽ hiển thị với students sau khi duyệt.";
            case EDIT:
                return "Phê duyệt chỉnh sửa cho:\n\"" + course.getTitle() + "\"\n\n" +
                        "Thay đổi sẽ được áp dụng lên khóa học.";
            case DELETE:
                return "Phê duyệt XÓA khóa học:\n\"" + course.getTitle() + "\"\n\n" +
                        "⚠️ Khóa học sẽ bị xóa vĩnh viễn!";
            default:
                return "Xác nhận phê duyệt?";
        }
    }

    private String getRejectMessage(Course course, ApprovalType type) {
        switch (type) {
            case INITIAL:
                return "Từ chối khóa học mới:\n\"" + course.getTitle() + "\"\n\n" +
                        "⚠️ Khóa học sẽ bị xóa khỏi hệ thống!";
            case EDIT:
                return "Từ chối chỉnh sửa cho:\n\"" + course.getTitle() + "\"\n\n" +
                        "Thay đổi sẽ bị hủy, giữ nguyên phiên bản gốc.";
            case DELETE:
                return "Từ chối yêu cầu xóa:\n\"" + course.getTitle() + "\"\n\n" +
                        "Khóa học sẽ được khôi phục trạng thái bình thường.";
            default:
                return "Xác nhận từ chối?";
        }
    }

    private void performApprove(Course course) {
        bgExecutor.execute(() -> {
            try {
                boolean success = false;
                String message = "";
                int totalApproved = 0;

                switch (currentType) {
                    case INITIAL:
                        // 1. Approve course
                        success = courseApi.approveInitialCreation(course.getId());
                        if (success) totalApproved++;

                        // 2. Approve ALL lessons of this course
                        List<Lesson> pendingLessons = lessonApi.getPendingLessonsForCourse(course.getId());
                        for (Lesson lesson : pendingLessons) {
                            if (!lesson.isInitialApproved()) {
                                if (lessonApi.approveInitialCreation(lesson.getId())) {
                                    totalApproved++;
                                }
                            }
                        }

                        // 3. TODO: Approve ALL quizzes of this course (when quiz approval is implemented)

                        message = success ? "✅ Đã duyệt khóa học mới + " + (totalApproved - 1) + " lessons" : "❌ Lỗi khi duyệt";
                        break;

                    case EDIT:
                        // 1. Approve course edit
                        success = courseApi.approveCourseEdit(course.getId());
                        if (success) totalApproved++;

                        // 2. Approve ALL lesson edits of this course
                        List<Lesson> editedLessons = lessonApi.getPendingLessonsForCourse(course.getId());
                        for (Lesson lesson : editedLessons) {
                            if (!lesson.isEditApproved() && !lesson.isDeleteRequested()) {
                                if (lessonApi.approveLessonEdit(lesson.getId())) {
                                    totalApproved++;
                                }
                            }
                        }

                        // 3. TODO: Approve ALL quiz edits of this course

                        message = success ? "✅ Đã duyệt chỉnh sửa + " + (totalApproved - 1) + " lessons" : "❌ Lỗi khi duyệt";
                        break;

                    case DELETE:
                        // 1. Permanently delete course (lessons will be orphaned/handled by cascade)
                        success = courseApi.permanentlyDeleteCourse(course.getId());

                        // 2. Delete ALL lessons of this course
                        if (success) {
                            List<Lesson> lessonsToDelete = lessonApi.getLessonsForCourse(course.getId());
                            for (Lesson lesson : lessonsToDelete) {
                                lessonApi.permanentlyDeleteLesson(lesson.getId());
                                totalApproved++;
                            }
                        }

                        // 3. TODO: Delete ALL quizzes of this course

                        message = success ? "✅ Đã xóa khóa học + " + totalApproved + " lessons" : "❌ Lỗi khi xóa";
                        break;
                }

                final String finalMessage = message;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), finalMessage, Toast.LENGTH_LONG).show();
                        loadPendingCourses(currentType);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error approving course", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void performReject(Course course) {
        bgExecutor.execute(() -> {
            try {
                boolean success = false;
                String message = "";
                int totalRejected = 0;

                switch (currentType) {
                    case INITIAL:
                        // 1. Reject course (will delete it)
                        success = courseApi.rejectInitialCreation(course.getId());
                        if (success) totalRejected++;

                        // 2. Reject ALL lessons of this course (will delete them)
                        List<Lesson> pendingLessons = lessonApi.getPendingLessonsForCourse(course.getId());
                        for (Lesson lesson : pendingLessons) {
                            if (!lesson.isInitialApproved()) {
                                if (lessonApi.rejectInitialCreation(lesson.getId())) {
                                    totalRejected++;
                                }
                            }
                        }

                        // 3. TODO: Reject ALL quizzes of this course

                        message = success ? "✅ Đã từ chối và xóa khóa học + " + (totalRejected - 1) + " lessons" : "❌ Lỗi khi từ chối";
                        break;

                    case EDIT:
                        // 1. Reject course edit (discard changes)
                        success = courseApi.rejectCourseEdit(course.getId());
                        if (success) totalRejected++;

                        // 2. Reject ALL lesson edits of this course
                        List<Lesson> editedLessons = lessonApi.getPendingLessonsForCourse(course.getId());
                        for (Lesson lesson : editedLessons) {
                            if (!lesson.isEditApproved() && !lesson.isDeleteRequested()) {
                                if (lessonApi.rejectLessonEdit(lesson.getId())) {
                                    totalRejected++;
                                }
                            }
                        }

                        // 3. TODO: Reject ALL quiz edits of this course

                        message = success ? "✅ Đã từ chối chỉnh sửa + " + (totalRejected - 1) + " lessons" : "❌ Lỗi khi từ chối";
                        break;

                    case DELETE:
                        // 1. Cancel delete request for course (restore it)
                        success = courseApi.cancelDeleteRequest(course.getId());
                        if (success) totalRejected++;

                        // 2. Cancel delete request for ALL lessons of this course
                        List<Lesson> lessonsToRestore = lessonApi.getPendingLessonsForCourse(course.getId());
                        for (Lesson lesson : lessonsToRestore) {
                            if (lesson.isDeleteRequested()) {
                                if (lessonApi.cancelDeleteRequest(lesson.getId())) {
                                    totalRejected++;
                                }
                            }
                        }

                        // 3. TODO: Cancel delete for ALL quizzes

                        message = success ? "✅ Đã khôi phục khóa học + " + (totalRejected - 1) + " lessons" : "❌ Lỗi khi từ chối";
                        break;
                }

                final String finalMessage = message;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), finalMessage, Toast.LENGTH_LONG).show();
                        loadPendingCourses(currentType);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rejecting course", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // ==================== EDIT COMPARISON WITH LESSON TRACKING ====================

    private void showEditComparisonDialog(Course course) {
        if (courseApi == null || lessonApi == null) return;

        bgExecutor.execute(() -> {
            try {
                Course pendingVersion = courseApi.getPendingEdit(course.getId());

                // Load lessons for both versions
                List<Lesson> originalLessons = lessonApi.getLessonsForCourse(course.getId());

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (pendingVersion != null) {
                        showEnhancedComparisonDialog(course, pendingVersion, originalLessons);
                    } else {
                        Toast.makeText(getContext(),
                                "Không tìm thấy phiên bản chỉnh sửa",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading edit comparison", e);
            }
        });
    }

    private void showEnhancedComparisonDialog(Course original, Course pending, List<Lesson> originalLessons) {
        // Create custom dialog with scrollable content
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_comparison, null);
        LinearLayout comparisonContainer = dialogView.findViewById(R.id.comparisonContainer);

        // Build comparison content
        addCourseComparison(comparisonContainer, original, pending);
        addLessonComparison(comparisonContainer, originalLessons, original.getId());

        builder.setView(dialogView);
        builder.setTitle("📝 So sánh thay đổi");
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    private void addCourseComparison(LinearLayout container, Course original, Course pending) {
        // Header
        addSectionHeader(container, "THÔNG TIN KHÓA HỌC");

        // Compare fields
        if (!original.getTitle().equals(pending.getTitle())) {
            addComparisonRow(container, "Tiêu đề", original.getTitle(), pending.getTitle());
        }

        if (!original.getCategory().equals(pending.getCategory())) {
            addComparisonRow(container, "Danh mục", original.getCategory(), pending.getCategory());
        }

        if (original.getPrice() != pending.getPrice()) {
            addComparisonRow(container, "Giá",
                    String.format("%,.0f VNĐ", original.getPrice()),
                    String.format("%,.0f VNĐ", pending.getPrice()));
        }

        if (!original.getDescription().equals(pending.getDescription())) {
            addComparisonRow(container, "Mô tả",
                    truncate(original.getDescription(), 50),
                    truncate(pending.getDescription(), 50));
        }

        if (!original.getImageUrl().equals(pending.getImageUrl())) {
            addComparisonRow(container, "Ảnh", "Đã thay đổi", "URL mới");
        }

        // Skills comparison
        if (!listsEqual(original.getSkills(), pending.getSkills())) {
            addComparisonRow(container, "Kỹ năng",
                    original.getSkills().size() + " kỹ năng",
                    pending.getSkills().size() + " kỹ năng");
        }

        // Requirements comparison
        if (!listsEqual(original.getRequirements(), pending.getRequirements())) {
            addComparisonRow(container, "Yêu cầu",
                    original.getRequirements().size() + " yêu cầu",
                    pending.getRequirements().size() + " yêu cầu");
        }
    }

    private void addLessonComparison(LinearLayout container, List<Lesson> originalLessons, String courseId) {
        // Load current lessons (after edit)
        bgExecutor.execute(() -> {
            List<Lesson> currentLessons = lessonApi.getLessonsForCourse(courseId);

            // Compare lessons
            LessonChanges changes = compareLessons(originalLessons, currentLessons);

            if (getActivity() != null && !changes.isEmpty()) {
                getActivity().runOnUiThread(() -> {
                    addSectionHeader(container, "BÀI HỌC");
                    displayLessonChanges(container, changes);
                });
            }
        });
    }

    private LessonChanges compareLessons(List<Lesson> original, List<Lesson> current) {
        LessonChanges changes = new LessonChanges();

        Map<String, Lesson> originalMap = new HashMap<>();
        for (Lesson l : original) {
            if (l.getId() != null) {
                originalMap.put(l.getId(), l);
            }
        }

        Set<String> currentIds = new HashSet<>();
        for (Lesson l : current) {
            if (l.getId() != null) {
                currentIds.add(l.getId());

                if (originalMap.containsKey(l.getId())) {
                    // Check if modified
                    Lesson orig = originalMap.get(l.getId());
                    if (!lessonsEqual(orig, l)) {
                        changes.modified.add(l);
                    }
                } else {
                    // New lesson
                    changes.added.add(l);
                }
            } else {
                // New lesson without ID yet
                changes.added.add(l);
            }
        }

        // Find deleted
        for (String origId : originalMap.keySet()) {
            if (!currentIds.contains(origId)) {
                changes.deleted.add(originalMap.get(origId));
            }
        }

        return changes;
    }

    private void displayLessonChanges(LinearLayout container, LessonChanges changes) {
        // Added lessons
        for (Lesson lesson : changes.added) {
            addLessonChangeRow(container, "➕ Thêm", lesson, true);
        }

        // Modified lessons
        for (Lesson lesson : changes.modified) {
            addLessonChangeRow(container, "✏️ Sửa", lesson, true);
        }

        // Deleted lessons
        for (Lesson lesson : changes.deleted) {
            addLessonChangeRow(container, "🗑️ Xóa", lesson, false);
        }
    }

    private void addLessonChangeRow(LinearLayout container, String changeType, Lesson lesson, boolean canPlay) {
        View row = getLayoutInflater().inflate(R.layout.item_admic_dialog_lesson_change, container, false);

        TextView tvChangeType = row.findViewById(R.id.tvChangeType);
        TextView tvLessonTitle = row.findViewById(R.id.tvLessonTitle);
        TextView tvLessonInfo = row.findViewById(R.id.tvLessonInfo);
        View btnPlay = row.findViewById(R.id.btnPlay);

        tvChangeType.setText(changeType);
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonInfo.setText("Video: " + lesson.getVideoUrl() + " • " + lesson.getDuration());

        if (canPlay) {
            btnPlay.setVisibility(View.VISIBLE);
            btnPlay.setOnClickListener(v -> {
                // Open video preview
                Intent intent = new Intent(getActivity(), AdminLessonVideoPreviewActivity.class);
                intent.putExtra("lesson_id", lesson.getId());
                intent.putExtra("course_title", "Preview");
                startActivity(intent);
            });
        } else {
            btnPlay.setVisibility(View.GONE);
        }

        container.addView(row);
    }

    // ==================== HELPERS ====================

    private void addSectionHeader(LinearLayout container, String title) {
        TextView tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextSize(14);
        tv.setTextColor(0xFF2196F3);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, 24, 0, 8);
        container.addView(tv);
    }

    private void addComparisonRow(LinearLayout container, String field, String oldValue, String newValue) {
        View row = getLayoutInflater().inflate(R.layout.item_admic_dialog_field_comparison, container, false);

        TextView tvField = row.findViewById(R.id.tvField);
        TextView tvOld = row.findViewById(R.id.tvOldValue);
        TextView tvNew = row.findViewById(R.id.tvNewValue);

        tvField.setText(field + ":");
        tvOld.setText("Cũ: " + oldValue);
        tvNew.setText("Mới: " + newValue);

        container.addView(row);
    }

    private String truncate(String s, int maxLength) {
        if (s == null) return "";
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength) + "...";
    }

    private boolean listsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private boolean lessonsEqual(Lesson l1, Lesson l2) {
        if (!l1.getTitle().equals(l2.getTitle())) return false;
        if (!l1.getVideoUrl().equals(l2.getVideoUrl())) return false;
        if (!l1.getDescription().equals(l2.getDescription())) return false;
        return true;
    }

    // Simple comparison dialog (fallback)
    private void showComparisonDialog(Course course) {
        if (courseApi == null) return;

        bgExecutor.execute(() -> {
            try {
                Course pendingVersion = courseApi.getPendingEdit(course.getId());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (pendingVersion != null) {
                            showSimpleCompareUI(course, pendingVersion);
                        } else {
                            Toast.makeText(getContext(),
                                    "Không tìm thấy phiên bản chỉnh sửa",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading pending version", e);
            }
        });
    }

    private void showSimpleCompareUI(Course original, Course pending) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("So sánh thay đổi");

        StringBuilder comparison = new StringBuilder();
        comparison.append("📝 Thay đổi cho: ").append(original.getTitle()).append("\n\n");

        if (!original.getTitle().equals(pending.getTitle())) {
            comparison.append("• Tiêu đề:\n");
            comparison.append("  Cũ: ").append(original.getTitle()).append("\n");
            comparison.append("  Mới: ").append(pending.getTitle()).append("\n\n");
        }

        if (!original.getCategory().equals(pending.getCategory())) {
            comparison.append("• Danh mục:\n");
            comparison.append("  Cũ: ").append(original.getCategory()).append("\n");
            comparison.append("  Mới: ").append(pending.getCategory()).append("\n\n");
        }

        if (original.getPrice() != pending.getPrice()) {
            comparison.append("• Giá:\n");
            comparison.append("  Cũ: ").append(String.format("%,.0f VNĐ", original.getPrice())).append("\n");
            comparison.append("  Mới: ").append(String.format("%,.0f VNĐ", pending.getPrice())).append("\n\n");
        }

        if (!original.getDescription().equals(pending.getDescription())) {
            comparison.append("• Mô tả: Đã thay đổi\n\n");
        }

        if (comparison.toString().endsWith("\n\n")) {
            comparison.setLength(comparison.length() - 1);
        } else {
            comparison.append("Không có thay đổi đáng kể.");
        }

        builder.setMessage(comparison.toString());
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (courseUpdateListener != null && courseApi != null) {
            try {
                courseApi.removeCourseUpdateListener(courseUpdateListener);
            } catch (Exception ignored) {}
        }

        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }

    // ==================== DATA CLASSES ====================

    static class LessonChanges {
        List<Lesson> added = new ArrayList<>();
        List<Lesson> modified = new ArrayList<>();
        List<Lesson> deleted = new ArrayList<>();

        boolean isEmpty() {
            return added.isEmpty() && modified.isEmpty() && deleted.isEmpty();
        }
    }
}