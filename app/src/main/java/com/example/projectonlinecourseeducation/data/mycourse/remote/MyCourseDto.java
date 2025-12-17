package com.example.projectonlinecourseeducation.data.mycourse.remote;

public class MyCourseDto {

    public String id;
    public String title;
    public String description;

    // Backend field
    public String thumbnailUrl;

    // Frontend alias (để map với Course.imageUrl)
    public String imageUrl;

    public float price;
    public boolean isApproved;

    /**
     * Helper: đảm bảo imageUrl luôn có giá trị
     * Gọi sau khi Gson parse xong
     */
    public void normalize() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = thumbnailUrl;
        }
    }
}
