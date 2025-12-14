package com.example.projectonlinecourseeducation.data.notification;

import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationStatus;

import java.util.List;

/**
 * Interface cho API quản lý thông báo
 * Hỗ trợ 2 role: Student và Teacher
 *
 * NOTIFICATION FLOW:
 * - Student: Nhận thông báo khi teacher reply comment
 * - Teacher: Nhận thông báo khi student comment vào lesson hoặc review course
 */
public interface NotificationApi {

    // ================ QUERY NOTIFICATIONS ================

    /**
     * Lấy tất cả thông báo của user
     * @param userId ID của user
     * @return Danh sách thông báo, sắp xếp theo thời gian (mới nhất trước)
     */
    List<Notification> getNotificationsForUser(String userId);

    /**
     * Lấy thông báo chưa đọc (UNREAD) của user
     * @param userId ID của user
     * @return Danh sách thông báo UNREAD
     */
    List<Notification> getUnreadNotifications(String userId);

    /**
     * Đếm số lượng thông báo chưa đọc (UNREAD) - hiển thị badge
     * @param userId ID của user
     * @return Số lượng thông báo UNREAD
     */
    int getUnreadCount(String userId);

    /**
     * Lấy thông báo theo loại
     * @param userId ID của user
     * @param type Loại thông báo
     * @return Danh sách thông báo của loại đó
     */
    List<Notification> getNotificationsByType(String userId, NotificationType type);

    /**
     * Lấy chi tiết một thông báo
     * @param notificationId ID của thông báo
     * @return Notification hoặc null nếu không tìm thấy
     */
    Notification getNotificationById(String notificationId);

    // ================ UPDATE NOTIFICATION STATUS ================

    /**
     * Đánh dấu thông báo đã xem (UNREAD → VIEWED)
     * Dùng khi user mở NotificationFragment
     * @param notificationId ID của thông báo
     * @return Notification đã update hoặc null nếu không tìm thấy
     */
    Notification markAsViewed(String notificationId);

    /**
     * Đánh dấu thông báo đã đọc (UNREAD/VIEWED → READ)
     * Dùng khi user click vào thông báo
     * @param notificationId ID của thông báo
     * @return Notification đã update hoặc null nếu không tìm thấy
     */
    Notification markAsRead(String notificationId);

    /**
     * Đánh dấu tất cả thông báo của user đã xem (UNREAD → VIEWED)
     * Dùng khi user mở NotificationFragment
     * @param userId ID của user
     * @return Số lượng thông báo đã được update
     */
    int markAllAsViewed(String userId);

    /**
     * Đánh dấu tất cả thông báo của user đã đọc (UNREAD/VIEWED → READ)
     * @param userId ID của user
     * @return Số lượng thông báo đã được update
     */
    int markAllAsRead(String userId);

    // ================ CREATE NOTIFICATIONS ================

    /**
     * Tạo thông báo khi teacher reply comment của student
     * @param studentId ID của student nhận thông báo
     * @param teacherName Tên teacher đã reply
     * @param lessonId ID của lesson
     * @param lessonTitle Tên lesson
     * @param courseId ID của course
     * @param courseTitle Tên course
     * @param commentId ID của comment được reply
     * @return Notification vừa tạo
     */
    Notification createTeacherReplyNotification(String studentId, String teacherName,
                                                String lessonId, String lessonTitle,
                                                String courseId, String courseTitle,
                                                String commentId);

    /**
     * Tạo thông báo cho teacher khi student review khóa học (đánh giá + comment)
     * Type: STUDENT_COURSE_COMMENT
     * @param teacherId ID của teacher nhận thông báo
     * @param studentName Tên student
     * @param courseId ID của course
     * @param courseTitle Tên course
     * @param reviewId ID của review
     * @param rating Rating của student (1-5 sao)
     * @return Notification vừa tạo
     */
    Notification createStudentCourseReviewNotification(String teacherId, String studentName,
                                                       String courseId, String courseTitle,
                                                       String reviewId, float rating);

    /**
     * Tạo thông báo cho teacher khi student comment trong lesson cụ thể
     * Type: STUDENT_LESSON_COMMENT
     * @param teacherId ID của teacher nhận thông báo
     * @param studentName Tên student
     * @param lessonId ID của lesson
     * @param lessonTitle Tên lesson
     * @param courseId ID của course
     * @param courseTitle Tên course
     * @param commentId ID của comment
     * @return Notification vừa tạo
     */
    Notification createStudentLessonCommentNotification(String teacherId, String studentName,
                                                        String lessonId, String lessonTitle,
                                                        String courseId, String courseTitle,
                                                        String commentId);

    // ================ DELETE NOTIFICATIONS ================

    /**
     * Xóa một thông báo
     * @param notificationId ID của thông báo
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    boolean deleteNotification(String notificationId);

    /**
     * Xóa tất cả thông báo đã đọc của user
     * @param userId ID của user
     * @return Số lượng thông báo đã xóa
     */
    int deleteReadNotifications(String userId);

    /**
     * Xóa tất cả thông báo của user
     * @param userId ID của user
     * @return Số lượng thông báo đã xóa
     */
    int deleteAllNotifications(String userId);

    // ================ LISTENER / NOTIFY ================

    /**
     * Listener để UI đăng ký nhận thông báo khi có notification mới hoặc status thay đổi
     * Component nên gọi lại getNotificationsForUser() hoặc getUnreadCount() để cập nhật UI
     */
    interface NotificationUpdateListener {
        /**
         * Gọi khi có thông báo mới hoặc status thay đổi
         * @param userId ID của user bị ảnh hưởng
         */
        void onNotificationsChanged(String userId);
    }

    /**
     * Đăng ký listener
     */
    void addNotificationUpdateListener(NotificationUpdateListener listener);

    /**
     * Hủy đăng ký listener
     */
    void removeNotificationUpdateListener(NotificationUpdateListener listener);
}
