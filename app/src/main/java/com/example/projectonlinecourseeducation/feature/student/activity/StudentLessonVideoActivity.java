package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonProgressApi;
import com.google.android.material.button.MaterialButton;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.List;

/**
 * Activity hiển thị video bài học
 * Bao gồm: YouTube player, tiêu đề, mô tả bài học, tracking progress
 * Tracking sẽ cập nhật: currentSecond, totalSecond, completionPercentage
 */
public class StudentLessonVideoActivity extends AppCompatActivity {

    private static final float MIN_UPDATE_INTERVAL_SEC = 5f; // khoảng thời gian tối thiểu giữa 2 lần update progress

    private ImageButton btnBack;
    private TextView tvLessonTitle, tvLessonDescription, tvProgressPercentage;
    private YouTubePlayerView youTubePlayerView;
    // NEW: nút chuyển bài tiếp theo
    private MaterialButton btnNextLesson;

    private String lessonId;
    private String courseId;
    private Lesson lesson;
    private Lesson nextLesson;          // bài tiếp theo (nếu có)

    private LessonApi lessonApi;
    private LessonProgressApi lessonProgressApi;

    // NEW: biến phục vụ tracking
    private float videoDurationSeconds = 0f;
    private float lastSavedSecond = 0f;
    private float startSecond = 0f;     // vị trí bắt đầu (resume từ progress cũ nếu có)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_lesson_video);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        bindViews();

        // Lấy API từ ApiProvider
        lessonApi = ApiProvider.getLessonApi();
        lessonProgressApi = ApiProvider.getLessonProgressApi();

        // Lấy dữ liệu từ Intent
        lessonId = getIntent().getStringExtra("lesson_id");

        if (lessonId == null) {
            lessonId = "c1_l1";
        }

        // Load dữ liệu bài học
        loadLessonData(lessonId);

        // Setup Actions
        setupActions();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonDescription = findViewById(R.id.tvLessonDescription);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        btnNextLesson = findViewById(R.id.btnNextLesson);
    }

    /**
     * Load dữ liệu bài học từ API
     */
    private void loadLessonData(String id) {
        lesson = lessonApi.getLessonDetail(id);

        if (lesson == null) {
            Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lưu courseId để dùng khi tracking / tìm bài tiếp theo
        courseId = lesson.getCourseId();

        // Bind UI
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonDescription.setText(lesson.getDescription());

        // Lấy progress hiện tại (nếu có) để:
        //  - hiển thị %
        //  - resume lại vị trí đã xem dở
        LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId);
        if (progress != null) {
            startSecond = progress.getCurrentSecond();
            updateProgressUI(progress);
        } else {
            startSecond = 0f;
            tvProgressPercentage.setText("0%");
        }

        // Chuẩn bị thông tin bài tiếp theo (nếu có)
        prepareNextLesson();

        // Setup YouTube Player (sau khi có startSecond)
        setupYouTubePlayer();
    }

    /**
     * Tìm bài học tiếp theo trong cùng khóa học dựa trên order
     * Nếu không có bài tiếp theo -> ẩn / disable nút Next.
     */
    private void prepareNextLesson() {
        nextLesson = null;

        List<Lesson> lessonsInCourse = lessonApi.getLessonsForCourse(courseId);
        if (lessonsInCourse == null || lessonsInCourse.isEmpty()) {
            btnNextLesson.setEnabled(false);
            btnNextLesson.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bgnav)
            );
            return;
        }

        int currentOrder = lesson.getOrder();
        Lesson candidate = null;

        for (Lesson l : lessonsInCourse) {
            if (l.getOrder() > currentOrder) {
                if (candidate == null || l.getOrder() < candidate.getOrder()) {
                    candidate = l;
                }
            }
        }

        nextLesson = candidate;

        if (nextLesson == null) {
            // Không có bài tiếp theo
            btnNextLesson.setText("Đây là bài cuối trong khóa");
            btnNextLesson.setEnabled(false);
            btnNextLesson.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bgnav)
            );
        } else {
            // Có bài tiếp theo nhưng sẽ được mở/khóa dựa trên progress hiện tại
            btnNextLesson.setText("Bài tiếp theo: " + nextLesson.getTitle());
            btnNextLesson.setEnabled(false);
            btnNextLesson.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bgnav)
            );
        }
    }

    /**
     * Setup YouTube Player với tracking progress thực tế
     */
    private void setupYouTubePlayer() {
        // Để thư viện tự handle pause/resume/destroy theo lifecycle của Activity
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // Load video từ lesson.getVideoUrl()
                youTubePlayer.loadVideo(lesson.getVideoUrl(), startSecond);
            }

            /**
             * TRACKING PROGRESS: được gọi liên tục khi video đang chạy
             * Cập nhật currentSecond (vị trí hiện tại của video)
             */
            @Override
            public void onCurrentSecond(YouTubePlayer youTubePlayer, float second) {
                super.onCurrentSecond(youTubePlayer, second);

                if (lesson == null || videoDurationSeconds <= 0f) return;

                // Chỉ update khi nhảy ít nhất MIN_UPDATE_INTERVAL_SEC giây để tránh spam
                if (second - lastSavedSecond >= MIN_UPDATE_INTERVAL_SEC) {
                    lastSavedSecond = second;

                    lessonProgressApi.updateLessonProgress(
                            lessonId,
                            second,
                            videoDurationSeconds
                    );

                    LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId);
                    updateProgressUI(progress);
                    updateNextButtonState(progress);
                }
            }

            /**
             * TRACKING: tổng thời lượng video (được gọi 1 lần khi video ready)
             * Dùng để tính phần trăm hoàn thành
             */
            @Override
            public void onVideoDuration(YouTubePlayer youTubePlayer, float duration) {
                super.onVideoDuration(youTubePlayer, duration);

                videoDurationSeconds = duration;

                // Cập nhật tổng thời lượng vào progress (nếu chưa có)
                lessonProgressApi.updateLessonProgress(
                        lessonId,
                        startSecond,
                        videoDurationSeconds
                );

                LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId);
                updateProgressUI(progress);
                updateNextButtonState(progress);
            }

            /**
             * Khi video kết thúc (hoặc người dùng skip đến cuối)
             */
            @Override
            public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
                super.onStateChange(youTubePlayer, state);

                if (state == PlayerConstants.PlayerState.ENDED) {
                    // Đánh dấu bài học là hoàn thành
                    if (lesson != null) {
                        lessonProgressApi.markLessonAsCompleted(lessonId);
                        LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId);
                        updateProgressUI(progress);
                        updateNextButtonState(progress);

                        Toast.makeText(StudentLessonVideoActivity.this,
                                "Bài học hoàn thành!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Cập nhật UI để hiển thị tiến độ hiện tại
     */
    private void updateProgressUI(LessonProgress progress) {
        if (progress == null) {
            tvProgressPercentage.setText("0%");
        } else {
            tvProgressPercentage.setText(progress.getCompletionPercentage() + "%");
        }
    }

    /**
     * Bật/tắt nút Next dựa trên rule:
     *  - Được phép Next nếu completion >= 90% hoặc isCompleted = true
     */
    private void updateNextButtonState(LessonProgress progress) {
        if (nextLesson == null) {
            // Không có bài tiếp theo
            btnNextLesson.setEnabled(false);
            btnNextLesson.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bgnav)
            );
            return;
        }

        boolean canGoNext = false;
        if (progress != null) {
            canGoNext = progress.isCompleted() || progress.getCompletionPercentage() >= 90;
        }

        btnNextLesson.setEnabled(canGoNext);
        btnNextLesson.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this,
                        canGoNext ? R.color.colorSecondary : R.color.bgnav
                )
        );
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        // Nút chuyển bài tiếp theo
        btnNextLesson.setOnClickListener(v -> {
            LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId);
            boolean canGoNext = progress != null &&
                    (progress.isCompleted() || progress.getCompletionPercentage() >= 90);

            if (!canGoNext) {
                Toast.makeText(this,
                        "Bạn cần xem ít nhất 90% thời lượng video trước khi chuyển bài.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (nextLesson == null) {
                Toast.makeText(this,
                        "Đây là bài cuối cùng trong khóa học.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, StudentLessonVideoActivity.class);
            intent.putExtra("lesson_id", nextLesson.getId());
            // courseId sẽ được load từ Lesson của bài mới
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }
}
