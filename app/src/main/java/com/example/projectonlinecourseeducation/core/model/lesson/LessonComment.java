package com.example.projectonlinecourseeducation.core.model.lesson;

/**
 * Model đại diện cho một bình luận trong bài học
 * Học sinh có thể bình luận để thắc mắc về bài giảng
 * Giáo viên có thể trả lời bình luận
 *
 * UPDATED: Thêm hỗ trợ teacher reply và delete state
 */
public class LessonComment {
    private final String id;           // Mã bình luận
    private final String lessonId;     // Mã bài học
    private final String userId;       // Mã người bình luận
    private final String userName;     // Tên người bình luận
    private final String content;      // Nội dung bình luận
    private final long createdAt;      // Timestamp tạo bình luận
    private final boolean isDeleted;   // Đã bị xóa chưa

    // Teacher reply fields (nullable)
    private final String teacherReplyContent;  // Nội dung trả lời từ teacher
    private final String teacherReplyBy;       // Tên teacher trả lời
    private final Long teacherReplyAt;         // Timestamp teacher reply (null nếu chưa reply)

    // Constructor đầy đủ (với teacher reply)
    public LessonComment(String id, String lessonId, String userId, String userName,
                         String content, long createdAt, boolean isDeleted,
                         String teacherReplyContent, String teacherReplyBy, Long teacherReplyAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.teacherReplyContent = teacherReplyContent;
        this.teacherReplyBy = teacherReplyBy;
        this.teacherReplyAt = teacherReplyAt;
    }

    // Constructor legacy (không có teacher reply)
    public LessonComment(String id, String lessonId, String userId, String userName,
                         String content, long createdAt) {
        this(id, lessonId, userId, userName, content, createdAt, false, null, null, null);
    }

    // ========== GETTERS ==========
    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
    public boolean isDeleted() { return isDeleted; }

    public String getTeacherReplyContent() { return teacherReplyContent; }
    public String getTeacherReplyBy() { return teacherReplyBy; }
    public Long getTeacherReplyAt() { return teacherReplyAt; }

    // Helper methods
    public boolean hasTeacherReply() {
        return teacherReplyContent != null && !teacherReplyContent.trim().isEmpty();
    }
}