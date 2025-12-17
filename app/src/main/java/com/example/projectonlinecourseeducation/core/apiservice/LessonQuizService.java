package com.example.projectonlinecourseeducation.core.apiservice;

import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

/**
 * Remote implementation of LessonQuizApi
 *
 * - All calls are synchronous
 * - MUST be wrapped by AsyncApiHelper at UI layer
 * - Listener methods are NO-OP (backend không push realtime)
 */
public class LessonQuizService implements LessonQuizApi {

    private final LessonQuizRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(LessonQuizRetrofitService.class);

    // ===================== Mapping helpers =====================

    private QuizQuestion mapQuestion(QuizQuestionDto d) {
        if (d == null) return null;
        return new QuizQuestion(
                d.id,
                d.text,
                d.options,
                d.correctOptionIndex
        );
    }

    private Quiz mapQuiz(QuizDto d) {
        if (d == null) return null;

        List<QuizQuestion> questions = new ArrayList<>();
        if (d.questions != null) {
            for (QuizQuestionDto q : d.questions) {
                QuizQuestion qq = mapQuestion(q);
                if (qq != null) questions.add(qq);
            }
        }

        return new Quiz(
                d.id,
                d.lessonId,
                d.title,
                questions
        );
    }

    private QuizAttempt mapAttempt(QuizAttemptDto d) {
        if (d == null) return null;

        return new QuizAttempt(
                d.id,
                d.quizId,
                d.lessonId,
                d.studentId,
                d.answers != null ? new HashMap<>(d.answers) : new HashMap<>(),
                d.correctCount,
                d.scorePercent,
                d.passed,
                d.createdAt
        );
    }

    private QuizDto toDto(Quiz quiz) {
        if (quiz == null) return null;

        QuizDto d = new QuizDto();
        d.id = quiz.getId();
        d.lessonId = quiz.getLessonId();
        d.title = quiz.getTitle();

        d.questions = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                QuizQuestionDto qd = new QuizQuestionDto();
                qd.id = q.getId();
                qd.text = q.getQuestion();
                qd.options = q.getOptions();
                qd.correctOptionIndex = q.getCorrectOptionIndex();
                d.questions.add(qd);
            }
        }
        return d;
    }


    // ===================== API methods =====================

    @Override
    public Quiz getQuizForLesson(String lessonId) {
        try {
            Response<QuizResponse> res =
                    api.getQuizForLesson(lessonId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapQuiz(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Quiz createQuiz(Quiz quiz) {
        try {
            QuizDto dto = toDto(quiz);
            Response<QuizResponse> res =
                    api.createQuiz(dto).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapQuiz(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Quiz updateQuiz(String quizId, Quiz updated) {
        try {
            QuizDto dto = toDto(updated);
            Response<QuizResponse> res =
                    api.updateQuiz(quizId, dto).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapQuiz(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean deleteQuiz(String quizId) {
        try {
            return api.deleteQuiz(quizId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public QuizAttempt submitQuizAttempt(String lessonId,
                                         String studentId,
                                         Map<String, Integer> answers) {
        try {
            SubmitQuizAttemptRequest req = new SubmitQuizAttemptRequest();
            req.lessonId = lessonId;
            req.studentId = studentId;
            req.answers = answers;

            Response<QuizAttemptResponse> res =
                    api.submitQuizAttempt(req).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapAttempt(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<QuizAttempt> getAttemptsForLesson(String lessonId, String studentId) {
        List<QuizAttempt> result = new ArrayList<>();
        try {
            Response<QuizAttemptResponse> res =
                    api.getAttemptsForLesson(lessonId, studentId).execute();

            if (res.isSuccessful()
                    && res.body() != null
                    && res.body().list != null) {
                for (QuizAttemptDto d : res.body().list) {
                    QuizAttempt a = mapAttempt(d);
                    if (a != null) result.add(a);
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    @Override
    public QuizAttempt getAttemptById(String attemptId) {
        try {
            Response<QuizAttemptResponse> res =
                    api.getAttemptById(attemptId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapAttempt(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    // ===================== Listener (NO-OP) =====================

    @Override
    public void addQuizUpdateListener(QuizUpdateListener l) {
        // NO-OP (backend không realtime)
    }

    @Override
    public void removeQuizUpdateListener(QuizUpdateListener l) {
        // NO-OP
    }

    @Override
    public void addAttemptListener(AttemptListener l) {
        // NO-OP
    }

    @Override
    public void removeAttemptListener(AttemptListener l) {
        // NO-OP
    }
}
