package com.example.projectonlinecourseeducation.data.lessonquiz.remote;

import java.util.Map;

public class SubmitQuizAttemptRequest {
    public String lessonId;
    public String studentId;
    public Map<String, Integer> answers;
}
