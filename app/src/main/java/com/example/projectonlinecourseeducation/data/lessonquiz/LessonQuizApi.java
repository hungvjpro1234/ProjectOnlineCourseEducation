// file: data/lessonquiz/LessonQuizApi.java
package com.example.projectonlinecourseeducation.data.lessonquiz;

import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt;

import java.util.List;
import java.util.Map;

/**
 * Interface API cho Quiz (per-lesson quiz)
 *
 * Quy tắc business chính (documented here):
 * - Quiz gắn 1-1 với lesson (lessonId)
 * - Quiz cố định 10 câu (implementer có thể enforce)
 * - Student chỉ có thể bắt đầu quiz khi LessonProgress.getLessonProgress(lessonId, studentId).isCompleted() == true
 *   -> implementer fake/remote có thể kiểm tra điều này.
 * - Quiz không tự động thay đổi lesson progress (quiz standalone). Việc unlock lesson tiếp theo nên do UI/flow xử lý dựa vào attempt.passed.
 */
public interface LessonQuizApi {

    /**
     * Lấy Quiz (câu hỏi) cho lesson
     * @param lessonId id lesson
     * @return Quiz hoặc null nếu chưa có
     */
    Quiz getQuizForLesson(String lessonId);

    /**
     * Create quiz cho lesson (teacher/admin)
     * @param quiz quiz object (quiz.id có thể null -> service tạo id)
     * @return quiz đã lưu (có id)
     */
    Quiz createQuiz(Quiz quiz);

    /**
     * Cập nhật quiz cho lesson
     * @param quizId id quiz
     * @param updated updated quiz (lessonId must match)
     * @return Quiz sau khi cập nhật, null nếu không tìm thấy
     */
    Quiz updateQuiz(String quizId, Quiz updated);

    /**
     * Xóa quiz
     * @param quizId id quiz
     * @return true nếu xóa thành công
     */
    boolean deleteQuiz(String quizId);

    /**
     * Student bắt đầu nộp quiz: answers map questionId -> chosenOptionIndex
     * - Implementer sẽ validate quiz tồn tại, số câu (=10) và permission (lesson completed).
     * - Trả về QuizAttempt đã tính: correctCount, score (0..100), passed boolean.
     *
     * @param lessonId lesson id
     * @param studentId student id
     * @param answers map questionId -> chosenOptionIndex
     * @return QuizAttempt object (saved), hoặc null nếu không hợp lệ / không có quyền
     */
    QuizAttempt submitQuizAttempt(String lessonId, String studentId, Map<String, Integer> answers);

    /**
     * Lấy danh sách attempts của 1 student cho 1 lesson (mới nhất trước)
     */
    List<QuizAttempt> getAttemptsForLesson(String lessonId, String studentId);

    /**
     * Lấy attempt theo id
     */
    QuizAttempt getAttemptById(String attemptId);

    // ---------------- Listener / notify ----------------

    /**
     * Listener cho thay đổi quiz (create/update/delete)
     */
    interface QuizUpdateListener {
        /**
         * Called when quiz for a lesson changed (create/update/delete)
         * @param lessonId nếu null nghĩa là thay đổi chung
         */
        void onQuizChanged(String lessonId);
    }

    void addQuizUpdateListener(QuizUpdateListener l);
    void removeQuizUpdateListener(QuizUpdateListener l);

    /**
     * Listener cho sự kiện attempt mới (student submit attempt)
     * Useful for admin dashboards or other UI to sync attempts live.
     */
    interface AttemptListener {
        /**
         * Called when a new attempt has been submitted and persisted.
         * @param attempt the saved QuizAttempt
         */
        void onAttemptSubmitted(QuizAttempt attempt);
    }

    void addAttemptListener(AttemptListener l);
    void removeAttemptListener(AttemptListener l);
}
