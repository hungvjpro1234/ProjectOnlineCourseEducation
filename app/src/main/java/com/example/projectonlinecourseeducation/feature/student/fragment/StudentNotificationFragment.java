package com.example.projectonlinecourseeducation.feature.student.fragment;

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
import com.example.projectonlinecourseeducation.feature.student.activity.StudentLessonVideoActivity;

import java.util.List;

/**
 * Fragment hiển thị thông báo cho Student
 * Student nhận thông báo: TEACHER_REPLY_COMMENT
 * Click → navigate to StudentLessonVideoActivity
 */
public class StudentNotificationFragment extends Fragment implements NotificationApi.NotificationUpdateListener {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private TextView tvMarkAllRead;

    private NotificationAdapter adapter;
    private NotificationApi notificationApi;
    private AuthApi authApi;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View view = inflater.inflate(R.layout.fragment_student_notification, container, false);

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
        // Điều này sẽ trigger listener → StudentHomeActivity update badge → badge về 0
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
        if (notification.getType() == NotificationType.TEACHER_REPLY_COMMENT) {
            navigateToLessonVideo(notification);
        } else {
            Toast.makeText(requireContext(), "Loại thông báo không hợp lệ", Toast.LENGTH_SHORT).show();
        }

        // Refresh list
        loadNotifications();
    }

    private void navigateToLessonVideo(Notification notification) {
        String courseId = notification.getTargetCourseId();
        String lessonId = notification.getTargetLessonId();

        if (courseId == null || lessonId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), StudentLessonVideoActivity.class);
        intent.putExtra("courseId", courseId);
        intent.putExtra("lessonId", lessonId);
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
