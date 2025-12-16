package com.example.projectonlinecourseeducation.data.mycourse.remote;

import com.example.projectonlinecourseeducation.core.model.course.Course;

public class CourseMapper {

    public static Course toCourse(CourseDto dto) {
        if (dto == null) return null;

        Course course = new Course();

        // ===== ID =====
        course.setId(String.valueOf(dto.getCourseId()));

        // ===== Basic info =====
        course.setTitle(dto.getTitle());
        course.setTeacher(dto.getTeacher() != null ? dto.getTeacher() : "");
        course.setImageUrl(dto.getImageUrl());
        course.setCategory(dto.getCategory());

        // ===== Numbers =====
        course.setStudents(dto.getStudents());
        course.setPrice(dto.getPrice());

        course.setRating(dto.getRating() != null ? dto.getRating() : 0.0);
        course.setRatingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0);
        course.setLectures(dto.getLectures() != null ? dto.getLectures() : 0);
        course.setTotalDurationMinutes(
                dto.getTotalDurationMinutes() != null ? dto.getTotalDurationMinutes() : 0
        );

        // ===== Detail =====
        course.setDescription(dto.getDescription());
        course.setCreatedAt(dto.getCreatedAt());

        course.setSkills(dto.getSkills());
        course.setRequirements(dto.getRequirements());

        // ===== Approval flags =====
        course.setInitialApproved(
                dto.getInitialApproved() != null ? dto.getInitialApproved() : true
        );
        course.setEditApproved(
                dto.getEditApproved() != null ? dto.getEditApproved() : true
        );
        course.setDeleteRequested(
                dto.getDeleteRequested() != null ? dto.getDeleteRequested() : false
        );

        return course;
    }
}
