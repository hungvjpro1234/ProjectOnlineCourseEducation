package com.example.projectonlinecourseeducation.core.model.lesson;

/**

 Model cho tracking progress của từng bài học
 Bao gồm: tiến độ xem video, completion percentage*/
public class LessonProgress {
    private final String id;                // progress_id
    private final String lessonId;          // lesson_id
    private final String courseId;          // course_id
    private final float currentSecond;      // vị trí hiện tại trong video (giây)
    private final float totalSecond;        // tổng thời lượng video (giây)
    private final int completionPercentage; // 0-100%
    private final boolean isCompleted;      // đã hoàn thành hay chưa (>= 90%)
    private final long lastUpdatedTime;     // thời gian cập nhật cuối cùng

    public LessonProgress(String id, String lessonId, String courseId,
                          float currentSecond, float totalSecond,
                          int completionPercentage, boolean isCompleted,
                          long lastUpdatedTime) {
        this.id = id;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.currentSecond = currentSecond;
        this.totalSecond = totalSecond;
        this.completionPercentage = completionPercentage;
        this.isCompleted = isCompleted;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    // Getters
    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getCourseId() { return courseId; }
    public float getCurrentSecond() { return currentSecond; }
    public float getTotalSecond() { return totalSecond; }
    public int getCompletionPercentage() { return completionPercentage; }
    public boolean isCompleted() { return isCompleted; }
    public long getLastUpdatedTime() { return lastUpdatedTime; }
}