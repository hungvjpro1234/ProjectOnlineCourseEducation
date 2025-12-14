package com.example.projectonlinecourseeducation.data.course.remote;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface cho Course API endpoints
 * Base URL: http://10.0.2.2:3000 (emulator) hoặc http://192.168.1.XXX:3000 (physical device)
 *
 * Backend Endpoints (server.js lines 956-1589):
 * - POST /course - Create course (multipart/form-data)
 * - GET /course - List courses (with filters)
 * - GET /course/:id - Get course detail
 * - PATCH /course/:id - Update course (creates pending edit)
 * - POST /course/:id/purchase - Record purchase
 * - GET /course/pending - List pending courses (admin)
 * - POST /course/:id/approve-initial - Approve initial creation
 * - GET /course/:id/pending - Get pending edit
 * - POST /course/:id/approve-edit - Approve edit
 * - POST /course/:id/reject-edit - Reject edit
 * - POST /course/:id/request-delete - Request delete
 * - POST /course/:id/approve-delete - Approve delete (permanent)
 * - POST /course/:id/reject-delete - Reject delete
 * - POST /course/:id/recalculate-rating - Recalculate rating
 * - GET /course/:id/students - Get enrolled students
 */
public interface CourseRetrofitService {

    // ============ BASIC CRUD ============

    /**
     * POST /course
     * Create new course (multipart/form-data for image upload)
     *
     * Request:
     * - courseAvatar: MultipartBody.Part (image file)
     * - title: RequestBody (text/plain)
     * - description: RequestBody
     * - teacher: RequestBody
     * - category: RequestBody
     * - price: RequestBody
     * - skills: RequestBody (JSON array string or CSV)
     * - requirements: RequestBody (JSON array string or CSV)
     *
     * Response: { success, message, data: CourseDto }
     *
     * NOTE: Backend sets is_approved=false (pending admin approval)
     */
    @Multipart
    @POST("course")
    Call<CourseApiResponse<CourseDto>> createCourse(
        @Part MultipartBody.Part courseAvatar, // Optional, can be null
        @Part("title") RequestBody title,
        @Part("description") RequestBody description,
        @Part("teacher") RequestBody teacher,
        @Part("category") RequestBody category,
        @Part("price") RequestBody price,
        @Part("skills") RequestBody skills, // JSON array string
        @Part("requirements") RequestBody requirements // JSON array string
    );

    /**
     * GET /course
     * List courses with optional filters
     *
     * Query params:
     * - teacher: Filter by teacher name (optional)
     * - include_unapproved: "true" to include unapproved courses (admin only, requires auth)
     *
     * Response: { success, data: List<CourseDto> }
     *
     * NOTE: Frontend has filterSearchSort() with sort & search, but backend doesn't support yet.
     * For now, we get all courses and filter/sort on client side.
     */
    @GET("course")
    Call<CourseApiResponse<List<CourseDto>>> listCourses(
        @Query("teacher") String teacher,
        @Query("include_unapproved") String includeUnapproved // "true" or null
    );

    /**
     * GET /course/:id
     * Get course detail by ID
     *
     * Query params:
     * - include_pending: "true" to include pending edit data (optional)
     *
     * Response: { success, data: CourseDto }
     *
     * NOTE: If include_pending=true, response.data.pending will contain pending edit
     */
    @GET("course/{id}")
    Call<CourseApiResponse<CourseDto>> getCourseDetail(
        @Path("id") String courseId,
        @Query("include_pending") String includePending // "true" or null
    );

    /**
     * PATCH /course/:id
     * Update course (creates pending edit, requires admin approval)
     *
     * Request: Same as createCourse (multipart/form-data)
     *
     * Response: { success, message, data: pending edit record }
     *
     * NOTE: Doesn't update course directly, creates pending edit instead
     * Sets is_edit_approved=false
     */
    @Multipart
    @PATCH("course/{id}")
    Call<CourseApiResponse<Object>> updateCourse(
        @Path("id") String courseId,
        @Part MultipartBody.Part courseAvatar, // Optional
        @Part("title") RequestBody title,
        @Part("description") RequestBody description,
        @Part("teacher") RequestBody teacher,
        @Part("category") RequestBody category,
        @Part("price") RequestBody price,
        @Part("skills") RequestBody skills,
        @Part("requirements") RequestBody requirements
    );

    /**
     * POST /course/:id/purchase
     * Record purchase (increment students count)
     *
     * Response: { success, data: CourseDto }
     */
    @POST("course/{id}/purchase")
    Call<CourseApiResponse<CourseDto>> recordPurchase(@Path("id") String courseId);

    // ============ APPROVAL WORKFLOW ============

    /**
     * GET /course/pending
     * List all pending courses (admin only, requires auth)
     *
     * Response: { success, data: List<CourseDto> }
     *
     * Returns courses with:
     * - is_approved=false (pending initial creation) OR
     * - is_edit_approved=false (pending edit approval)
     */
    @GET("course/pending")
    Call<CourseApiResponse<List<CourseDto>>> getPendingCourses();

    /**
     * POST /course/:id/approve-initial
     * Approve initial course creation (admin only)
     *
     * Response: { success, message, data: CourseDto }
     *
     * Sets is_approved=true and is_edit_approved=true
     */
    @POST("course/{id}/approve-initial")
    Call<CourseApiResponse<CourseDto>> approveInitialCreation(@Path("id") String courseId);

    /**
     * GET /course/:id/pending
     * Get pending edit for a course
     *
     * Response: { success, data: pending_data, meta: ... }
     *
     * Returns null if no pending edit
     */
    @GET("course/{id}/pending")
    Call<CourseApiResponse<CourseDto>> getPendingEdit(@Path("id") String courseId);

    /**
     * POST /course/:id/approve-edit
     * Approve pending edit (admin only)
     *
     * Response: { success, data: CourseDto }
     *
     * Applies pending changes to course and sets is_edit_approved=true
     */
    @POST("course/{id}/approve-edit")
    Call<CourseApiResponse<CourseDto>> approveCourseEdit(@Path("id") String courseId);

    /**
     * POST /course/:id/reject-edit
     * Reject pending edit (admin only)
     *
     * Response: { success, message }
     *
     * Deletes pending edit and sets is_edit_approved=true
     */
    @POST("course/{id}/reject-edit")
    Call<CourseApiResponse<Object>> rejectCourseEdit(@Path("id") String courseId);

    // ============ DELETE WORKFLOW ============

    /**
     * POST /course/:id/request-delete
     * Teacher requests to delete course (soft delete)
     *
     * Response: { success, message }
     *
     * Sets is_delete_requested=true and is_edit_approved=false
     */
    @POST("course/{id}/request-delete")
    Call<CourseApiResponse<Object>> requestDelete(@Path("id") String courseId);

    /**
     * POST /course/:id/approve-delete
     * Admin approves delete request (permanent deletion)
     *
     * Response: { success, message, data: CourseDto }
     *
     * Permanently deletes course from database
     */
    @POST("course/{id}/approve-delete")
    Call<CourseApiResponse<CourseDto>> approveDelete(@Path("id") String courseId);

    /**
     * POST /course/:id/reject-delete
     * Admin rejects delete request
     *
     * Response: { success, message }
     *
     * Clears is_delete_requested and sets is_edit_approved=true
     */
    @POST("course/{id}/reject-delete")
    Call<CourseApiResponse<Object>> rejectDelete(@Path("id") String courseId);

    // ============ ADDITIONAL ENDPOINTS ============

    /**
     * POST /course/:id/recalculate-rating
     * Recalculate course rating from reviews
     *
     * Response: { success, data: CourseDto }
     *
     * Aggregates from course_review table
     */
    @POST("course/{id}/recalculate-rating")
    Call<CourseApiResponse<CourseDto>> recalculateRating(@Path("id") String courseId);

    /**
     * GET /course/:id/students
     * Get list of enrolled students (teacher/admin only)
     *
     * Response: { success, data: List<StudentDto> }
     *
     * NOTE: Not used by CourseApi, might be used by CourseStudentApi
     */
    @GET("course/{id}/students")
    Call<CourseApiResponse<List<Object>>> getEnrolledStudents(@Path("id") String courseId);

    // ============ MISSING BACKEND ENDPOINTS (TODO) ============
    // Frontend có những methods này nhưng backend chưa implement:
    // 1. GET /course/:id/related - Get related courses (same teacher or category)
    // 2. POST /course/:id/reject-initial - Reject initial creation (delete unapproved course)
    // 3. GET /course?query=XXX&sort=AZ&limit=10 - Search and sort (backend chỉ có teacher filter)
}
