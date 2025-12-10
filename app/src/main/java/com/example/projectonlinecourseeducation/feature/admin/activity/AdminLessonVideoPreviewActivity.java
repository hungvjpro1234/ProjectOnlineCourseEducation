package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity xem preview video của lesson (cho admin)
 * Sử dụng youtube-android-player library
 */
public class AdminLessonVideoPreviewActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private YouTubePlayerView youTubePlayerView;
    private TextView tvCourseTitle, tvLessonTitle, tvLessonOrder, tvDuration, tvDescription;
    private View loadingIndicator;

    private LessonApi lessonApi;
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    private String lessonId;
    private Lesson lesson;

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

        // Add YouTubePlayerView to lifecycle
        getLifecycle().addObserver(youTubePlayerView);
    }

    private void initApis() {
        lessonApi = ApiProvider.getLessonApi();
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
                youTubePlayer.cueVideo(videoId, 0);
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