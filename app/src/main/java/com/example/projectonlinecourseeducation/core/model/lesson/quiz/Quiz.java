// file: core/model/quiz/Quiz.java
package com.example.projectonlinecourseeducation.core.model.lesson.quiz;

import java.util.List;

/**
 * Quiz gắn với 1 lesson
 * - lessonId: id của lesson mà quiz thuộc về
 * - title: tiêu đề quiz
 * - questions: list câu hỏi (theo yêu cầu cố định: 10 câu nếu bạn muốn enforce)
 *
 * NOTE: Quiz chỉ là cấu trúc câu hỏi (không lưu kết quả attempt -> attempts riêng)
 */
public class Quiz {
    private String id;
    private String lessonId;
    private String title;
    private List<QuizQuestion> questions;

    public Quiz(String id, String lessonId, String title, List<QuizQuestion> questions) {
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.questions = questions;
    }

    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getTitle() { return title; }
    public List<QuizQuestion> getQuestions() { return questions; }
}
