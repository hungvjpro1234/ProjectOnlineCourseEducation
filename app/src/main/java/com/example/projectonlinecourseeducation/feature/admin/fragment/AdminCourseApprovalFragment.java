package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminPendingCourseAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment phê duyệt khóa học cho Admin
 *
 * 3 LOẠI PHÊ DUYỆT:
 * 1. INITIAL - Khóa học mới (chưa được duyệt lần đầu)
 * 2. EDIT - Chỉnh sửa khóa học (pending version vs original)
 * 3. DELETE - Yêu cầu xóa khóa học
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
                // onViewChanges (for EDIT type)
                this::handleViewChanges
        );

        rvPendingCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingCourses.setAdapter(adapter);
    }

    private void registerCourseListener() {
        if (courseApi == null) return;

        courseUpdateListener = (courseId, updatedCourse) -> {
            // Reload when any course changes
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
                    // Khóa học mới: chưa được duyệt initial
                    if (!course.isInitialApproved()) {
                        result.add(course);
                    }
                    break;

                case EDIT:
                    // Chỉnh sửa: đã duyệt initial nhưng có pending edit
                    if (course.isInitialApproved() && !course.isEditApproved() && !course.isDeleteRequested()) {
                        result.add(course);
                    }
                    break;

                case DELETE:
                    // Xóa: đã được duyệt initial và có yêu cầu xóa
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
        // --- FIX: convert fragment's ApprovalType to adapter's ApprovalType to avoid enum type mismatch ---
        adapter.setType(AdminPendingCourseAdapter.ApprovalType.valueOf(type.name()));
        adapter.setCourses(courses);
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
        // Show dialog comparing original vs pending version
        showComparisonDialog(course);
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

                switch (currentType) {
                    case INITIAL:
                        success = courseApi.approveInitialCreation(course.getId());
                        message = success ? "✅ Đã duyệt khóa học mới" : "❌ Lỗi khi duyệt";
                        break;

                    case EDIT:
                        success = courseApi.approveCourseEdit(course.getId());
                        message = success ? "✅ Đã duyệt chỉnh sửa" : "❌ Lỗi khi duyệt";
                        break;

                    case DELETE:
                        success = courseApi.permanentlyDeleteCourse(course.getId());
                        message = success ? "✅ Đã xóa khóa học" : "❌ Lỗi khi xóa";
                        break;
                }

                final String finalMessage = message;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), finalMessage, Toast.LENGTH_SHORT).show();
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

                switch (currentType) {
                    case INITIAL:
                        success = courseApi.rejectInitialCreation(course.getId());
                        message = success ? "✅ Đã từ chối và xóa khóa học" : "❌ Lỗi khi từ chối";
                        break;

                    case EDIT:
                        success = courseApi.rejectCourseEdit(course.getId());
                        message = success ? "✅ Đã từ chối chỉnh sửa" : "❌ Lỗi khi từ chối";
                        break;

                    case DELETE:
                        success = courseApi.cancelDeleteRequest(course.getId());
                        message = success ? "✅ Đã từ chối xóa, khôi phục khóa học" : "❌ Lỗi khi từ chối";
                        break;
                }

                final String finalMessage = message;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), finalMessage, Toast.LENGTH_SHORT).show();
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

    private void showComparisonDialog(Course course) {
        if (courseApi == null) return;

        bgExecutor.execute(() -> {
            try {
                Course pendingVersion = courseApi.getPendingEdit(course.getId());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (pendingVersion != null) {
                            showCompareUI(course, pendingVersion);
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

    private void showCompareUI(Course original, Course pending) {
        // Create comparison dialog
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

        // Cleanup listener
        if (courseUpdateListener != null && courseApi != null) {
            try {
                courseApi.removeCourseUpdateListener(courseUpdateListener);
            } catch (Exception ignored) {}
        }

        // Shutdown executor
        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}
