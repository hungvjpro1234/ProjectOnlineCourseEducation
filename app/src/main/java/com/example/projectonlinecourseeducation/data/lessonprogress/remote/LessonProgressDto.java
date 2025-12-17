package com.example.projectonlinecourseeducation.data.lessonprogress.remote;

public class LessonProgressDto {

    public String id;
    public String lessonId;
    public String courseId;
    public String studentId;

    public float currentSecond;
    public float totalSecond;

    public int completionPercentage;
    public boolean isCompleted;

    public long updatedAt;
}
