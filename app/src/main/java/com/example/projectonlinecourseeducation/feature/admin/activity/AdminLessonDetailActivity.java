package com.example.projectonlinecourseeducation.feature.admin.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminLessonCommentAdapter;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Admin Lesson Detail Activity
 *
 * ADMIN ROLE - READ-ONLY với DELETE POWER:
 * - ✅ Xem video YouTube
 * - ✅ Xem mô tả bài học
 * - ✅ Xem comments của students
 * - ✅ Xem replies của teacher
 * - ✅ Xóa comments (bất kỳ)
 * - ✅ Xóa replies (bất kỳ)
 * - ❌ KHÔNG post comments
 * - ❌ KHÔNG reply comments
 *
 * Admin = Supervisor/Moderator: Giám sát và kiểm duyệt nội dung
 */
public class AdminLessonDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminLessonDetail";

    // Data
    private String lessonId;
    private Lesson lesson;
    private List<LessonComment> comments = new ArrayList<>();

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

    // Adapters
    private AdminLessonCommentAdapter commentAdapter;

    // APIs
    private LessonApi lessonApi;
    private LessonCommentApi commentApi;

    // Listeners
    private LessonCommentApi.LessonCommentUpdateListener commentUpdateListener;

    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

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

        // ẨN comment input - Admin chỉ XEM và XÓA, không post comment
        if (layoutCommentInput != null) {
            layoutCommentInput.setVisibility(View.GONE);
        }

        // Add YouTube player to lifecycle
        getLifecycle().addObserver(youTubePlayerView);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Admin KHÔNG có comment input, nên không cần setup listener
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

        bgExecutor.execute(() -> {
            try {
                Lesson l = lessonApi.getLessonDetail(lessonId);

                if (l == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                final Lesson finalLesson = l;
                runOnUiThread(() -> {
                    lesson = finalLesson;
                    displayLessonInfo();
                    loadYouTubeVideo();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching lesson detail", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
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

        // Nếu chỉ là video ID (11 ký tự)
        if (videoUrl.length() == 11 && !videoUrl.contains("/") && !videoUrl.contains(".")) {
            return videoUrl;
        }

        // Extract từ URL
        // https://www.youtube.com/watch?v=VIDEO_ID
        // https://youtu.be/VIDEO_ID
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

        // Fallback: coi như là video ID
        return videoUrl;
    }

    private void fetchComments() {
        commentApi = ApiProvider.getLessonCommentApi();
        if (commentApi == null) return;

        bgExecutor.execute(() -> {
            try {
                List<LessonComment> fetchedComments = commentApi.getCommentsForLesson(lessonId);
                if (fetchedComments == null) fetchedComments = new ArrayList<>();

                final List<LessonComment> finalComments = fetchedComments;
                runOnUiThread(() -> {
                    comments = finalComments;
                    commentAdapter.setComments(comments);
                    updateCommentCount();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching comments", e);
            }
        });
    }

    private void updateCommentCount() {
        int count = comments.size();
        tvCommentCount.setText(count + " bình luận");
    }

    private void deleteReply(LessonComment comment) {
        if (commentApi == null) return;

        bgExecutor.execute(() -> {
            try {
                LessonComment updated = commentApi.deleteReply(comment.getId());

                if (updated != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xóa câu trả lời", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi xóa câu trả lời", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting reply", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
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

        bgExecutor.execute(() -> {
            try {
                // Admin soft delete (mark as deleted)
                LessonComment updated = commentApi.markCommentAsDeleted(comment.getId());

                if (updated != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi xóa bình luận", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting comment", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void registerCommentUpdateListener() {
        commentApi = ApiProvider.getLessonCommentApi();
        if (commentApi == null) return;

        commentUpdateListener = () -> runOnUiThread(() -> fetchComments());
        commentApi.addLessonCommentUpdateListener(commentUpdateListener);
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

        // Shutdown executor
        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}