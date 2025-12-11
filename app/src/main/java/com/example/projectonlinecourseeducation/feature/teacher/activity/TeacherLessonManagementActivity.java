package com.example.projectonlinecourseeducation.feature.teacher.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.core.utils.YouTubeUtils; // dùng để extract videoId
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementLessonCommentAdapter;
import com.example.projectonlinecourseeducation.feature.teacher.adapter.ManagementLessonQuizAdapter;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity quản lý chi tiết một bài giảng
 * Hiển thị:
 * - Video preview + thông tin
 * - Thông tin bài học (tiêu đề, mô tả)
 * - Danh sách bình luận + phản hồi từ teacher
 *
 * SỬA: Đã gỡ nút đổi video và nút chỉnh sửa thông tin.
 */
public class TeacherLessonManagementActivity extends AppCompatActivity {

    public static final String EXTRA_LESSON_ID = "lesson_id";
    public static final String EXTRA_COURSE_ID = "course_id";

    private String lessonId;
    private String courseId;
    private Lesson lesson;

    // APIs
    private LessonApi lessonApi;
    private LessonCommentApi lessonCommentApi;
    private LessonQuizApi lessonQuizApi;

    // Header views
    private ImageButton btnBack;
    private TextView tvLessonTitle;

    // Video Section
    private ImageView imgVideoThumbnail;
    private ImageButton btnPlayVideo;
    private TextView tvDuration;
    private TextView tvVideoUrl;

    // YouTube Player
    private YouTubePlayerView youTubePlayerView;
    // keep reference to current listener so we can remove it properly
    private AbstractYouTubePlayerListener currentYouTubeListener;

    // Keep reference to the actual YouTubePlayer instance so we can call loadVideo()
    private YouTubePlayer youTubePlayerInstance = null;
    // If user pressed Play before player ready, keep pending id
    private String pendingPlayVideoId = null;

    // Lesson Info Section
    private TextView tvLessonName;
    private TextView tvDescription;

    // Quiz Section views
    private RecyclerView rvQuiz;
    private ManagementLessonQuizAdapter quizAdapter;
    private ImageView imgQuizExpand;
    private boolean isQuizExpanded = true;
    private TextView tvQuizTitle;
    private Quiz currentQuiz;

    // Comments Section
    private RecyclerView rvComments;
    private ManagementLessonCommentAdapter commentAdapter;
    private ImageView imgCommentExpand;
    private boolean isCommentsExpanded = true;

    // Listeners
    private LessonApi.LessonUpdateListener lessonUpdateListener;
    private LessonCommentApi.LessonCommentUpdateListener commentUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_lesson_management);

        // Initialize APIs
        lessonApi = ApiProvider.getLessonApi();
        lessonCommentApi = ApiProvider.getLessonCommentApi();
        lessonQuizApi = ApiProvider.getLessonQuizApi();

        // Get lesson ID from intent
        lessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);
        courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);

        initViews();
        setupListeners();
        loadLessonFromApi();
        setupCommentAdapter();
        loadCommentsFromApi();
        setupQuizAdapter();
        loadQuizFromApi();
        registerApiListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterApiListeners();

        // Remove and release YouTube player listener + view
        try {
            if (youTubePlayerView != null && currentYouTubeListener != null) {
                youTubePlayerView.removeYouTubePlayerListener(currentYouTubeListener);
                currentYouTubeListener = null;
            }
            // release view (library manages resources)
            if (youTubePlayerView != null) {
                youTubePlayerView.release();
            }
            youTubePlayerInstance = null;
            pendingPlayVideoId = null;
        } catch (Exception ignored) {}
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);

        // Video Section
        imgVideoThumbnail = findViewById(R.id.imgVideoThumbnail);
        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        tvDuration = findViewById(R.id.tvDuration);
        tvVideoUrl = findViewById(R.id.tvVideoUrl);

        // YouTube player view (thêm vào layout activity_teacher_lesson_management)
        youTubePlayerView = findViewById(R.id.youtubePlayerView);

        // Lesson Info Section
        tvLessonName = findViewById(R.id.tvLessonName);
        tvDescription = findViewById(R.id.tvDescription);

        // Quiz Section
        rvQuiz = findViewById(R.id.rvQuiz);
        imgQuizExpand = findViewById(R.id.imgQuizExpand);
        tvQuizTitle = findViewById(R.id.tvQuizTitle);

        // Comments Section
        rvComments = findViewById(R.id.rvComments);
        imgCommentExpand = findViewById(R.id.imgCommentExpand);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlayVideo.setOnClickListener(v -> {
            if (lesson == null) return;
            String videoId = YouTubeUtils.extractVideoId(lesson.getVideoUrl());
            if (videoId == null || videoId.isEmpty()) {
                Toast.makeText(this, "Video không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // If player instance ready -> load and autoplay
            if (youTubePlayerInstance != null) {
                youTubePlayerInstance.loadVideo(videoId, 0f); // autoplay
                imgVideoThumbnail.setVisibility(View.GONE);
                youTubePlayerView.setVisibility(View.VISIBLE);
                btnPlayVideo.setVisibility(View.GONE);
            } else {
                // Player not ready yet: set pending id and show player view.
                pendingPlayVideoId = videoId;
                youTubePlayerView.setVisibility(View.VISIBLE);
                imgVideoThumbnail.setVisibility(View.GONE);
                btnPlayVideo.setVisibility(View.GONE);
                Toast.makeText(this, "Đang tải player, sẽ tự động phát khi sẵn sàng...", Toast.LENGTH_SHORT).show();
            }
        });

        imgCommentExpand.setOnClickListener(v -> toggleCommentsSection());
    }

    private void loadLessonFromApi() {
        if (lessonId == null || courseId == null) {
            Toast.makeText(this, "Thiếu thông tin bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load lesson from API
        Lesson loaded = lessonApi.getLessonDetail(lessonId);
        if (loaded == null) {
            // fallback: try to search in getLessonsForCourse
            List<Lesson> lessons = lessonApi.getLessonsForCourse(courseId);
            for (Lesson l : lessons) {
                if (l.getId().equals(lessonId)) {
                    loaded = l;
                    break;
                }
            }
        }

        if (loaded == null) {
            Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lesson = loaded;
        displayLessonData();
    }

    private void displayLessonData() {
        if (lesson == null) return;

        runOnUiThread(() -> {
            tvLessonTitle.setText(lesson.getTitle());
            tvDuration.setText(lesson.getDuration() != null ? lesson.getDuration() : "Đang tính...");
            tvVideoUrl.setText(lesson.getVideoUrl());
            tvLessonName.setText(lesson.getTitle());
            tvDescription.setText(lesson.getDescription());

            // Preview thumbnail if videoId-like
            String videoId = YouTubeUtils.extractVideoId(lesson.getVideoUrl());
            if (videoId != null && !videoId.isEmpty()) {
                // YouTube thumbnail URL pattern
                String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                ImageLoader.getInstance().display(thumbUrl, imgVideoThumbnail, R.drawable.ic_image_placeholder);
            } else {
                // fallback: placeholder
                ImageLoader.getInstance().display(null, imgVideoThumbnail, R.drawable.ic_image_placeholder);
            }

            // Setup YouTube player to load this video's id (if present)
            setupYouTubePlayerForLesson();
        });
    }

    private void setupCommentAdapter() {
        commentAdapter = new ManagementLessonCommentAdapter(new ManagementLessonCommentAdapter.OnCommentActionListener() {
            @Override
            public void onReplyClick(LessonComment comment) {
                showReplyDialog(comment);
            }

            @Override
            public void onDeleteClick(LessonComment comment) {
                showDeleteCommentDialog(comment);
            }

            @Override
            public void onDeleteReplyClick(LessonComment comment) {
                showDeleteReplyDialog(comment);
            }
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void loadCommentsFromApi() {
        if (lessonId == null) return;

        List<LessonComment> comments = lessonCommentApi.getCommentsForLesson(lessonId);
        runOnUiThread(() -> commentAdapter.setComments(comments));
    }

    private void setupQuizAdapter() {
        quizAdapter = new ManagementLessonQuizAdapter(new ArrayList<>());
        rvQuiz.setLayoutManager(new LinearLayoutManager(this));
        rvQuiz.setAdapter(quizAdapter);

        // Setup expand/collapse for quiz section
        if (imgQuizExpand != null) {
            imgQuizExpand.setOnClickListener(v -> toggleExpandable(rvQuiz, imgQuizExpand));
        }
    }

    private void loadQuizFromApi() {
        if (lessonId == null) {
            // hide quiz block
            runOnUiThread(() -> {
                if (tvQuizTitle != null) tvQuizTitle.setText("Quiz");
                if (quizAdapter != null) quizAdapter.updateQuestions(new ArrayList<>());
                if (rvQuiz != null) rvQuiz.setVisibility(View.GONE);
                if (imgQuizExpand != null) imgQuizExpand.setVisibility(View.GONE);
            });
            return;
        }

        Quiz quiz = null;
        try {
            quiz = lessonQuizApi != null ? lessonQuizApi.getQuizForLesson(lessonId) : null;
        } catch (Exception ignored) {}

        final Quiz finalQuiz = quiz;
        runOnUiThread(() -> {
            currentQuiz = finalQuiz;
            if (finalQuiz == null || finalQuiz.getQuestions() == null || finalQuiz.getQuestions().isEmpty()) {
                // Show placeholder: no quiz
                if (tvQuizTitle != null) tvQuizTitle.setText("Quiz (chưa có)");
                if (quizAdapter != null) quizAdapter.updateQuestions(new ArrayList<>());
                // still show header with collapsed arrow but keep RecyclerView gone
                if (rvQuiz != null) rvQuiz.setVisibility(View.GONE);
                if (imgQuizExpand != null) imgQuizExpand.setVisibility(View.GONE);
            } else {
                // Show quiz
                int count = finalQuiz.getQuestions().size();
                if (tvQuizTitle != null) tvQuizTitle.setText("Quiz (" + count + " câu)");
                if (quizAdapter != null) quizAdapter.updateQuestions(finalQuiz.getQuestions());
                if (rvQuiz != null) rvQuiz.setVisibility(isQuizExpanded ? View.VISIBLE : View.GONE);
                if (imgQuizExpand != null) imgQuizExpand.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Toggle expand/collapse for a RecyclerView section
     */
    private void toggleExpandable(RecyclerView recyclerView, ImageView expandIcon) {
        if (recyclerView == null || expandIcon == null) return;

        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            expandIcon.setRotation(0);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            expandIcon.setRotation(180);
        }
    }

    // ========== DIALOG HANDLERS ==========

    /**
     * Dialog để teacher trả lời comment
     *
     * (không thay đổi: gửi reply ngay khi bấm Gửi)
     */
    private void showReplyDialog(LessonComment comment) {
        if (comment == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trả lời bình luận của " + comment.getUserName());

        final EditText input = new EditText(this);
        input.setHint("Nhập nội dung trả lời");
        input.setMinLines(2);

        // If already has reply, show it
        if (comment.hasTeacherReply()) {
            input.setText(comment.getTeacherReplyContent());
        }

        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String replyContent = input.getText().toString().trim();
            if (replyContent.isEmpty()) {
                Toast.makeText(this, "Nội dung trả lời không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get teacher name
            User currentUser = SessionManager.getInstance(this).getCurrentUser();
            String teacherName = currentUser != null ? currentUser.getName() : "Teacher";

            // Add reply via API
            LessonComment updated = lessonCommentApi.addReply(comment.getId(), teacherName, replyContent);

            if (updated != null) {
                DialogConfirmHelper.showSuccessDialog(
                        this,
                        "Thành công",
                        "Đã trả lời bình luận",
                        R.drawable.ic_check_success,
                        "Đóng",
                        () -> loadCommentsFromApi()
                );
            } else {
                Toast.makeText(this, "Lỗi khi trả lời bình luận", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Dialog xác nhận xóa comment -> sử dụng DialogConfirmHelper
     * Sau khi xóa thành công: hiện Toast và reload comments (KHÔNG show success dialog).
     */
    private void showDeleteCommentDialog(LessonComment comment) {
        if (comment == null) return;

        DialogConfirmHelper.showConfirmDialog(
                this,
                "Xóa bình luận",
                "Bạn chắc chắn muốn xóa bình luận này? Bình luận sẽ được đánh dấu là '[Bình luận đã bị xóa]'.",
                R.drawable.delete_check,
                "Xóa",
                "Hủy",
                R.color.blue_700,
                () -> {
                    LessonComment updated = lessonCommentApi.markCommentAsDeleted(comment.getId());

                    if (updated != null) {
                        Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                        loadCommentsFromApi();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa bình luận", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Dialog xác nhận xóa reply của teacher -> sử dụng DialogConfirmHelper
     * Sau khi xóa thành công: hiện Toast và reload comments (KHÔNG show success dialog).
     */
    private void showDeleteReplyDialog(LessonComment comment) {
        if (comment == null) return;

        DialogConfirmHelper.showConfirmDialog(
                this,
                "Xóa trả lời",
                "Bạn chắc chắn muốn xóa câu trả lời của bạn?",
                R.drawable.delete_check,
                "Xóa",
                "Hủy",
                R.color.blue_700,
                () -> {
                    LessonComment updated = lessonCommentApi.deleteReply(comment.getId());

                    if (updated != null) {
                        Toast.makeText(this, "Đã xóa trả lời", Toast.LENGTH_SHORT).show();
                        loadCommentsFromApi();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa trả lời", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Toggle expand/collapse comments section with rotation animation
     */
    private void toggleCommentsSection() {
        isCommentsExpanded = !isCommentsExpanded;

        if (isCommentsExpanded) {
            rvComments.setVisibility(View.VISIBLE);
            imgCommentExpand.setRotation(0);
        } else {
            rvComments.setVisibility(View.GONE);
            imgCommentExpand.setRotation(180);
        }
    }

    // ========== API LISTENERS ==========

    private void registerApiListeners() {
        // Lesson update listener
        lessonUpdateListener = new LessonApi.LessonUpdateListener() {
            @Override
            public void onLessonUpdated(String updatedLessonId, Lesson updatedLesson) {
                if (updatedLessonId != null && updatedLessonId.equals(lessonId)) {
                    // Update local lesson reference and UI
                    lesson = updatedLesson;
                    displayLessonData();
                }
            }
        };
        try {
            lessonApi.addLessonUpdateListener(lessonUpdateListener);
        } catch (Throwable ignored) {}

        // Comment update listener
        commentUpdateListener = new LessonCommentApi.LessonCommentUpdateListener() {
            @Override
            public void onCommentsChanged() {
                loadCommentsFromApi();
            }
        };
        try {
            lessonCommentApi.addLessonCommentUpdateListener(commentUpdateListener);
        } catch (Throwable ignored) {}
    }

    private void unregisterApiListeners() {
        if (lessonUpdateListener != null && lessonApi != null) {
            lessonApi.removeLessonUpdateListener(lessonUpdateListener);
        }
        if (commentUpdateListener != null && lessonCommentApi != null) {
            lessonCommentApi.removeLessonCommentUpdateListener(commentUpdateListener);
        }
    }

    // ========== YouTube Player helper ==========

    /**
     * Setup YouTube player to load current lesson's videoId (if present).
     * If videoUrl is not a valid id/url, do nothing (thumbnail still shown).
     */
    private void setupYouTubePlayerForLesson() {
        if (youTubePlayerView == null || lesson == null) return;

        // Register lifecycle observer so view auto-manages lifecycle
        try {
            getLifecycle().addObserver(youTubePlayerView);
        } catch (Exception ignored) {}

        final String videoId = YouTubeUtils.extractVideoId(lesson.getVideoUrl());
        if (videoId == null || videoId.isEmpty()) return;

        // Remove previous listener if present
        try {
            if (currentYouTubeListener != null) {
                youTubePlayerView.removeYouTubePlayerListener(currentYouTubeListener);
                currentYouTubeListener = null;
            }
        } catch (Exception ignored) {}

        // Create and add new listener
        currentYouTubeListener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                try {
                    youTubePlayerInstance = youTubePlayer;

                    if (pendingPlayVideoId != null && !pendingPlayVideoId.isEmpty()) {
                        // If user requested play earlier, load and autoplay
                        youTubePlayer.loadVideo(pendingPlayVideoId, 0f);
                        pendingPlayVideoId = null;
                        // ensure player visible (btnPlayVideo was hidden earlier)
                        youTubePlayerView.setVisibility(View.VISIBLE);
                        imgVideoThumbnail.setVisibility(View.GONE);
                        btnPlayVideo.setVisibility(View.GONE);
                    } else {
                        // cue preview (no autoplay)
                        youTubePlayer.cueVideo(videoId, 0f);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
                // no-op for now in management screen
            }
        };

        youTubePlayerView.addYouTubePlayerListener(currentYouTubeListener);
    }
}
