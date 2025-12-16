package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminLessonCommentAdapter;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Variant of AdminLessonDetailActivity with colorful question blocks.
 * - Each question is shown inside a MaterialCardView with a soft pastel background.
 * - Correct option is emphasized and a subtle green tint is applied to it.
 * - This file only changes the quiz rendering (displayQuiz) to make it more lively.
 *
 * NOTE: Requires Material Components dependency in the app module (com.google.android.material:material).
 */
public class AdminLessonDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminLessonDetail";

    // Data
    private String lessonId;
    private Lesson lesson;
    private List<LessonComment> comments = new ArrayList<>();
    private Quiz lessonQuiz;

    // Views
    private ImageButton btnBack;
    private YouTubePlayerView youTubePlayerView;
    private ProgressBar pbVideoLoading;
    private TextView tvLessonTitle;
    private TextView tvLessonOrder;
    private TextView tvDuration;
    private TextView tvDescription;
    private TextView tvCommentCount;
    private RecyclerView rvComments;
    private View layoutCommentInput;
    private EditText etComment;
    private ImageButton btnSendComment;

    // Quiz views
    private LinearLayout llQuizContainer; // container inside Quiz Card
    private TextView tvQuizTitle;

    // Adapters
    private AdminLessonCommentAdapter commentAdapter;

    // APIs
    private LessonApi lessonApi;
    private LessonCommentApi commentApi;
    private LessonQuizApi lessonQuizApi;

    // Listeners
    private LessonCommentApi.LessonCommentUpdateListener commentUpdateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lesson_detail);

        // Get lesson ID from intent
        lessonId = getIntent() != null ? getIntent().getStringExtra("lesson_id") : null;

        if (lessonId == null || lessonId.trim().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupAdapters();
        registerCommentUpdateListener();

        fetchLessonDetail();
        fetchComments();
        fetchQuizForLesson(); // <-- load quiz
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        youTubePlayerView = findViewById(R.id.youTubePlayerView);
        pbVideoLoading = findViewById(R.id.pbVideoLoading);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonOrder = findViewById(R.id.tvLessonOrder);
        tvDuration = findViewById(R.id.tvDuration);
        tvDescription = findViewById(R.id.tvDescription);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        rvComments = findViewById(R.id.rvComments);
        layoutCommentInput = findViewById(R.id.layoutCommentInput);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);

        // Quiz views (mới)
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        llQuizContainer = findViewById(R.id.llQuizContainer);

        // ẨN comment input - Admin chỉ XEM và XÓA, không post comment
        if (layoutCommentInput != null) {
            layoutCommentInput.setVisibility(View.GONE);
        }

        // Add YouTube player to lifecycle
        getLifecycle().addObserver(youTubePlayerView);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupAdapters() {
        commentAdapter = new AdminLessonCommentAdapter(new AdminLessonCommentAdapter.OnCommentDeleteListener() {
            @Override
            public void onDeleteClick(LessonComment comment) {
                showDeleteConfirmDialog(comment);
            }

            @Override
            public void onDeleteReplyClick(LessonComment comment) {
                deleteReply(comment);
            }
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void fetchLessonDetail() {
        lessonApi = ApiProvider.getLessonApi();
        if (lessonApi == null) {
            Toast.makeText(this, "Lỗi: Lesson API chưa cấu hình", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncApiHelper.execute(
                () -> lessonApi.getLessonDetail(lessonId),
                new AsyncApiHelper.ApiCallback<Lesson>() {
                    @Override
                    public void onSuccess(Lesson l) {
                        if (l == null) {
                            Toast.makeText(AdminLessonDetailActivity.this,
                                    "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        lesson = l;
                        displayLessonInfo();
                        loadYouTubeVideo();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching lesson detail", e);
                        Toast.makeText(AdminLessonDetailActivity.this,
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void displayLessonInfo() {
        if (lesson == null) return;

        tvLessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "");
        tvLessonOrder.setText("Bài " + lesson.getOrder());
        tvDuration.setText(lesson.getDuration() != null ? lesson.getDuration() : "");
        tvDescription.setText(lesson.getDescription() != null ? lesson.getDescription() : "Chưa có mô tả");
    }

    private void loadYouTubeVideo() {
        if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().isEmpty()) {
            pbVideoLoading.setVisibility(View.GONE);
            Toast.makeText(this, "Bài học chưa có video", Toast.LENGTH_SHORT).show();
            return;
        }

        final String videoId = extractVideoId(lesson.getVideoUrl());
        if (videoId == null) {
            pbVideoLoading.setVisibility(View.GONE);
            Toast.makeText(this, "Video ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                pbVideoLoading.setVisibility(View.GONE);
                youTubePlayer.cueVideo(videoId, 0f);
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer,
                                @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                pbVideoLoading.setVisibility(View.GONE);
                Toast.makeText(AdminLessonDetailActivity.this,
                        "Lỗi khi tải video: " + error.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractVideoId(String videoUrl) {
        if (videoUrl == null) return null;

        if (videoUrl.length() == 11 && !videoUrl.contains("/") && !videoUrl.contains(".")) {
            return videoUrl;
        }

        try {
            if (videoUrl.contains("youtube.com/watch?v=")) {
                int start = videoUrl.indexOf("v=") + 2;
                int end = videoUrl.indexOf("&", start);
                if (end == -1) end = videoUrl.length();
                return videoUrl.substring(start, end);
            } else if (videoUrl.contains("youtu.be/")) {
                int start = videoUrl.indexOf("youtu.be/") + 9;
                int end = videoUrl.indexOf("?", start);
                if (end == -1) end = videoUrl.length();
                return videoUrl.substring(start, end);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video ID", e);
        }

        return videoUrl;
    }

    private void fetchComments() {
        commentApi = ApiProvider.getLessonCommentApi();
        if (commentApi == null) return;

        AsyncApiHelper.execute(
                () -> commentApi.getCommentsForLesson(lessonId),
                new AsyncApiHelper.ApiCallback<List<LessonComment>>() {
                    @Override
                    public void onSuccess(List<LessonComment> fetchedComments) {
                        if (fetchedComments == null) fetchedComments = new ArrayList<>();

                        comments = fetchedComments;
                        commentAdapter.setComments(comments);
                        updateCommentCount();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching comments", e);
                    }
                }
        );

    }

    private void updateCommentCount() {
        int count = comments.size();
        tvCommentCount.setText(count + " bình luận");
    }

    private void deleteReply(LessonComment comment) {
        if (commentApi == null) return;

        AsyncApiHelper.execute(
                () -> commentApi.deleteReply(comment.getId()),
                new AsyncApiHelper.ApiCallback<LessonComment>() {
                    @Override
                    public void onSuccess(LessonComment updated) {
                        if (updated != null) {
                            Toast.makeText(AdminLessonDetailActivity.this,
                                    "Đã xóa câu trả lời", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminLessonDetailActivity.this,
                                    "Lỗi khi xóa câu trả lời", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error deleting reply", e);
                        Toast.makeText(AdminLessonDetailActivity.this,
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showDeleteConfirmDialog(LessonComment comment) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc muốn xóa bình luận này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteComment(LessonComment comment) {
        if (commentApi == null) return;

        AsyncApiHelper.execute(
                () -> commentApi.markCommentAsDeleted(comment.getId()),
                new AsyncApiHelper.ApiCallback<LessonComment>() {
                    @Override
                    public void onSuccess(LessonComment updated) {
                        if (updated != null) {
                            Toast.makeText(AdminLessonDetailActivity.this,
                                    "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminLessonDetailActivity.this,
                                    "Lỗi khi xóa bình luận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error deleting comment", e);
                        Toast.makeText(AdminLessonDetailActivity.this,
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void registerCommentUpdateListener() {
        commentApi = ApiProvider.getLessonCommentApi();
        if (commentApi == null) return;

        commentUpdateListener = () -> runOnUiThread(() -> fetchComments());
        commentApi.addLessonCommentUpdateListener(commentUpdateListener);
    }

    // ------------------ Quiz: fetch + display (colorful cards) ------------------

    private void fetchQuizForLesson() {
        lessonQuizApi = ApiProvider.getLessonQuizApi();
        if (lessonQuizApi == null) {
            return;
        }

        AsyncApiHelper.execute(
                () -> lessonQuizApi.getQuizForLesson(lessonId),
                new AsyncApiHelper.ApiCallback<Quiz>() {
                    @Override
                    public void onSuccess(Quiz q) {
                        lessonQuiz = q;
                        displayQuiz();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching quiz", e);
                    }
                }
        );
    }

    /**
     * Render quiz using MaterialCardView cards per question with pastel colors.
     * Correct option is highlighted with a subtle green background and bold text.
     */
    /**
     * Render quiz: mỗi question nằm trên MaterialCard trắng.
     * - Không dùng palette pastel (card trắng, elevation nhẹ).
     * - Đáp án đúng: nổi bật với left-accent bar + nền xanh nhạt + viền xanh.
     * - Question title (tvQ) đậm hơn và màu tối để không bị lu mờ.
     */
    private void displayQuiz() {
        if (tvQuizTitle == null || llQuizContainer == null) return;

        llQuizContainer.removeAllViews();

        if (lessonQuiz == null) {
            tvQuizTitle.setText("Quiz: (chưa có)");
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có quiz cho bài học này");
            tvEmpty.setPadding(12, 12, 12, 12);
            tvEmpty.setTextSize(14f);
            tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray));
            llQuizContainer.addView(tvEmpty);
            return;
        }

        // ensure quiz title still visible
        tvQuizTitle.setText(lessonQuiz.getTitle() != null && !lessonQuiz.getTitle().isEmpty() ? lessonQuiz.getTitle() : "Quiz");
        tvQuizTitle.setTextSize(17f);
        tvQuizTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvQuizTitle.setTextColor(getResources().getColor(android.R.color.black));
        tvQuizTitle.setPadding(12, 6, 12, 10);

        List<QuizQuestion> qs = lessonQuiz.getQuestions();
        if (qs == null || qs.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Quiz chưa có câu hỏi");
            tvEmpty.setPadding(12, 12, 12, 12);
            tvEmpty.setTextSize(14f);
            tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray));
            llQuizContainer.addView(tvEmpty);
            return;
        }

        int qIndex = 1;
        for (QuizQuestion q : qs) {
            // Create a MaterialCardView for each question (white background)
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(12, 8, 12, 8);
            card.setLayoutParams(cardLp);
            card.setRadius(12f);
            card.setCardElevation(6f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            // inner vertical layout
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(18, 14, 18, 14);

            // Question text — make it bold & dark so it won't be overshadowed
            TextView tvQ = new TextView(this);
            tvQ.setText(qIndex + ". " + (q.getQuestion() != null ? q.getQuestion() : ""));
            tvQ.setTextSize(16f);
            tvQ.setPadding(6, 6, 6, 10);
            tvQ.setTypeface(null, android.graphics.Typeface.BOLD);
            tvQ.setTextColor(getResources().getColor(android.R.color.black));
            inner.addView(tvQ);

            // Options container
            LinearLayout optionsContainer = new LinearLayout(this);
            optionsContainer.setOrientation(LinearLayout.VERTICAL);
            optionsContainer.setPadding(0, 0, 0, 0);

            List<String> opts = q.getOptions();
            int correctIdx = q.getCorrectOptionIndex();

            if (opts != null) {
                for (int i = 0; i < opts.size(); i++) {
                    String opt = opts.get(i);

                    // Wrap each option in a horizontal layout so we can show left accent in background drawable
                    LinearLayout optWrapper = new LinearLayout(this);
                    LinearLayout.LayoutParams wrapLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    wrapLp.setMargins(6, 6, 6, 6);
                    optWrapper.setLayoutParams(wrapLp);
                    optWrapper.setOrientation(LinearLayout.HORIZONTAL);
                    optWrapper.setPadding(0,0,0,0);

                    TextView tvOpt = new TextView(this);
                    tvOpt.setTextSize(14f);
                    tvOpt.setPadding(14, 12, 14, 12);

                    if (i == correctIdx) {
                        // Mark correct option: use special drawable with left-accent + rounded corners
                        tvOpt.setText("✓ " + opt);
                        tvOpt.setTypeface(null, android.graphics.Typeface.BOLD);
                        // apply the correct-bg drawable (which includes light green background + left accent)
                        tvOpt.setBackgroundResource(R.drawable.bg_option_correct);
                        // ensure text color contrasts
                        tvOpt.setTextColor(getResources().getColor(android.R.color.black));
                    } else {
                        tvOpt.setText("• " + opt);
                        // use transparent rounded background (existing drawable)
                        tvOpt.setBackgroundResource(R.drawable.bg_option_transparent);
                        tvOpt.setTextColor(getResources().getColor(android.R.color.black));
                    }

                    // Make tvOpt fill width so the background stretches nicely
                    LinearLayout.LayoutParams optLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tvOpt.setLayoutParams(optLp);

                    optWrapper.addView(tvOpt);
                    optionsContainer.addView(optWrapper);
                }
            } else {
                TextView tvNoOpt = new TextView(this);
                tvNoOpt.setText("Không có đáp án");
                tvNoOpt.setPadding(6, 6, 6, 6);
                optionsContainer.addView(tvNoOpt);
            }

            inner.addView(optionsContainer);
            card.addView(inner);
            llQuizContainer.addView(card);

            qIndex++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release YouTube player
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }

        // Remove listener
        if (commentApi != null && commentUpdateListener != null) {
            commentApi.removeLessonCommentUpdateListener(commentUpdateListener);
        }

    }
}
