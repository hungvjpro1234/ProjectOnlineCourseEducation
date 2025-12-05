package com.example.projectonlinecourseeducation.core.model.lesson;

/**
 * Model đại diện cho một bình luận trong bài học
 * Học sinh có thể bình luận để thắc mắc về bài giảng
 */
public class LessonComment {
    private final String id;           // Mã bình luận
    private final String lessonId;     // Mã bài học
    private final String userId;       // Mã học sinh bình luận
    private final String userName;     // Tên học sinh hiển thị
    private final String userAvatar;   // Avatar của học sinh (URL hoặc null)
    private final String content;      // Nội dung bình luận
    private final long createdAt;      // Timestamp tạo bình luận

    public LessonComment(String id, String lessonId, String userId, String userName,
                         String userAvatar, String content, long createdAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    // ========== GETTERS ==========
    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
}
