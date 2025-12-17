package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.course.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class CourseRemoteApiService implements CourseApi {

    private final CourseRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(CourseRetrofitService.class);

    // ================= MAPPING =================

    private Course map(CourseDto d) {
        if (d == null) return null;

        Course c = new Course();
        c.setId(d.id);
        c.setTitle(d.title);
        c.setTeacher(d.teacher);
        c.setImageUrl(d.imageUrl);
        c.setCategory(d.category);
        c.setLectures(d.lectures);
        c.setStudents(d.students);
        c.setRating(d.rating);
        c.setPrice(d.price);

        c.setDescription(d.description);
        c.setCreatedAt(d.createdAt);
        c.setRatingCount(d.ratingCount);
        c.setTotalDurationMinutes(d.totalDurationMinutes);
        c.setSkills(d.skills);
        c.setRequirements(d.requirements);

        c.setInitialApproved(d.isInitialApproved);
        c.setEditApproved(d.isEditApproved);
        c.setDeleteRequested(d.isDeleteRequested);

        return c;
    }

    // ================= LIST =================

    @Override
    public List<Course> listAll() {
        try {
            Response<CoursesResponse> res = api.getAllCourses().execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                List<Course> result = new ArrayList<>();
                for (CourseDto d : res.body().data) {
                    Course c = map(d);
                    if (c != null) result.add(c);
                }
                return result;
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    /**
     * Backend chưa support filter/search/sort
     * → fallback trả toàn bộ list
     */
    @Override
    public List<Course> filterSearchSort(
            String categoryOrAll,
            String query,
            Sort sort,
            int limit
    ) {
        return listAll();
    }

    // ================= DETAIL =================

    @Override
    public Course getCourseDetail(String courseId) {
        if (courseId == null) return null;
        try {
            Response<CourseDto> res = api.getCourseDetail(courseId).execute();
            if (res.isSuccessful() && res.body() != null) {
                return map(res.body());
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ================= UNUSED / BACKEND NOT READY =================

    @Override
    public List<Course> getCoursesByTeacher(String teacherName) {
        return new ArrayList<>();
    }

    @Override
    public List<Course> getRelatedCourses(String courseId) {
        return new ArrayList<>();
    }

    @Override
    public Course createCourse(Course newCourse) {
        return null;
    }

    @Override
    public Course updateCourse(String id, Course updatedCourse) {
        return null;
    }

    @Override
    public boolean deleteCourse(String id) {
        return false;
    }

    @Override
    public Course recalculateCourseRating(String courseId) {
        return null;
    }

    @Override
    public Course recordPurchase(String courseId) {
        return null;
    }

    // ================= APPROVAL =================

    @Override
    public List<Course> getPendingCourses() {
        return new ArrayList<>();
    }

    @Override
    public boolean approveInitialCreation(String courseId) {
        return false;
    }

    @Override
    public boolean rejectInitialCreation(String courseId) {
        return false;
    }

    @Override
    public boolean approveCourseEdit(String courseId) {
        return false;
    }

    @Override
    public boolean rejectCourseEdit(String courseId) {
        return false;
    }

    @Override
    public Course getPendingEdit(String courseId) {
        return null;
    }

    @Override
    public boolean hasPendingEdit(String courseId) {
        return false;
    }

    @Override
    public boolean permanentlyDeleteCourse(String courseId) {
        return false;
    }

    @Override
    public boolean cancelDeleteRequest(String courseId) {
        return false;
    }

    // ================= LISTENER (REMOTE = NO-OP) =================

    @Override
    public void addCourseUpdateListener(CourseUpdateListener l) {
        // NO-OP
    }

    @Override
    public void removeCourseUpdateListener(CourseUpdateListener l) {
        // NO-OP
    }
}
