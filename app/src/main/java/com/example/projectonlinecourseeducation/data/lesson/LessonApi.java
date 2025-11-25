package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.Lesson;

import java.util.List;

public interface LessonApi {

    // Mỗi khóa học có danh sách bài học riêng
    List<Lesson> getLessonsForCourse(String courseId);
}
