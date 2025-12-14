package com.example.projectonlinecourseeducation.data.course.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO (Data Transfer Object) cho Course từ backend
 * Backend trả về: { id, title, description, teacher, imageUrl, category, lectures, students, rating, price, createdAt, ratingCount, totalDurationMinutes, skills, requirements, is_approved, is_edit_approved, is_delete_requested }
 *
 * NOTE: Backend transformCourseRow() đã convert snake_case → camelCase:
 * - course_id → id
 * - imageurl → imageUrl
 * - totaldurationminutes → totalDurationMinutes
 * - ratingcount → ratingCount
 * - created_at → createdAt
 */
public class CourseDto {

    @SerializedName("id")
    private String id; // Backend trả về integer, nhưng transformCourseRow() convert thành number, frontend cần parse

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("lectures")
    private int lectures;

    @SerializedName("students")
    private int students;

    @SerializedName("rating")
    private double rating;

    @SerializedName("price")
    private double price;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("ratingCount")
    private int ratingCount;

    @SerializedName("totalDurationMinutes")
    private int totalDurationMinutes;

    @SerializedName("skills")
    private List<String> skills;

    @SerializedName("requirements")
    private List<String> requirements;

    // Approval fields (backend uses is_approved, is_edit_approved, is_delete_requested)
    // But transformCourseRow() doesn't transform these, so we need both snake_case and camelCase
    @SerializedName("is_approved")
    private Boolean isApproved;

    @SerializedName("is_edit_approved")
    private Boolean isEditApproved;

    @SerializedName("is_delete_requested")
    private Boolean isDeleteRequested;

    // Pending edit data (optional, only in GET /course/:id?include_pending=true)
    @SerializedName("pending")
    private CourseDto pending;

    public CourseDto() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
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

    public int getLectures() {
        return lectures;
    }

    public void setLectures(int lectures) {
        this.lectures = lectures;
    }

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Boolean getIsEditApproved() {
        return isEditApproved;
    }

    public void setIsEditApproved(Boolean isEditApproved) {
        this.isEditApproved = isEditApproved;
    }

    public Boolean getIsDeleteRequested() {
        return isDeleteRequested;
    }

    public void setIsDeleteRequested(Boolean isDeleteRequested) {
        this.isDeleteRequested = isDeleteRequested;
    }

    public CourseDto getPending() {
        return pending;
    }

    public void setPending(CourseDto pending) {
        this.pending = pending;
    }
}
