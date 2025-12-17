package com.example.projectonlinecourseeducation.data.lesson.remote;

public class LessonDto {

    public String id;
    public String courseId;
    public String title;
    public String description;
    public String videoUrl;
    public String duration;
    public int order;

    public boolean isInitialApproved;
    public boolean isEditApproved;
    public boolean isDeleteRequested;
}
