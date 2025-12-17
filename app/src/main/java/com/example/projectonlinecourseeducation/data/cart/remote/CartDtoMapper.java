package com.example.projectonlinecourseeducation.data.cart.remote;

import com.example.projectonlinecourseeducation.core.model.course.Course;

public final class CartDtoMapper {

    private CartDtoMapper() {}

    public static Course toCourse(CartCourseDto dto) {
        if (dto == null) return new Course();

        Course c = new Course();
        c.setId(dto.getId());
        c.setTitle(nullSafe(dto.getTitle()));
        c.setImageUrl(nullSafe(dto.getImageUrl()));
        c.setTeacher(nullSafe(dto.getTeacher()));
        c.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        c.setRating(dto.getRating() != null ? dto.getRating() : 0.0);
        return c;
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    public static Integer parseCourseId(String courseId) {
        try {
            return Integer.parseInt(courseId);
        } catch (Exception e) {
            return null;
        }
    }
}
