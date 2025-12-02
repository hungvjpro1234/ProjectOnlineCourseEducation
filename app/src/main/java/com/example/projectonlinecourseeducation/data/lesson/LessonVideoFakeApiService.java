package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

/**
 * Fake API Service cho LessonVideo
 * Mock dữ liệu video của các bài học.
 *
 * Hiện tại dùng chung data với LessonFakeApiService.
 * Mục tiêu: giữ nguyên interface LessonVideoApi nhưng dùng model Lesson
 * và chỉ còn một nguồn dữ liệu duy nhất.
 */
public class LessonVideoFakeApiService implements LessonVideoApi {

    // Singleton
    private static LessonVideoFakeApiService instance;

    public static LessonVideoFakeApiService getInstance() {
        if (instance == null) instance = new LessonVideoFakeApiService();
        return instance;
    }

    // Dùng LessonApi làm source data
    private final LessonApi lessonApi;

    private LessonVideoFakeApiService() {
        this.lessonApi = LessonFakeApiService.getInstance();
    }

    @Override
    public Lesson getLessonVideoDetail(String lessonId) {
        // Dùng chung logic với LessonApi
        return lessonApi.getLessonDetail(lessonId);
    }
}
