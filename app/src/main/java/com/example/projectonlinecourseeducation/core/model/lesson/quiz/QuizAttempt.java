// file: core/model/quiz/QuizAttempt.java
package com.example.projectonlinecourseeducation.core.model.lesson.quiz;

import java.util.Map;

/**
 * Lưu 1 lần làm quiz của 1 student
 *
 * - id: attempt id
 * - quizId: quiz
 * - lessonId: lesson (dễ truy vấn)
 * - studentId
 * - answers: map questionId -> chosenOptionIndex (-1 nếu bỏ qua)
 * - correctCount, score (0..100), passed (true nếu >= threshold)
 * - createdAt: timestamp
 */
public class QuizAttempt {
    private String id;
    private String quizId;
    private String lessonId;
    private String studentId;
    private java.util.Map<String, Integer> answers;
    private int correctCount;
    private int score; // percent
    private boolean passed;
    private long createdAt;

    public QuizAttempt(String id,
                       String quizId,
                       String lessonId,
                       String studentId,
                       java.util.Map<String, Integer> answers,
                       int correctCount,
                       int score,
                       boolean passed,
                       long createdAt) {
        this.id = id;
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.answers = answers;
        this.correctCount = correctCount;
        this.score = score;
        this.passed = passed;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getQuizId() { return quizId; }
    public String getLessonId() { return lessonId; }
    public String getStudentId() { return studentId; }
    public java.util.Map<String, Integer> getAnswers() { return answers; }
    public int getCorrectCount() { return correctCount; }
    public int getScore() { return score; }
    public boolean isPassed() { return passed; }
    public long getCreatedAt() { return createdAt; }
}
