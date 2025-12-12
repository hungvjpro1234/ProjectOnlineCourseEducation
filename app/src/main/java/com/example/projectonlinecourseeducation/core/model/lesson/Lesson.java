package com.example.projectonlinecourseeducation.core.model.lesson;

/**
 * Model cho Lesson - bài học trong khóa học
 * Bao gồm: thông tin video, URL, duration (được lấy từ video thực tế), tracking progress
 *
 * NOTE: Đã chuyển sang mutable (có setters) vì code hiện tại trong project
 *       đang gọi các phương thức setXxx() (ví dụ: updateLesson).
 *       Nếu bạn muốn immutable, hãy refactor chỗ gọi setters để tạo object mới.
 */
public class Lesson {
    private String id;                // lesson_id
    private String courseId;          // khóa học chứa bài này
    private String title;             // tiêu đề bài học
    private String description;       // mô tả chi tiết bài học
    private String videoUrl;          // URL YouTube video (videoId)
    private String duration;          // thời lượng video (lấy từ video thực tế)
    private int order;                // vị trí bài học trong khóa học

    // --- Approval fields (giống Course) ---
    private boolean isInitialApproved = true;  // Đã duyệt khởi tạo (default true cho compatibility)
    private boolean isEditApproved = true;     // Đã duyệt chỉnh sửa
    private boolean isDeleteRequested = false; // Yêu cầu xóa đang chờ duyệt

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

    // Constructor with approval fields
    public Lesson(String id, String courseId, String title, String description,
                  String videoUrl, String duration, int order,
                  boolean isInitialApproved, boolean isEditApproved, boolean isDeleteRequested) {
        this(id, courseId, title, description, videoUrl, duration, order);
        this.isInitialApproved = isInitialApproved;
        this.isEditApproved = isEditApproved;
        this.isDeleteRequested = isDeleteRequested;
    }

    // Getters
    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVideoUrl() { return videoUrl; }
    public String getDuration() { return duration; }
    public int getOrder() { return order; }

    // Setters (đã thêm để hỗ trợ update tại runtime)
    public void setId(String id) { this.id = id; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setOrder(int order) { this.order = order; }

    // Approval getters/setters
    public boolean isInitialApproved() { return isInitialApproved; }
    public void setInitialApproved(boolean initialApproved) { isInitialApproved = initialApproved; }

    public boolean isEditApproved() { return isEditApproved; }
    public void setEditApproved(boolean editApproved) { isEditApproved = editApproved; }

    public boolean isDeleteRequested() { return isDeleteRequested; }
    public void setDeleteRequested(boolean deleteRequested) { isDeleteRequested = deleteRequested; }

    // Helper methods
    /**
     * Kiểm tra xem lesson có đang chờ phê duyệt không
     * @return true nếu đang chờ phê duyệt (khởi tạo, chỉnh sửa, hoặc xóa)
     */
    public boolean isPendingApproval() {
        return !isInitialApproved || !isEditApproved || isDeleteRequested;
    }

    /**
     * Lấy text trạng thái phê duyệt để hiển thị trên UI
     * @return Text trạng thái hoặc empty string nếu đã approved
     */
    public String getApprovalStatusText() {
        if (isDeleteRequested) return "Chờ duyệt xóa";
        else if (!isInitialApproved) return "Chờ duyệt khởi tạo";
        else if (!isEditApproved) return "Chờ duyệt chỉnh sửa";
        return "";
    }
}
