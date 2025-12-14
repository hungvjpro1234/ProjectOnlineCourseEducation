// file: core/model/quiz/QuizQuestion.java
package com.example.projectonlinecourseeducation.core.model.lesson.quiz;

import java.util.List;

/**
 * Một câu hỏi trong quiz
 *
 * Quy định cho project: mỗi câu hỏi **CHỈ** có **4** lựa chọn (4 options).
 *
 * - id: id câu hỏi (local)
 * - question: nội dung câu hỏi
 * - options: list 4 đáp án (index: 0..3)
 * - correctOptionIndex: index (0-based) chỉ ra đáp án đúng (phải trong [0,3])
 *
 * LƯU Ý: Đây chỉ là model; việc validate được thực hiện tại service (create/update/submit).
 */
public class QuizQuestion {
    private String id;
    private String question;
    private java.util.List<String> options;
    private int correctOptionIndex;

    public QuizQuestion(String id, String question, java.util.List<String> options, int correctOptionIndex) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getId() { return id; }
    public String getQuestion() { return question; }
    public java.util.List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }

    // Helper convenience: check basic validity of this question according to project rule
    public boolean isValidFourOptions() {
        if (options == null) return false;
        if (options.size() != 4) return false;
        if (correctOptionIndex < 0 || correctOptionIndex > 3) return false;
        for (String s : options) {
            if (s == null || s.trim().isEmpty()) return false; // require non-empty option text
        }
        if (question == null || question.trim().isEmpty()) return false;
        return true;
    }
}
