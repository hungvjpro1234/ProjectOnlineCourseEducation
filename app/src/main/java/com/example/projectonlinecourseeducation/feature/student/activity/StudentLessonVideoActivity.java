package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Lesson;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonProgressApi;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

/**
 * Activity hiển thị video bài học
 * Bao gồm: YouTube player, tiêu đề, mô tả bài học, tracking progress
 * Tracking sẽ cập nhật: currentSecond, totalSecond, completionPercentage
 */
public class StudentLessonVideoActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvLessonTitle, tvLessonDescription, tvProgressPercentage;
    private YouTubePlayerView youTubePlayerView;
    private String lessonId;
    private String courseId;
    private Lesson lesson;
    private LessonApi lessonApi;
    private LessonProgressApi lessonProgressApi;

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

        // Lưu courseId để dùng khi tracking
        courseId = lesson.getCourseId();

        // Bind UI
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonDescription.setText(lesson.getDescription());

        // Hiển thị progress từ API
        updateProgressUI();

        // Setup YouTube Player
        setupYouTubePlayer();
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
                youTubePlayer.loadVideo(lesson.getVideoUrl(), 0f);
            }

            /**
             * TRACKING PROGRESS: được gọi liên tục khi video đang chạy
             * Cập nhật currentSecond (vị trí hiện tại của video)
             */
            @Override
            public void onCurrentSecond(YouTubePlayer youTubePlayer, float second) {
                super.onCurrentSecond(youTubePlayer, second);

                // Cập nhật progress vào API
                // Lưu ý: VideoPlayerView tự cung cấp tổng thời lượng, không cần từ duration string
                if (lesson != null) {
                    // Gọi API để lưu progress (thường 1-2 giây gọi 1 lần)
                    // lessonProgressApi.updateLessonProgress(lessonId, second, videoDuration);
                    // updateProgressUI();
                }
            }

            /**
             * TRACKING: tổng thời lượng video (được gọi 1 lần khi video ready)
             * Dùng để tính phần trăm hoàn thành
             */
            @Override
            public void onVideoDuration(YouTubePlayer youTubePlayer, float duration) {
                super.onVideoDuration(youTubePlayer, duration);

                // Duration này là thời gian thực từ video YouTube
                // Nên cập nhật progress với duration thực tế này
                if (lesson != null) {
                    // lessonProgressApi.updateLessonProgress(lessonId, 0, duration);
                }
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
                        Toast.makeText(StudentLessonVideoActivity.this,
                                "Bài học hoàn thành!",
                                Toast.LENGTH_SHORT).show();
                        updateProgressUI();
                    }
                }
            }
        });
    }

    /**
     * Cập nhật UI để hiển thị tiến độ hiện tại
     */
    private void updateProgressUI() {
        // Lấy progress hiện tại từ API
        // var progress = lessonProgressApi.getLessonProgress(lessonId);
        // tvProgressPercentage.setText(progress.getCompletionPercentage() + "%");
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }
}