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
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.example.projectonlinecourseeducation.feature.common.adapter.NotificationAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherCourseManagementActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherHomeActivity;
import com.example.projectonlinecourseeducation.feature.teacher.activity.TeacherLessonManagementActivity;

import java.util.List;

public class TeacherNotificationFragment extends Fragment
        implements NotificationApi.NotificationUpdateListener {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private TextView tvMarkAllRead;

    private NotificationAdapter adapter;
    private NotificationApi notificationApi;
    private AuthApi authApi;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_notification, container, false);

        notificationApi = ApiProvider.getNotificationApi();
        authApi = ApiProvider.getAuthApi();

        User currentUser = authApi.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return view;
        }
        currentUserId = currentUser.getId();

        rvNotifications = view.findViewById(R.id.rvNotifications);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);

        setupRecyclerView();

        // Load notifications (ASYNC)
        loadNotifications();

        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Register listener
        notificationApi.addNotificationUpdateListener(this);

        // Mark all as VIEWED (ASYNC)
        markAllAsViewed();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);
    }

    /**
     * 🔴 LOAD NOTIFICATIONS (ASYNC)
     */
    private void loadNotifications() {
        AsyncApiHelper.execute(
                () -> notificationApi.getNotificationsForUser(currentUserId),
                new AsyncApiHelper.ApiCallback<List<Notification>>() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        if (!isAdded()) return;

                        if (notifications.isEmpty()) {
                            rvNotifications.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvNotifications.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                            adapter.setNotifications(notifications);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Lỗi tải thông báo",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 🔴 MARK ALL AS VIEWED (ASYNC)
     */
    private void markAllAsViewed() {
        AsyncApiHelper.execute(
                () -> {
                    notificationApi.markAllAsViewed(currentUserId);
                    return null;
                },
                new AsyncApiHelper.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Không cần UI update
                    }

                    @Override
                    public void onError(Exception e) {
                        // Có thể log nếu cần
                    }
                }
        );
    }

    /**
     * 🔴 MARK ALL AS READ (ASYNC)
     */
    private void markAllAsRead() {
        AsyncApiHelper.execute(
                () -> notificationApi.markAllAsRead(currentUserId),
                new AsyncApiHelper.ApiCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        if (!isAdded()) return;

                        if (count > 0) {
                            Toast.makeText(requireContext(),
                                    "Đã đánh dấu " + count + " thông báo đã đọc",
                                    Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Không có thông báo nào để đánh dấu",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Lỗi khi đánh dấu thông báo",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 🔴 MARK SINGLE NOTIFICATION AS READ (ASYNC)
     */
    private void onNotificationClick(Notification notification) {
        AsyncApiHelper.execute(
                () -> {
                    notificationApi.markAsRead(notification.getId());
                    return null;
                },
                new AsyncApiHelper.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!isAdded()) return;

                        NotificationType type = notification.getType();

                        if (type == NotificationType.STUDENT_LESSON_COMMENT) {
                            navigateToLessonManagement(notification);
                        } else if (type == NotificationType.STUDENT_COURSE_COMMENT) {
                            navigateToCourseManagement(notification);
                        } else {
                            Toast.makeText(requireContext(),
                                    "Loại thông báo không hợp lệ",
                                    Toast.LENGTH_SHORT).show();
                        }

                        loadNotifications();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Lỗi xử lý thông báo",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
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

    @Override
    public void onNotificationsChanged(String userId) {
        if (userId.equals(currentUserId) && isAdded()) {
            loadNotifications();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationApi != null) {
            notificationApi.removeNotificationUpdateListener(this);
        }
    }
}
