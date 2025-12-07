package com.example.projectonlinecourseeducation.core.model.lesson;

/**
 * Model cho tracking progress của từng bài học (hỗ trợ per-student).
 */
public class LessonProgress {
    private String id;                // progress_id (ví dụ "progress_c1_l1_u123")
    private String lessonId;          // lesson_id
    private String courseId;          // course_id
    private String studentId;         // NEW: id của student (null nếu global/legacy)
    private float currentSecond;      // vị trí hiện tại trong video (giây)
    private float totalSecond;        // tổng thời lượng video (giây)
    private int completionPercentage; // 0-100%
    private boolean isCompleted;      // đã hoàn thành hay chưa (>= 90%)
    private long updatedAt;           // thời gian cập nhật cuối cùng (epoch millis)

    public LessonProgress(String id, String lessonId, String courseId, String studentId,
                          float currentSecond, float totalSecond,
                          int completionPercentage, boolean isCompleted,
                          long updatedAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.currentSecond = currentSecond;
        this.totalSecond = totalSecond;
        this.completionPercentage = clampPercent(completionPercentage);
        // đảm bảo consistency: isCompleted phản ánh completionPercentage
        this.isCompleted = isCompleted || this.completionPercentage >= 90;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getCourseId() { return courseId; }
    public String getStudentId() { return studentId; }
    public float getCurrentSecond() { return currentSecond; }
    public float getTotalSecond() { return totalSecond; }
    public int getCompletionPercentage() { return completionPercentage; }
    public boolean isCompleted() { return isCompleted; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters (cần thiết khi cập nhật từ API hoặc UI)
    public void setCurrentSecond(float currentSecond) { this.currentSecond = currentSecond; }
    public void setTotalSecond(float totalSecond) { this.totalSecond = totalSecond; }
    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = clampPercent(completionPercentage);
        this.isCompleted = this.completionPercentage >= 90;
    }
    public void setIsCompleted(boolean completed) { this.isCompleted = completed; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    private int clampPercent(int p) {
        if (p < 0) return 0;
        if (p > 100) return 100;
        return p;
    }
}
