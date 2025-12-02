package com.example.projectonlinecourseeducation.core.model.lesson;

/**

 Model cho thông tin video của một bài học
 Bao gồm: thông tin video, URL, comment người dùng, quiz*/
public class LessonVideo {
    private final String id;              // lesson_id từ lesson
    private final String title;           // tiêu đề bài học
    private final String description;     // mô tả chi tiết bài học
    private final String videoUrl;        // URL YouTube (videoId)
    private final String duration;        // thời lượng video
    private final int order;              // vị trí bài học trong khóa học
    private final String courseId;        // khóa học chứa bài này

    public LessonVideo(String id, String title, String description, String videoUrl,
                       String duration, int order, String courseId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.order = order;
        this.courseId = courseId;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVideoUrl() { return videoUrl; }
    public String getDuration() { return duration; }
    public int getOrder() { return order; }
    public String getCourseId() { return courseId; }
}