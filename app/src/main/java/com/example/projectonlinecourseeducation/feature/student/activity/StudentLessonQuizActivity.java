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
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
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
            if (nextLessonId == null) {
                Toast.makeText(this,
                        "Đang tải bài tiếp theo, vui lòng thử lại.",
                        Toast.LENGTH_SHORT).show();
                resolveNextLessonIdFallbackAsync();
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

                    boolean relevant =
                            changedLessonId == null
                                    || changedLessonId.isEmpty()
                                    || (lessonId != null && lessonId.equals(changedLessonId));

                    if (!relevant) return;

                    // ⬇️ BỌC CALL API ĐÚNG CHUẨN REMOTE
                    AsyncApiHelper.execute(
                            () -> {
                                // ===== BACKGROUND THREAD =====
                                return lessonQuizApi.getQuizForLesson(lessonId);
                            },
                            new AsyncApiHelper.ApiCallback<Quiz>() {
                                @Override
                                public void onSuccess(Quiz newQuiz) {
                                    // ===== MAIN THREAD =====
                                    if (newQuiz == null) {
                                        Toast.makeText(
                                                StudentLessonQuizActivity.this,
                                                "Quiz đã bị xóa.",
                                                Toast.LENGTH_SHORT
                                        ).show();
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

                                    // restore submit button to default state
                                    restoreSubmitButtonToDefault();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(
                                            StudentLessonQuizActivity.this,
                                            "Không thể tải lại quiz.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );
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

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    return lessonQuizApi.getQuizForLesson(lessonId);
                },
                new AsyncApiHelper.ApiCallback<Quiz>() {
                    @Override
                    public void onSuccess(Quiz result) {
                        if (result == null) {
                            Toast.makeText(StudentLessonQuizActivity.this,
                                    "Quiz chưa có cho bài này.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        quiz = result;
                        tvQuizTitle.setText(
                                quiz.getTitle() != null ? quiz.getTitle() : "Quiz"
                        );

                        adapter.submitList(quiz.getQuestions());

                        btnGoNext.setEnabled(false);
                        btnGoNext.setVisibility(Button.GONE);
                        tvScore.setVisibility(TextView.GONE);
                        tvMessage.setVisibility(TextView.GONE);

                        restoreSubmitButtonToDefault();

                        if (nextLessonId == null) {
                            resolveNextLessonIdFallbackAsync();
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(StudentLessonQuizActivity.this,
                                "Lỗi tải quiz",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    /**
     * Fallback method: try to find next lesson from LessonApi using courseId and current lessonId.
     * Returns next lessonId or null if not found.
     */
    private void resolveNextLessonIdFallbackAsync() {
        if (courseId == null || lessonId == null) return;

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    LessonApi lessonApi = ApiProvider.getLessonApi();
                    if (lessonApi == null) return null;

                    List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
                    if (lessons == null || lessons.isEmpty()) return null;

                    for (int i = 0; i < lessons.size(); i++) {
                        Lesson current = lessons.get(i);
                        if (current == null) continue;

                        if (lessonId.equals(current.getId())) {
                            int nextIndex = i + 1;
                            if (nextIndex < lessons.size()) {
                                Lesson next = lessons.get(nextIndex);
                                if (next != null) {
                                    return next.getId(); // ✅ FOUND
                                }
                            }
                            break;
                        }
                    }
                    return null; // không có bài tiếp theo
                },
                new AsyncApiHelper.ApiCallback<String>() {
                    @Override
                    public void onSuccess(String id) {
                        // ===== MAIN THREAD =====
                        nextLessonId = id;
                        // Không toast ở đây vì đây là fallback silent
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                StudentLessonQuizActivity.this,
                                "Không thể xác định bài học tiếp theo",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    private void onSubmit() {
        if (quiz == null) {
            Toast.makeText(this, "Quiz chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để nộp quiz", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> answers = new HashMap<>();
        for (QuizQuestion q : quiz.getQuestions()) {
            Integer sel = chosenMap.get(q.getId());
            answers.put(q.getId(), sel == null ? -1 : sel);
        }

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND =====
                    return lessonQuizApi.submitQuizAttempt(
                            lessonId,
                            currentUser.getId(),
                            answers
                    );
                },
                new AsyncApiHelper.ApiCallback<QuizAttempt>() {
                    @Override
                    public void onSuccess(QuizAttempt attempt) {
                        if (attempt == null) {
                            Toast.makeText(StudentLessonQuizActivity.this,
                                    "Không thể nộp quiz.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showResult(attempt);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(StudentLessonQuizActivity.this,
                                "Lỗi mạng khi nộp quiz",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showResult(QuizAttempt attempt) {
        tvScore.setVisibility(TextView.VISIBLE);
        tvMessage.setVisibility(TextView.VISIBLE);

        int correctCount = attempt.getCorrectCount();
        int total = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;

        tvScore.setText(
                "Điểm: " + correctCount + "/" + total + " (" + attempt.getScore() + "%)"
        );

        if (attempt.isPassed()) {
            tvMessage.setText("Bạn đã đạt yêu cầu! Các câu sai (nếu có) đã được hiển thị.");
            btnGoNext.setEnabled(true);
            btnGoNext.setVisibility(Button.VISIBLE);

            adapter.revealWrongAnswers(attempt.getAnswers());

            btnSubmit.setText("Làm lại");
            btnSubmit.setBackgroundColor(Color.parseColor("#FF9800"));
            btnSubmit.setTextColor(Color.WHITE);

            btnSubmit.setOnClickListener(v -> {
                chosenMap.clear();
                adapter.clearReveal();
                adapter.submitList(quiz.getQuestions());

                tvScore.setVisibility(TextView.GONE);
                tvMessage.setVisibility(TextView.GONE);
                btnGoNext.setVisibility(Button.GONE);
                btnGoNext.setEnabled(false);

                restoreSubmitButtonToDefault();
            });

        } else {
            tvMessage.setText("Chưa đạt yêu cầu. Vui lòng làm lại (>=8/10).");
            btnGoNext.setEnabled(false);
            btnGoNext.setVisibility(Button.GONE);
            adapter.clearReveal();
        }
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
