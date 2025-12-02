package com.example.projectonlinecourseeducation.core.model.lesson;

/**

 Model cho Lesson - bài học trong khóa học
 Bao gồm: thông tin video, URL, duration (được lấy từ video thực tế), tracking progress*/
public class Lesson {
    private final String id;                // lesson_id
    private final String courseId;          // khóa học chứa bài này
    private final String title;             // tiêu đề bài học
    private final String description;       // mô tả chi tiết bài học
    private final String videoUrl;          // URL YouTube video (videoId)
    private final String duration;          // thời lượng video (lấy từ video thực tế)
    private final int order;                // vị trí bài học trong khóa học

    public Lesson(String id, String courseId, String title, String description,
                  String videoUrl, String duration, int order) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.order = order;
    }

    // Getters
    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVideoUrl() { return videoUrl; }
    public String getDuration() { return duration; }
    public int getOrder() { return order; }
}