package com.example.projectonlinecourseeducation.data.lessonquiz.remote;

import java.util.Map;

public class QuizAttemptDto {
    public String id;
    public String quizId;
    public String lessonId;
    public String studentId;

    public Map<String, Integer> answers;

    public int correctCount;
    public int scorePercent;
    public boolean passed;

    public long createdAt;
}
