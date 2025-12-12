package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity xem preview video của lesson (cho admin)
 * Sử dụng youtube-android-player library
 *
 * MỞ RỘNG: hiển thị Quiz (nếu có) ngay dưới Lesson Info.
 * - Quiz title: tvQuizTitle
 * - Các câu hỏi sẽ được dựng động vào llQuizContainer
 * - Đáp án đúng sẽ dùng drawable R.drawable.bg_option_correct (nổi bật)
 * - Đáp án khác dùng R.drawable.bg_option_transparent
 */
public class AdminLessonVideoPreviewActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private YouTubePlayerView youTubePlayerView;
    private TextView tvCourseTitle, tvLessonTitle, tvLessonOrder, tvDuration, tvDescription;
    private View loadingIndicator;

    // Quiz views
    private TextView tvQuizTitle;
    private LinearLayout llQuizContainer;

    private LessonApi lessonApi;
    private LessonQuizApi lessonQuizApi;
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    private String lessonId;
    private Lesson lesson;
    private Quiz lessonQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lesson_video_preview);

        initViews();
        initApis();
        setupListeners();

        lessonId = getIntent().getStringExtra("lesson_id");
        String courseTitle = getIntent().getStringExtra("course_title");

        if (courseTitle != null) {
            tvCourseTitle.setText(courseTitle);
        }

        if (lessonId != null) {
            loadLessonData();
            fetchQuizForLesson(); // load quiz (non-blocking)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy lessonId", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        youTubePlayerView = findViewById(R.id.youTubePlayerView);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonOrder = findViewById(R.id.tvLessonOrder);
        tvDuration = findViewById(R.id.tvDuration);
        tvDescription = findViewById(R.id.tvDescription);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Quiz view bindings (new)
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        llQuizContainer = findViewById(R.id.llQuizContainer);

        // Add YouTubePlayerView to lifecycle
        getLifecycle().addObserver(youTubePlayerView);
    }

    private void initApis() {
        lessonApi = ApiProvider.getLessonApi();
        lessonQuizApi = ApiProvider.getLessonQuizApi();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLessonData() {
        loadingIndicator.setVisibility(View.VISIBLE);

        bgExecutor.execute(() -> {
            try {
                lesson = lessonApi.getLessonDetail(lessonId);

                if (lesson == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    displayLessonInfo(lesson);
                    initYouTubePlayer(lesson.getVideoUrl());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayLessonInfo(Lesson lesson) {
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonOrder.setText("Bài " + lesson.getOrder());
        tvDuration.setText("⏱ " + lesson.getDuration());

        if (lesson.getDescription() != null && !lesson.getDescription().isEmpty()) {
            tvDescription.setText(lesson.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
    }

    private void initYouTubePlayer(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "Video ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                // cueVideo expects video id, if you stored a full URL earlier ensure you extract ID.
                youTubePlayer.cueVideo(videoId, 0f);
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer,
                                @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                Toast.makeText(AdminLessonVideoPreviewActivity.this,
                        "Lỗi phát video: " + error.name(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- Quiz ----------------

    private void fetchQuizForLesson() {
        if (lessonQuizApi == null) return;

        bgExecutor.execute(() -> {
            try {
                Quiz q = lessonQuizApi.getQuizForLesson(lessonId);
                runOnUiThread(() -> {
                    lessonQuiz = q;
                    renderQuiz();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    // fail silently but log if needed
                });
            }
        });
    }

    /**
     * Render quiz: card per question (white card), question title bold and dark.
     * Correct option uses R.drawable.bg_option_correct, others use bg_option_transparent.
     */
    private void renderQuiz() {
        if (tvQuizTitle == null || llQuizContainer == null) return;

        llQuizContainer.removeAllViews();

        if (lessonQuiz == null) {
            tvQuizTitle.setText("Quiz: (chưa có)");
            return;
        }

        // Title styling
        tvQuizTitle.setText(lessonQuiz.getTitle() != null && !lessonQuiz.getTitle().isEmpty() ? lessonQuiz.getTitle() : "Quiz");
        tvQuizTitle.setTextSize(17f);
        tvQuizTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvQuizTitle.setTextColor(getResources().getColor(android.R.color.black));
        tvQuizTitle.setPadding(12, 8, 12, 8);

        List<QuizQuestion> questions = lessonQuiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Quiz chưa có câu hỏi");
            tvEmpty.setPadding(12, 12, 12, 12);
            llQuizContainer.addView(tvEmpty);
            return;
        }

        int idx = 1;
        for (QuizQuestion q : questions) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(12, 8, 12, 8);
            card.setLayoutParams(cardLp);
            card.setRadius(12f);
            card.setCardElevation(6f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(18, 14, 18, 14);

            // question text
            TextView tvQ = new TextView(this);
            tvQ.setText(idx + ". " + (q.getQuestion() != null ? q.getQuestion() : ""));
            tvQ.setTextSize(16f);
            tvQ.setTypeface(null, android.graphics.Typeface.BOLD);
            tvQ.setTextColor(getResources().getColor(android.R.color.black));
            tvQ.setPadding(6, 6, 6, 10);
            inner.addView(tvQ);

            // options
            List<String> opts = q.getOptions();
            int correct = q.getCorrectOptionIndex();

            if (opts != null) {
                for (int i = 0; i < opts.size(); i++) {
                    String text = opts.get(i);

                    // wrapper so background drawable can have margin
                    LinearLayout optWrapper = new LinearLayout(this);
                    LinearLayout.LayoutParams wrapLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    wrapLp.setMargins(6, 6, 6, 6);
                    optWrapper.setLayoutParams(wrapLp);

                    TextView tvOpt = new TextView(this);
                    tvOpt.setTextSize(14f);
                    tvOpt.setPadding(14, 12, 14, 12);

                    if (i == correct) {
                        tvOpt.setText("✓ " + text);
                        tvOpt.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvOpt.setBackgroundResource(R.drawable.bg_option_correct);
                        tvOpt.setTextColor(getResources().getColor(android.R.color.black));
                    } else {
                        tvOpt.setText("• " + text);
                        tvOpt.setBackgroundResource(R.drawable.bg_option_transparent);
                        tvOpt.setTextColor(getResources().getColor(android.R.color.black));
                    }

                    LinearLayout.LayoutParams optLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tvOpt.setLayoutParams(optLp);

                    optWrapper.addView(tvOpt);
                    inner.addView(optWrapper);
                }
            }

            card.addView(inner);
            llQuizContainer.addView(card);
            idx++;
        }
    }

    // ---------------- lifecycle ----------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            youTubePlayerView.release();
        } catch (Exception ignored) {}
        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}
