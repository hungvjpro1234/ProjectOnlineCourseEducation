package com.example.projectonlinecourseeducation.feature.teacher.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.example.projectonlinecourseeducation.feature.common.adapter.NotificationAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherCourseManagementActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherHomeActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherLessonManagementActivity;

import java.util.List;

/**
 * Fragment hiển thị thông báo cho Teacher
 * Teacher nhận thông báo:
 * - STUDENT_LESSON_COMMENT → navigate to TeacherLessonManagementActivity
 * - STUDENT_COURSE_REVIEW → navigate to TeacherCourseManagementActivity
 * - COURSE_APPROVED / COURSE_REJECTED → navigate to TeacherHomeActivity (Management tab)
 */
public class TeacherNotificationFragment extends Fragment implements NotificationApi.NotificationUpdateListener {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private TextView tvMarkAllRead;

    private NotificationAdapter adapter;
    private NotificationApi notificationApi;
    private AuthApi authApi;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_notification, container, false);

        // Init APIs
        notificationApi = ApiProvider.getNotificationApi();
        authApi = ApiProvider.getAuthApi();

        // Get current user
        User currentUser = authApi.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return view;
        }
        currentUserId = currentUser.getId();

        // Init views
        rvNotifications = view.findViewById(R.id.rvNotifications);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);

        // Setup RecyclerView
        setupRecyclerView();

        // Load notifications
        loadNotifications();

        // Mark all as read button
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Register listener TRƯỚC KHI mark as viewed để badge được update
        notificationApi.addNotificationUpdateListener(this);

        // Mark all as VIEWED when fragment is opened
        // Điều này sẽ trigger listener → TeacherHomeActivity update badge → badge về 0
        notificationApi.markAllAsViewed(currentUserId);

        return view;
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationApi.getNotificationsForUser(currentUserId);

        if (notifications.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.setNotifications(notifications);
        }
    }

    private void onNotificationClick(Notification notification) {
        // Mark as READ
        notificationApi.markAsRead(notification.getId());

        // Navigate based on notification type
        NotificationType type = notification.getType();

        if (type == NotificationType.STUDENT_LESSON_COMMENT) {
            // Student đã comment vào bài học cụ thể
            navigateToLessonManagement(notification);
        } else if (type == NotificationType.STUDENT_COURSE_COMMENT) {
            // Student đã review/đánh giá khóa học
            navigateToCourseManagement(notification);
        } else {
            Toast.makeText(requireContext(), "Loại thông báo không hợp lệ", Toast.LENGTH_SHORT).show();
        }

        // Refresh list
        loadNotifications();
    }

    private void navigateToLessonManagement(Notification notification) {
        String courseId = notification.getTargetCourseId();
        String lessonId = notification.getTargetLessonId();

        if (courseId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), TeacherLessonManagementActivity.class);
        intent.putExtra(TeacherLessonManagementActivity.EXTRA_COURSE_ID, courseId);
        intent.putExtra(TeacherLessonManagementActivity.EXTRA_LESSON_ID, lessonId);
        startActivity(intent);

    }

    private void navigateToCourseManagement(Notification notification) {
        String courseId = notification.getTargetCourseId();

        if (courseId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), TeacherCourseManagementActivity.class);
        intent.putExtra("course_id", courseId);
        startActivity(intent);

    }

    private void navigateToTeacherHome() {
        // Navigate to TeacherHomeActivity and switch to Management tab (index 2)
        Intent intent = new Intent(requireContext(), TeacherHomeActivity.class);
        intent.putExtra("selectedTab", 2); // Management tab index
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void markAllAsRead() {
        int count = notificationApi.markAllAsRead(currentUserId);
        if (count > 0) {
            Toast.makeText(requireContext(), "Đã đánh dấu " + count + " thông báo đã đọc", Toast.LENGTH_SHORT).show();
            loadNotifications();
        } else {
            Toast.makeText(requireContext(), "Không có thông báo nào để đánh dấu", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNotificationsChanged(String userId) {
        // Reload notifications when there's a change
        if (userId.equals(currentUserId) && isAdded()) {
            requireActivity().runOnUiThread(this::loadNotifications);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister listener
        if (notificationApi != null) {
            notificationApi.removeNotificationUpdateListener(this);
        }
    }
}
