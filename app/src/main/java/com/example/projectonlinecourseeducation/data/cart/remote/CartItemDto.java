package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for cart item from backend (course_payment_status table)
 * Backend GET /cart/:userId returns array of these
 */
public class CartItemDto {

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("course_id")
    private Integer courseId;

    @SerializedName("status")
    private String status; // "NOT_PURCHASED" | "IN_CART" | "PURCHASED"

    @SerializedName("price_snapshot")
    private Double priceSnapshot;

    @SerializedName("course_name")
    private String courseName;

    // Course fields (if backend JOINs with course table - recommended)
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private Double price;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("students")
    private Integer students;

    @SerializedName("totalDurationMinutes")
    private Integer totalDurationMinutes;

    public CartItemDto() {
    }

    // Getters and Setters

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(Double priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getStudents() {
        return students;
    }

    public void setStudents(Integer students) {
        this.students = students;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }
}
