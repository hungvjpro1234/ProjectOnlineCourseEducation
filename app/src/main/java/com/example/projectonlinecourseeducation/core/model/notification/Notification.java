package com.example.projectonlinecourseeducation.core.model.notification;

/**
 * Model đại diện cho một thông báo trong hệ thống
 * Hỗ trợ 3 role: Student, Teacher, Admin với các loại thông báo khác nhau
 */
public class Notification {

    // ========== NOTIFICATION TYPES ==========
    public enum NotificationType {
        // Student notifications
        TEACHER_REPLY_COMMENT,           // Teacher reply comment của student trong lesson

        // Teacher notifications (Student → Teacher)
        STUDENT_COURSE_COMMENT,          // Student comment vào khóa học (review/đánh giá)
        STUDENT_LESSON_COMMENT           // Student comment vào bài học cụ thể
    }

    // ========== NOTIFICATION STATUS ==========
    public enum NotificationStatus {
        UNREAD,    // Chưa xem - background sẫm màu, đếm badge +1
        VIEWED,    // Đã xem - background sẫm màu, badge reset
        READ       // Đã đọc - background transparent, badge reset
    }

    // ========== FIELDS ==========
    private final String id;                    // Mã thông báo (UUID)
    private final String userId;                // User nhận thông báo
    private final NotificationType type;        // Loại thông báo
    private NotificationStatus status;          // Trạng thái thông báo (mutable)
    private final long createdAt;               // Timestamp tạo thông báo
    private final String title;                 // Tiêu đề thông báo
    private final String message;               // Nội dung thông báo
    private final String avatarUrl;             // Avatar của người gửi (optional)

    // Navigation data (để navigate đến đúng màn hình khi click)
    private final String targetCourseId;        // ID khóa học liên quan (nullable)
    private final String targetLessonId;        // ID bài học liên quan (nullable)
    private final String targetCommentId;       // ID comment liên quan (nullable)
    private final String targetReviewId;        // ID review liên quan (nullable)
    private final String senderName;            // Tên người gửi (teacher/student name)

    // Additional metadata
    private final String courseTitle;           // Tên khóa học (để hiển thị)
    private final String lessonTitle;           // Tên bài học (để hiển thị)

    // Constructor đầy đủ
    public Notification(String id, String userId, NotificationType type, NotificationStatus status,
                        long createdAt, String title, String message, String avatarUrl,
                        String targetCourseId, String targetLessonId, String targetCommentId,
                        String targetReviewId, String senderName, String courseTitle, String lessonTitle) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.title = title;
        this.message = message;
        this.avatarUrl = avatarUrl;
        this.targetCourseId = targetCourseId;
        this.targetLessonId = targetLessonId;
        this.targetCommentId = targetCommentId;
        this.targetReviewId = targetReviewId;
        this.senderName = senderName;
        this.courseTitle = courseTitle;
        this.lessonTitle = lessonTitle;
    }

    // ========== BUILDER PATTERN (Recommended) ==========
    public static class Builder {
        private String id;
        private String userId;
        private NotificationType type;
        private NotificationStatus status = NotificationStatus.UNREAD; // Default
        private long createdAt = System.currentTimeMillis();
        private String title;
        private String message;
        private String avatarUrl;
        private String targetCourseId;
        private String targetLessonId;
        private String targetCommentId;
        private String targetReviewId;
        private String senderName;
        private String courseTitle;
        private String lessonTitle;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder type(NotificationType type) { this.type = type; return this; }
        public Builder status(NotificationStatus status) { this.status = status; return this; }
        public Builder createdAt(long createdAt) { this.createdAt = createdAt; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder targetCourseId(String targetCourseId) { this.targetCourseId = targetCourseId; return this; }
        public Builder targetLessonId(String targetLessonId) { this.targetLessonId = targetLessonId; return this; }
        public Builder targetCommentId(String targetCommentId) { this.targetCommentId = targetCommentId; return this; }
        public Builder targetReviewId(String targetReviewId) { this.targetReviewId = targetReviewId; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder courseTitle(String courseTitle) { this.courseTitle = courseTitle; return this; }
        public Builder lessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; return this; }

        public Notification build() {
            return new Notification(id, userId, type, status, createdAt, title, message, avatarUrl,
                    targetCourseId, targetLessonId, targetCommentId, targetReviewId,
                    senderName, courseTitle, lessonTitle);
        }
    }

    // ========== GETTERS ==========
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public NotificationStatus getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getTargetCourseId() { return targetCourseId; }
    public String getTargetLessonId() { return targetLessonId; }
    public String getTargetCommentId() { return targetCommentId; }
    public String getTargetReviewId() { return targetReviewId; }
    public String getSenderName() { return senderName; }
    public String getCourseTitle() { return courseTitle; }
    public String getLessonTitle() { return lessonTitle; }

    // ========== SETTERS (chỉ cho status - mutable) ==========
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    // ========== HELPER METHODS ==========
    /**
     * Kiểm tra thông báo có được đếm vào badge không (UNREAD)
     */
    public boolean shouldCountBadge() {
        return status == NotificationStatus.UNREAD;
    }

    /**
     * Kiểm tra thông báo có background sẫm màu không (UNREAD hoặc VIEWED)
     */
    public boolean shouldHighlight() {
        return status == NotificationStatus.UNREAD || status == NotificationStatus.VIEWED;
    }

    /**
     * Đánh dấu thông báo đã xem (chuyển UNREAD → VIEWED)
     */
    public void markAsViewed() {
        if (status == NotificationStatus.UNREAD) {
            status = NotificationStatus.VIEWED;
        }
    }

    /**
     * Đánh dấu thông báo đã đọc (chuyển UNREAD/VIEWED → READ)
     */
    public void markAsRead() {
        status = NotificationStatus.READ;
    }
}
