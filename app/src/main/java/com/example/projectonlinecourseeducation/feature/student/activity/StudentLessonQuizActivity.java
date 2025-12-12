package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.feature.student.adapter.QuizQuestionAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity xử lý quiz cho 1 lesson.
 *
 * - Hiển thị danh sách câu hỏi (RecyclerView)
 * - Submit answers -> gọi LessonQuizApi.submitQuizAttempt(...)
 * - Nếu pass (>=8/10) -> hiển thị câu sai (nếu có) và bật nút sang bài tiếp
 * - Nếu fail -> thông báo và yêu cầu làm lại
 *
 * Đăng ký:
 * - QuizUpdateListener: reload quiz nếu quiz bị create/update/delete.
 */
public class StudentLessonQuizActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvQuestions;
    private TextView tvQuizTitle;
    private Button btnSubmit, btnGoNext;
    private TextView tvScore, tvMessage;

    private String lessonId;
    private String nextLessonId; // optional
    private String courseId; // for fallback lookup

    private LessonQuizApi lessonQuizApi;
    private Quiz quiz;

    private QuizQuestionAdapter adapter;

    // map questionId -> chosenIndex
    private final Map<String, Integer> chosenMap = new HashMap<>();

    // listener để reload khi quiz thay đổi (create/update/delete)
    private LessonQuizApi.QuizUpdateListener quizUpdateListener;

    // Keep original submit button appearance so we can restore it when user retries
    private Drawable btnSubmitDefaultBackground;
    private CharSequence btnSubmitDefaultText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_lesson_quiz);

        // bind views
        btnBack = findViewById(R.id.btnBackQuiz);
        rvQuestions = findViewById(R.id.rvQuestions);
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoNext = findViewById(R.id.btnGoNext);
        tvScore = findViewById(R.id.tvScore);
        tvMessage = findViewById(R.id.tvMessage);

        // save default appearance for later restore
        btnSubmitDefaultBackground = btnSubmit.getBackground();
        btnSubmitDefaultText = btnSubmit.getText();

        lessonId = getIntent().getStringExtra("lesson_id");
        nextLessonId = getIntent().getStringExtra("next_lesson_id");
        courseId = getIntent().getStringExtra("course_id");

        // LẤY API QUIZ từ ApiProvider (đảm bảo ApiProvider có getLessonQuizApi())
        lessonQuizApi = ApiProvider.getLessonQuizApi();

        // Handle system back (gesture / hardware) using AndroidX OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to StudentCoursePurchasedActivity (keeps logic consistent)
                Intent intent = new Intent(StudentLessonQuizActivity.this, StudentCoursePurchasedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setupRecycler();
        loadQuiz();

        btnSubmit.setOnClickListener(v -> onSubmit());

        btnGoNext.setOnClickListener(v -> {
            // If we still don't have nextLessonId, try to resolve it now (fallback)
            if (nextLessonId == null) {
                nextLessonId = resolveNextLessonIdFallback();
            }

            if (nextLessonId == null) {
                Toast.makeText(this, "Không có bài tiếp theo.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, StudentLessonVideoActivity.class);
            intent.putExtra("lesson_id", nextLessonId);
            startActivity(intent);
            finish();
        });

        // Back ImageButton should go to StudentCoursePurchasedActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentCoursePurchasedActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Đăng ký quiz listener để reload nếu quiz thay đổi
        if (lessonQuizApi != null && quizUpdateListener == null) {
            quizUpdateListener = new LessonQuizApi.QuizUpdateListener() {
                @Override
                public void onQuizChanged(String changedLessonId) {
                    boolean relevant = (changedLessonId == null || changedLessonId.isEmpty()
                            || (lessonId != null && lessonId.equals(changedLessonId)));
                    if (!relevant) return;

                    // đảm bảo update UI trên main thread
                    runOnUiThread(() -> {
                        try {
                            Quiz newQuiz = lessonQuizApi.getQuizForLesson(lessonId);
                            if (newQuiz == null) {
                                // quiz bị xóa trong lúc student đang mở -> thông báo và finish
                                Toast.makeText(StudentLessonQuizActivity.this, "Quiz đã bị xóa.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                            // reload quiz content, clear selections/results
                            quiz = newQuiz;
                            chosenMap.clear();
                            adapter.submitList(quiz.getQuestions());
                            tvScore.setVisibility(TextView.GONE);
                            tvMessage.setVisibility(TextView.GONE);
                            btnGoNext.setVisibility(Button.GONE);
                            btnGoNext.setEnabled(false);

                            // restore submit button to default state in case it was switched to retry
                            restoreSubmitButtonToDefault();
                        } catch (Exception e) {
                            // an toàn: nếu lỗi, show toast nhỏ
                            Toast.makeText(StudentLessonQuizActivity.this, "Không thể tải lại quiz.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            lessonQuizApi.addQuizUpdateListener(quizUpdateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lessonQuizApi != null && quizUpdateListener != null) {
            lessonQuizApi.removeQuizUpdateListener(quizUpdateListener);
            quizUpdateListener = null;
        }
    }

    private void setupRecycler() {
        adapter = new QuizQuestionAdapter((questionId, selectedIndex) -> {
            // update chosen map
            if (questionId != null) {
                chosenMap.put(questionId, selectedIndex);
            }
        });
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);
    }

    private void loadQuiz() {
        if (lessonId == null) {
            Toast.makeText(this, "Không xác định lesson.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            quiz = lessonQuizApi.getQuizForLesson(lessonId);
        } catch (Exception e) {
            quiz = null;
        }

        if (quiz == null) {
            Toast.makeText(this, "Quiz chưa có cho bài này.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvQuizTitle.setText(quiz.getTitle() != null ? quiz.getTitle() : "Quiz");
        List<QuizQuestion> qs = quiz.getQuestions();
        adapter.submitList(qs);

        // reset result UI
        btnGoNext.setEnabled(false);
        btnGoNext.setVisibility(Button.GONE);
        tvScore.setVisibility(TextView.GONE);
        tvMessage.setVisibility(TextView.GONE);

        // Ensure submit button is default
        restoreSubmitButtonToDefault();

        // If nextLessonId not provided via intent, attempt to resolve now (best-effort)
        if (nextLessonId == null) {
            nextLessonId = resolveNextLessonIdFallback();
        }
    }

    /**
     * Fallback method: try to find next lesson from LessonApi using courseId and current lessonId.
     * Returns next lessonId or null if not found.
     */
    private String resolveNextLessonIdFallback() {
        if (courseId == null || lessonId == null) return null;
        try {
            LessonApi lessonApi = ApiProvider.getLessonApi();
            if (lessonApi == null) return null;
            List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
            if (lessons == null || lessons.isEmpty()) return null;
            for (int i = 0; i < lessons.size(); i++) {
                Lesson l = lessons.get(i);
                if (l == null) continue;
                if (lessonId.equals(l.getId())) {
                    int nextIdx = i + 1;
                    if (nextIdx < lessons.size()) {
                        Lesson next = lessons.get(nextIdx);
                        if (next != null) return next.getId();
                    }
                    break;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void onSubmit() {
        if (quiz == null) {
            Toast.makeText(this, "Quiz chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare answers map questionId -> chosenIndex (if null -> -1)
        Map<String, Integer> answers = new HashMap<>();
        List<QuizQuestion> qs = quiz.getQuestions();
        if (qs == null) {
            Toast.makeText(this, "Không có câu hỏi.", Toast.LENGTH_SHORT).show();
            return;
        }
        for (QuizQuestion q : qs) {
            if (q == null) continue;
            Integer sel = chosenMap.get(q.getId());
            answers.put(q.getId(), sel == null ? -1 : sel);
        }

        // Ensure student logged in
        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để nộp quiz", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call API to submit attempt; FakeApi sẽ validate lesson completed và quiz structure.
        QuizAttempt attempt = null;
        try {
            attempt = lessonQuizApi.submitQuizAttempt(lessonId, currentUser.getId(), answers);
        } catch (Exception e) {
            attempt = null;
        }

        if (attempt == null) {
            // Không thể nộp -> có thể do lesson chưa hoàn thành hoặc validation fail
            Toast.makeText(this, "Không thể nộp quiz. Kiểm tra điều kiện (hoàn thành lesson).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show result UI
        tvScore.setVisibility(TextView.VISIBLE);
        tvMessage.setVisibility(TextView.VISIBLE);
        int correctCount = attempt.getCorrectCount();
        int total = (quiz.getQuestions() != null) ? quiz.getQuestions().size() : 0;
        tvScore.setText("Điểm: " + correctCount + "/" + total + " (" + attempt.getScore() + "%)");

        if (attempt.isPassed()) {
            // PASS: reveal wrongs (nếu có) và show correct answers; enable goNext
            tvMessage.setText("Bạn đã đạt yêu cầu! Các câu sai (nếu có) đã được hiển thị.");
            btnGoNext.setEnabled(true);
            btnGoNext.setVisibility(Button.VISIBLE);

            // adapter sẽ highlight: selected-wrong (red) + correct (green)
            // QuizAttempt.getAnswers() expected Map<String,Integer>
            Map<String, Integer> attemptAnswers = attempt.getAnswers();
            adapter.revealWrongAnswers(attemptAnswers);

            // Change submit button to \"Làm lại\" (Retry) and change color to indicate retry action
            btnSubmit.setText("Làm lại");
            try {
                btnSubmit.setBackgroundColor(Color.parseColor("#FF9800")); // orange, visible
                btnSubmit.setTextColor(Color.WHITE);
            } catch (Exception ignored) {}

            // Change submit button behavior to perform a retry/reset when tapped
            btnSubmit.setOnClickListener(v -> {
                // clear selections and reveals so student can retake
                chosenMap.clear();
                adapter.clearReveal();
                adapter.submitList(quiz.getQuestions()); // re-bind current questions

                // hide score/message and goNext until next submission
                tvScore.setVisibility(TextView.GONE);
                tvMessage.setVisibility(TextView.GONE);
                btnGoNext.setVisibility(Button.GONE);
                btnGoNext.setEnabled(false);

                // restore submit button to original appearance and behavior
                restoreSubmitButtonToDefault();
            });

        } else {
            // FAIL: theo yêu cầu không reveal answers, user phải thử lại
            tvMessage.setText("Chưa đạt yêu cầu. Vui lòng làm lại (>=8/10).");
            btnGoNext.setEnabled(false);
            btnGoNext.setVisibility(Button.GONE);

            // clear any previous reveal and allow retry
            adapter.clearReveal();

            // keep submit button as-is (Nộp bài) so user can change answers and submit again
        }

        // Note: FakeApi already lưu attempt và sẽ notify AttemptListeners (admin có thể lắng nghe).
    }

    /**
     * Restore submit button to default appearance and set click to submit behavior.
     */
    private void restoreSubmitButtonToDefault() {
        if (btnSubmitDefaultBackground != null) {
            btnSubmit.setBackground(btnSubmitDefaultBackground);
        }
        if (btnSubmitDefaultText != null) {
            btnSubmit.setText(btnSubmitDefaultText);
        }
        // default text color - rely on layout/default. Fallback to black if necessary
        btnSubmit.setTextColor(Color.WHITE);
        btnSubmit.setOnClickListener(v -> onSubmit());
    }
}
