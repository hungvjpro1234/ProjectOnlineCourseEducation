package com.example.projectonlinecourseeducation.data.notification;

import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fake implementation của NotificationApi với sample data trong RAM
 * Hỗ trợ 3 role: Student, Teacher, Admin với các loại thông báo khác nhau
 */
public class NotificationFakeApiService implements NotificationApi {

    private static NotificationFakeApiService instance;

    // In-memory storage: userId -> List<Notification>
    private final Map<String, List<Notification>> notificationsByUser;

    // Listeners for notification updates
    private final List<NotificationUpdateListener> listeners;

    private NotificationFakeApiService() {
        this.notificationsByUser = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        initSampleData();
    }

    public static synchronized NotificationFakeApiService getInstance() {
        if (instance == null) {
            instance = new NotificationFakeApiService();
        }
        return instance;
    }

    // ================ SAMPLE DATA ================

    private void initSampleData() {
        long now = System.currentTimeMillis();
        long oneHourAgo = now - 3600000;
        long twoDaysAgo = now - 2 * 24 * 3600000L;
        long threeDaysAgo = now - 3 * 24 * 3600000L;
        long oneWeekAgo = now - 7 * 24 * 3600000L;

        // ========== STUDENT NOTIFICATIONS (student1) ==========
        List<Notification> studentNotifications = new ArrayList<>();

        // Thông báo 1: Teacher reply comment (UNREAD)
        studentNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("student1")
                .type(NotificationType.TEACHER_REPLY_COMMENT)
                .status(NotificationStatus.UNREAD)
                .createdAt(oneHourAgo)
                .title("Giáo viên đã trả lời bình luận của bạn")
                .message("Nguyễn Văn A đã trả lời bình luận của bạn trong bài \"Introduction to Java\"")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId("L1")
                .targetCommentId("C123")
                .targetReviewId(null)
                .senderName("Nguyễn Văn A")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle("Introduction to Java")
                .build());

        // Thông báo 2: Teacher reply comment (VIEWED)
        studentNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("student1")
                .type(NotificationType.TEACHER_REPLY_COMMENT)
                .status(NotificationStatus.VIEWED)
                .createdAt(twoDaysAgo)
                .title("Giáo viên đã trả lời bình luận của bạn")
                .message("Trần Thị B đã trả lời bình luận của bạn trong bài \"Variables and Data Types\"")
                .avatarUrl(null)
                .targetCourseId("2")
                .targetLessonId("L5")
                .targetCommentId("C456")
                .targetReviewId(null)
                .senderName("Trần Thị B")
                .courseTitle("Python cho người mới bắt đầu")
                .lessonTitle("Variables and Data Types")
                .build());

        // Thông báo 3: Teacher reply comment (READ)
        studentNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("student1")
                .type(NotificationType.TEACHER_REPLY_COMMENT)
                .status(NotificationStatus.READ)
                .createdAt(oneWeekAgo)
                .title("Giáo viên đã trả lời bình luận của bạn")
                .message("Lê Văn C đã trả lời bình luận của bạn trong bài \"Functions and Methods\"")
                .avatarUrl(null)
                .targetCourseId("3")
                .targetLessonId("L8")
                .targetCommentId("C789")
                .targetReviewId(null)
                .senderName("Lê Văn C")
                .courseTitle("JavaScript ES6 toàn tập")
                .lessonTitle("Functions and Methods")
                .build());

        // Map cho cả userId và username để đảm bảo student nào cũng thấy thông báo fake
        notificationsByUser.put("student1", studentNotifications); // username
        notificationsByUser.put("u1", studentNotifications);      // id của Student One (theo AuthFakeApiService)

        // ========== TEACHER NOTIFICATIONS (teacher) ==========
        List<Notification> teacherNotifications = new ArrayList<>();

        // Thông báo 1: Student comment (UNREAD)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.STUDENT_LESSON_COMMENT)
                .status(NotificationStatus.UNREAD)
                .createdAt(oneHourAgo)
                .title("Học viên mới bình luận trong bài học")
                .message("Nguyễn Học Sinh đã bình luận trong bài \"Introduction to Java\"")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId("L1")
                .targetCommentId("C999")
                .targetReviewId(null)
                .senderName("Nguyễn Học Sinh")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle("Introduction to Java")
                .build());

        // Thông báo 2: Student review (UNREAD)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.STUDENT_COURSE_COMMENT)  // Updated type
                .status(NotificationStatus.UNREAD)
                .createdAt(twoDaysAgo)
                .title("Học viên mới đánh giá khóa học")
                .message("Trần Học Sinh đã đánh giá khóa học \"Lập trình Java từ cơ bản đến nâng cao\" - 5.0 sao")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId("R123")
                .senderName("Trần Học Sinh")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle(null)
                .build());

        // Thông báo 3: Student comment in another lesson (VIEWED)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.STUDENT_LESSON_COMMENT)
                .status(NotificationStatus.VIEWED)
                .createdAt(threeDaysAgo)
                .title("Học viên mới bình luận trong bài học")
                .message("Lê Văn C đã bình luận trong bài \"Variables and Data Types\"")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId("c1_l2")
                .targetCommentId("C888")
                .targetReviewId(null)
                .senderName("Lê Văn C")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle("Variables and Data Types")
                .build());

        // Thông báo 4: Student course review (READ)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.STUDENT_COURSE_COMMENT)
                .status(NotificationStatus.READ)
                .createdAt(oneWeekAgo)
                .title("Học viên mới đánh giá khóa học")
                .message("Nguyễn Thị D đã đánh giá khóa học \"Python cho người mới bắt đầu\" - 4.5 sao")
                .avatarUrl(null)
                .targetCourseId("2")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId("R456")
                .senderName("Nguyễn Thị D")
                .courseTitle("Python cho người mới bắt đầu")
                .lessonTitle(null)
                .build());

        // Map cho cả userId và username để đảm bảo teacher nào cũng thấy thông báo fake
        notificationsByUser.put("teacher", teacherNotifications); // username
        notificationsByUser.put("u2", teacherNotifications);      // id của Nguyễn A (theo AuthFakeApiService)
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
     * Dùng cho FakeApiService để tìm teacherId từ tên teacher
     *
     * NOTE: Trong RemoteApiService sẽ cần query từ database để lấy đúng teacherId
     */
    public String getTeacherIdByName(String teacherName) {
        if (teacherName == null) return "u2"; // Fallback: Nguyễn A

        // Map tên teacher trong sample data → userId từ AuthFakeApiService
        switch (teacherName.trim()) {
            case "Nguyễn A":
                return "u2";  // Teacher chính (username: teacher)
            case "Teacher Assistant":
                return "u5";  // Teacher phụ (username: teacher2)
            default:
                return "u2";  // Fallback: mặc định là Nguyễn A
        }
    }
}
