package com.example.projectonlinecourseeducation.data.notification;

import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationStatus;
import com.example.projectonlinecourseeducation.core.utils.datahelper.TeacherIdHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService implements NotificationApi {

    private static NotificationService instance;

    // In-memory storage: userId -> List<Notification>
    private final Map<String, List<Notification>> notificationsByUser;

    // Listeners for notification updates
    private final List<NotificationUpdateListener> listeners;


    private static final boolean ENABLE_DEMO_NOTIFICATIONS = false;

    private NotificationService() {
        this.notificationsByUser = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();

        if (ENABLE_DEMO_NOTIFICATIONS) {
            initSampleData();
        }
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    private void initSampleData() {
    }

    // ================ QUERY NOTIFICATIONS ================

    @Override
    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null) {
            return new ArrayList<>();
        }
        // Sort by createdAt descending (newest first)
        List<Notification> sortedList = new ArrayList<>(notifications);
        Collections.sort(sortedList, new Comparator<Notification>() {
            @Override
            public int compare(Notification n1, Notification n2) {
                return Long.compare(n2.getCreatedAt(), n1.getCreatedAt());
            }
        });
        return sortedList;
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        List<Notification> allNotifications = getNotificationsForUser(userId);
        List<Notification> unreadList = new ArrayList<>();
        for (Notification notification : allNotifications) {
            if (notification.getStatus() == NotificationStatus.UNREAD) {
                unreadList.add(notification);
            }
        }
        return unreadList;
    }

    @Override
    public int getUnreadCount(String userId) {
        return getUnreadNotifications(userId).size();
    }

    @Override
    public List<Notification> getNotificationsByType(String userId, NotificationType type) {
        List<Notification> allNotifications = getNotificationsForUser(userId);
        List<Notification> filteredList = new ArrayList<>();
        for (Notification notification : allNotifications) {
            if (notification.getType() == type) {
                filteredList.add(notification);
            }
        }
        return filteredList;
    }

    @Override
    public Notification getNotificationById(String notificationId) {
        for (List<Notification> notifications : notificationsByUser.values()) {
            for (Notification notification : notifications) {
                if (notification.getId().equals(notificationId)) {
                    return notification;
                }
            }
        }
        return null;
    }

    // ================ UPDATE NOTIFICATION STATUS ================

    @Override
    public Notification markAsViewed(String notificationId) {
        Notification notification = getNotificationById(notificationId);
        if (notification != null && notification.getStatus() == NotificationStatus.UNREAD) {
            notification.markAsViewed();
            notifyListeners(notification.getUserId());
            return notification;
        }
        return notification;
    }

    @Override
    public Notification markAsRead(String notificationId) {
        Notification notification = getNotificationById(notificationId);
        if (notification != null) {
            notification.markAsRead();
            notifyListeners(notification.getUserId());
            return notification;
        }
        return null;
    }

    @Override
    public int markAllAsViewed(String userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null) {
            return 0;
        }
        int count = 0;
        for (Notification notification : notifications) {
            if (notification.getStatus() == NotificationStatus.UNREAD) {
                notification.markAsViewed();
                count++;
            }
        }
        if (count > 0) {
            notifyListeners(userId);
        }
        return count;
    }

    @Override
    public int markAllAsRead(String userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null) {
            return 0;
        }
        int count = 0;
        for (Notification notification : notifications) {
            if (notification.getStatus() != NotificationStatus.READ) {
                notification.markAsRead();
                count++;
            }
        }
        if (count > 0) {
            notifyListeners(userId);
        }
        return count;
    }

    // ================ CREATE NOTIFICATIONS ================

    @Override
    public Notification createTeacherReplyNotification(String studentId, String teacherName,
                                                       String lessonId, String lessonTitle,
                                                       String courseId, String courseTitle,
                                                       String commentId) {
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(studentId)
                .type(NotificationType.TEACHER_REPLY_COMMENT)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Giáo viên đã trả lời bình luận của bạn")
                .message(teacherName + " đã trả lời bình luận của bạn trong bài \"" + lessonTitle + "\"")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(lessonId)
                .targetCommentId(commentId)
                .targetReviewId(null)
                .senderName(teacherName)
                .courseTitle(courseTitle)
                .lessonTitle(lessonTitle)
                .build();

        addNotificationToUser(studentId, notification);
        return notification;
    }

    @Override
    public Notification createStudentCourseReviewNotification(String teacherId, String studentName,
                                                              String courseId, String courseTitle,
                                                              String reviewId, float rating) {
        String ratingText = String.format("%.1f sao", rating);
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(teacherId)
                .type(NotificationType.STUDENT_COURSE_COMMENT)  // Changed from STUDENT_COURSE_REVIEW
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Học viên mới đánh giá khóa học")
                .message(studentName + " đã đánh giá khóa học \"" + courseTitle + "\" - " + ratingText)
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(reviewId)
                .senderName(studentName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
                .build();

        addNotificationToUser(teacherId, notification);
        return notification;
    }

    @Override
    public Notification createStudentLessonCommentNotification(String teacherId, String studentName,
                                                               String lessonId, String lessonTitle,
                                                               String courseId, String courseTitle,
                                                               String commentId) {
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(teacherId)
                .type(NotificationType.STUDENT_LESSON_COMMENT)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Học viên mới bình luận trong bài học")
                .message(studentName + " đã bình luận trong bài \"" + lessonTitle + "\"")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(lessonId)
                .targetCommentId(commentId)
                .targetReviewId(null)
                .senderName(studentName)
                .courseTitle(courseTitle)
                .lessonTitle(lessonTitle)
                .build();

        addNotificationToUser(teacherId, notification);
        return notification;
    }

    // ================ DELETE NOTIFICATIONS ================

    @Override
    public boolean deleteNotification(String notificationId) {
        for (Map.Entry<String, List<Notification>> entry : notificationsByUser.entrySet()) {
            List<Notification> notifications = entry.getValue();
            for (int i = 0; i < notifications.size(); i++) {
                if (notifications.get(i).getId().equals(notificationId)) {
                    notifications.remove(i);
                    notifyListeners(entry.getKey());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int deleteReadNotifications(String userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null) {
            return 0;
        }
        int count = 0;
        for (int i = notifications.size() - 1; i >= 0; i--) {
            if (notifications.get(i).getStatus() == NotificationStatus.READ) {
                notifications.remove(i);
                count++;
            }
        }
        if (count > 0) {
            notifyListeners(userId);
        }
        return count;
    }

    @Override
    public int deleteAllNotifications(String userId) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null || notifications.isEmpty()) {
            return 0;
        }
        int count = notifications.size();
        notifications.clear();
        notifyListeners(userId);
        return count;
    }

    // ================ LISTENER / NOTIFY ================

    @Override
    public void addNotificationUpdateListener(NotificationUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeNotificationUpdateListener(NotificationUpdateListener listener) {
        listeners.remove(listener);
    }

    // ================ HELPER METHODS ================

    private void addNotificationToUser(String userId, Notification notification) {
        List<Notification> notifications = notificationsByUser.get(userId);
        if (notifications == null) {
            notifications = new ArrayList<>();
            notificationsByUser.put(userId, notifications);
        }
        notifications.add(notification);
        notifyListeners(userId);
    }

    private void notifyListeners(String userId) {
        for (NotificationUpdateListener listener : listeners) {
            listener.onNotificationsChanged(userId);
        }
    }

    /**
     * Helper method: Map tên teacher → userId
     */
    public String getTeacherIdByName(String teacherName) {
        return TeacherIdHelper.TEACHER_NAME_TO_ID.getOrDefault(
                teacherName == null ? "" : teacherName.trim(),
                "u2"
        );
    }
}
