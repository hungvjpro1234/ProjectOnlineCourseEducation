package com.example.projectonlinecourseeducation.data.lessonquiz.remote;

import java.util.List;

public class QuizAttemptResponse {
    public boolean success;
    public String message;
    public QuizAttemptDto data;
    public List<QuizAttemptDto> list;
}
