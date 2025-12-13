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

        notificationsByUser.put("student1", studentNotifications);

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
                .type(NotificationType.STUDENT_COURSE_REVIEW)
                .status(NotificationStatus.UNREAD)
                .createdAt(twoDaysAgo)
                .title("Học viên mới đánh giá khóa học")
                .message("Trần Học Sinh đã đánh giá khóa học \"Lập trình Java từ cơ bản đến nâng cao\" - 5 sao")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId("R123")
                .senderName("Trần Học Sinh")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle(null)
                .build());

        // Thông báo 3: Course approved (VIEWED)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.COURSE_APPROVED)
                .status(NotificationStatus.VIEWED)
                .createdAt(threeDaysAgo)
                .title("Khóa học đã được phê duyệt")
                .message("Admin đã phê duyệt khóa học \"Lập trình Java từ cơ bản đến nâng cao\"")
                .avatarUrl(null)
                .targetCourseId("1")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Admin")
                .courseTitle("Lập trình Java từ cơ bản đến nâng cao")
                .lessonTitle(null)
                .build());

        // Thông báo 4: Course rejected (READ)
        teacherNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("teacher")
                .type(NotificationType.COURSE_REJECTED)
                .status(NotificationStatus.READ)
                .createdAt(oneWeekAgo)
                .title("Khóa học bị từ chối")
                .message("Admin đã từ chối chỉnh sửa khóa học \"Python cho người mới bắt đầu\". Lý do: Nội dung chưa đầy đủ")
                .avatarUrl(null)
                .targetCourseId("2")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Admin")
                .courseTitle("Python cho người mới bắt đầu")
                .lessonTitle(null)
                .build());

        notificationsByUser.put("teacher", teacherNotifications);

        // ========== ADMIN NOTIFICATIONS (admin) ==========
        List<Notification> adminNotifications = new ArrayList<>();

        // Thông báo 1: Course create pending (UNREAD)
        adminNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("admin")
                .type(NotificationType.COURSE_CREATE_PENDING)
                .status(NotificationStatus.UNREAD)
                .createdAt(oneHourAgo)
                .title("Yêu cầu tạo khóa học mới")
                .message("Nguyễn Văn A đã tạo khóa học mới \"Lập trình C++ nâng cao\" cần phê duyệt")
                .avatarUrl(null)
                .targetCourseId("10")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Nguyễn Văn A")
                .courseTitle("Lập trình C++ nâng cao")
                .lessonTitle(null)
                .build());

        // Thông báo 2: Course edit pending (UNREAD)
        adminNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("admin")
                .type(NotificationType.COURSE_EDIT_PENDING)
                .status(NotificationStatus.UNREAD)
                .createdAt(twoDaysAgo)
                .title("Yêu cầu chỉnh sửa khóa học")
                .message("Trần Thị B đã chỉnh sửa khóa học \"Python cho người mới bắt đầu\" cần phê duyệt")
                .avatarUrl(null)
                .targetCourseId("2")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Trần Thị B")
                .courseTitle("Python cho người mới bắt đầu")
                .lessonTitle(null)
                .build());

        // Thông báo 3: Course delete pending (VIEWED)
        adminNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("admin")
                .type(NotificationType.COURSE_DELETE_PENDING)
                .status(NotificationStatus.VIEWED)
                .createdAt(threeDaysAgo)
                .title("Yêu cầu xóa khóa học")
                .message("Lê Văn C đã yêu cầu xóa khóa học \"JavaScript ES6 toàn tập\"")
                .avatarUrl(null)
                .targetCourseId("3")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Lê Văn C")
                .courseTitle("JavaScript ES6 toàn tập")
                .lessonTitle(null)
                .build());

        // Thông báo 4: Course create pending (READ)
        adminNotifications.add(new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId("admin")
                .type(NotificationType.COURSE_CREATE_PENDING)
                .status(NotificationStatus.READ)
                .createdAt(oneWeekAgo)
                .title("Yêu cầu tạo khóa học mới")
                .message("Phạm Văn D đã tạo khóa học mới \"React Native từ A-Z\" cần phê duyệt")
                .avatarUrl(null)
                .targetCourseId("11")
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName("Phạm Văn D")
                .courseTitle("React Native từ A-Z")
                .lessonTitle(null)
                .build());

        notificationsByUser.put("admin", adminNotifications);
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
    public Notification createCourseCreateNotification(String adminId, String teacherName,
                                                       String courseId, String courseTitle) {
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(adminId)
                .type(NotificationType.COURSE_CREATE_PENDING)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Yêu cầu tạo khóa học mới")
                .message(teacherName + " đã tạo khóa học mới \"" + courseTitle + "\" cần phê duyệt")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName(teacherName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
                .build();

        addNotificationToUser(adminId, notification);
        return notification;
    }

    @Override
    public Notification createCourseEditNotification(String adminId, String teacherName,
                                                     String courseId, String courseTitle) {
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(adminId)
                .type(NotificationType.COURSE_EDIT_PENDING)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Yêu cầu chỉnh sửa khóa học")
                .message(teacherName + " đã chỉnh sửa khóa học \"" + courseTitle + "\" cần phê duyệt")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName(teacherName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
                .build();

        addNotificationToUser(adminId, notification);
        return notification;
    }

    @Override
    public Notification createCourseDeleteNotification(String adminId, String teacherName,
                                                       String courseId, String courseTitle) {
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(adminId)
                .type(NotificationType.COURSE_DELETE_PENDING)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Yêu cầu xóa khóa học")
                .message(teacherName + " đã yêu cầu xóa khóa học \"" + courseTitle + "\"")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName(teacherName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
                .build();

        addNotificationToUser(adminId, notification);
        return notification;
    }

    @Override
    public Notification createStudentReviewNotification(String teacherId, String studentName,
                                                        String courseId, String courseTitle,
                                                        String reviewId, float rating) {
        String ratingText = String.format("%.1f sao", rating);
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(teacherId)
                .type(NotificationType.STUDENT_COURSE_REVIEW)
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
    public Notification createStudentCommentNotification(String teacherId, String studentName,
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

    @Override
    public Notification createCourseApprovedNotification(String teacherId, String adminName,
                                                         String courseId, String courseTitle,
                                                         String approvalType) {
        String actionText = approvalType.equals("create") ? "tạo" : "chỉnh sửa";
        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(teacherId)
                .type(NotificationType.COURSE_APPROVED)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Khóa học đã được phê duyệt")
                .message(adminName + " đã phê duyệt " + actionText + " khóa học \"" + courseTitle + "\"")
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName(adminName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
                .build();

        addNotificationToUser(teacherId, notification);
        return notification;
    }

    @Override
    public Notification createCourseRejectedNotification(String teacherId, String adminName,
                                                         String courseId, String courseTitle,
                                                         String rejectType, String reason) {
        String actionText;
        switch (rejectType) {
            case "create": actionText = "tạo"; break;
            case "edit": actionText = "chỉnh sửa"; break;
            case "delete": actionText = "xóa"; break;
            default: actionText = "cập nhật";
        }

        String messageText = adminName + " đã từ chối " + actionText + " khóa học \"" + courseTitle + "\"";
        if (reason != null && !reason.trim().isEmpty()) {
            messageText += ". Lý do: " + reason;
        }

        Notification notification = new Notification.Builder()
                .id(UUID.randomUUID().toString())
                .userId(teacherId)
                .type(NotificationType.COURSE_REJECTED)
                .status(NotificationStatus.UNREAD)
                .createdAt(System.currentTimeMillis())
                .title("Khóa học bị từ chối")
                .message(messageText)
                .avatarUrl(null)
                .targetCourseId(courseId)
                .targetLessonId(null)
                .targetCommentId(null)
                .targetReviewId(null)
                .senderName(adminName)
                .courseTitle(courseTitle)
                .lessonTitle(null)
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
