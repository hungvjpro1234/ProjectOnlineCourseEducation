package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.example.projectonlinecourseeducation.data.notification.NotificationFakeApiService;
import com.example.projectonlinecourseeducation.feature.student.adapter.LessonCommentAdapter;
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
 *
 * CHANGES:
 * - Sử dụng LessonProgressApi.LessonProgressUpdateListener để đồng bộ UI
 *   với các thay đổi tiến độ (từ chính activity hoặc từ nơi khác).
 * - Không cập nhật UI trực tiếp ngay sau updateLessonProgress/markLessonAsCompleted,
 *   thay vào đó listener sẽ đảm nhiệm việc refresh UI.
 *
 * - Thêm integration với LessonQuizApi: nếu lesson có quiz, nút Next chuyển sang "Làm quiz"
 *   và chỉ cho phép làm quiz khi lesson đã completed (theo business rule).
 *
 * - Thêm QuizUpdateListener để tự cập nhật khi Quiz được create/update/delete.
 */
public class StudentLessonVideoActivity extends AppCompatActivity {

    private static final float MIN_UPDATE_INTERVAL_SEC = 5f; // khoảng thời gian tối thiểu giữa 2 lần update progress

    private ImageButton btnBack;
    private TextView tvLessonTitle, tvLessonDescription, tvProgressPercentage;
    private YouTubePlayerView youTubePlayerView;
    // NEW: nút chuyển bài tiếp theo / hoặc làm quiz
    private MaterialButton btnNextLesson;

    // Comment views
    private RecyclerView rvComments;
    private EditText edtCommentInput;
    private ImageButton btnSendComment;
    private TextView tvCommentCount;
    private TextView tvEmptyComments;

    private String lessonId;
    private String courseId;
    private Lesson lesson;
    private Lesson nextLesson;          // bài tiếp theo (nếu có)

    private LessonApi lessonApi;
    private LessonProgressApi lessonProgressApi;
    private LessonCommentApi lessonCommentApi;
    private LessonQuizApi lessonQuizApi; // NEW: quiz API

    // Comment adapter
    private LessonCommentAdapter commentAdapter;

    // NEW: biến phục vụ tracking
    private float videoDurationSeconds = 0f;
    private float lastSavedSecond = 0f;
    private float startSecond = 0f;     // vị trí bắt đầu (resume từ progress cũ nếu có)

    // NEW: listener để nhận thông báo progress thay đổi
    private LessonProgressApi.LessonProgressUpdateListener lessonProgressListener;

    // NEW: quiz listener để reload state khi quiz thay đổi
    private LessonQuizApi.QuizUpdateListener quizUpdateListener;

    // NEW: flag nếu lesson có quiz
    private boolean lessonHasQuiz = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
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
        lessonCommentApi = ApiProvider.getLessonCommentApi();
        lessonQuizApi = ApiProvider.getLessonQuizApi(); // NEW

        // Handle system back (gesture / hardware) using AndroidX OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(StudentLessonVideoActivity.this, StudentCoursePurchasedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Setup comment adapter
        setupCommentSection();

        // Lấy dữ liệu từ Intent
        lessonId = getIntent().getStringExtra("lesson_id");

        if (lessonId == null) {
            lessonId = "c1_l1";
        }

        // Load dữ liệu bài học
        loadLessonData(lessonId);

        // Load bình luận
        loadComments();

        // Setup Actions
        setupActions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Đăng ký listener để nhận notify khi có thay đổi progress (từ activity này hoặc nơi khác)
        if (lessonProgressApi != null && lessonProgressListener == null) {
            lessonProgressListener = new LessonProgressApi.LessonProgressUpdateListener() {
                @Override
                public void onLessonProgressChanged(String changedLessonId) {
                    // Nếu thay đổi liên quan tới lesson hiện tại (hoặc truyền null/empty => global), refresh UI
                    boolean relevant = false;
                    if (changedLessonId == null || changedLessonId.isEmpty()) {
                        relevant = true;
                    } else if (lessonId != null && changedLessonId.equals(lessonId)) {
                        relevant = true;
                    }

                    if (relevant) {
                        runOnUiThread(() -> {
                            // Lấy progress mới (single source of truth) với studentId
                            User currentUser = SessionManager.getInstance(StudentLessonVideoActivity.this).getCurrentUser();
                            String studentId = currentUser != null ? currentUser.getId() : null;
                            LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId, studentId);
                            updateProgressUI(progress);
                            updateNextButtonState(progress);
                        });
                    }
                }
            };

            lessonProgressApi.addLessonProgressUpdateListener(lessonProgressListener);

            // === NEW: initial sync immediately after re-registering listener ===
            // Ensure UI is in sync if progress changed while this Activity was stopped.
            // Call listener with current lessonId to refresh only this lesson's UI.
            lessonProgressListener.onLessonProgressChanged(lessonId);
        }

        // Register quiz update listener to refresh "lessonHasQuiz" and button state when quiz changes
        if (lessonQuizApi != null && quizUpdateListener == null) {
            quizUpdateListener = new LessonQuizApi.QuizUpdateListener() {
                @Override
                public void onQuizChanged(String changedLessonId) {
                    // If change is global (null) or related to this lesson, refresh state
                    boolean relevant = (changedLessonId == null || changedLessonId.isEmpty() || (lessonId != null && lessonId.equals(changedLessonId)));
                    if (!relevant) return;

                    runOnUiThread(() -> {
                        try {
                            lessonHasQuiz = lessonQuizApi.getQuizForLesson(lessonId) != null;
                        } catch (Exception ignored) {
                            lessonHasQuiz = false;
                        }
                        // Re-evaluate button enabled state using current lesson progress
                        User currentUser = SessionManager.getInstance(StudentLessonVideoActivity.this).getCurrentUser();
                        String studentId = currentUser != null ? currentUser.getId() : null;
                        LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId, studentId);
                        updateNextButtonState(progress);
                    });
                }
            };
            lessonQuizApi.addQuizUpdateListener(quizUpdateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký listener để tránh leak
        if (lessonProgressApi != null && lessonProgressListener != null) {
            lessonProgressApi.removeLessonProgressUpdateListener(lessonProgressListener);
            lessonProgressListener = null;
        }

        if (lessonQuizApi != null && quizUpdateListener != null) {
            lessonQuizApi.removeQuizUpdateListener(quizUpdateListener);
            quizUpdateListener = null;
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonDescription = findViewById(R.id.tvLessonDescription);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        btnNextLesson = findViewById(R.id.btnNextLesson);

        // Comment views
        rvComments = findViewById(R.id.rvComments);
        edtCommentInput = findViewById(R.id.edtCommentInput);
        btnSendComment = findViewById(R.id.btnSendComment);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        tvEmptyComments = findViewById(R.id.tvEmptyComments);
    }

    /**
     * Setup RecyclerView và adapter cho phần bình luận
     */
    private void setupCommentSection() {
        commentAdapter = new LessonCommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        // Lấy thông tin người dùng hiện tại
        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        if (currentUser != null) {
            commentAdapter.setCurrentUserId(currentUser.getId());
        }

        // Xử lý sự kiện xóa bình luận
        commentAdapter.setOnCommentActionListener(new LessonCommentAdapter.OnCommentActionListener() {
            @Override
            public void onDeleteComment(LessonComment comment) {
                showDeleteCommentDialog(comment);
            }
        });

        // Xử lý sự kiện gửi bình luận
        btnSendComment.setOnClickListener(v -> sendComment());

        // Back button -> navigate to StudentCoursePurchasedActivity (consistent behavior)
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(StudentLessonVideoActivity.this, StudentCoursePurchasedActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Load dữ liệu bài học từ API
     */
    private void loadLessonData(String id) {
        AsyncApiHelper.execute(
                () -> {
                    Lesson lesson = lessonApi.getLessonDetail(id);
                    if (lesson == null) return null;

                    User user = SessionManager.getInstance(this).getCurrentUser();
                    String studentId = user != null ? user.getId() : null;

                    LessonProgress progress =
                            lessonProgressApi.getLessonProgress(id, studentId);

                    return new Object[]{lesson, progress};
                },
                new AsyncApiHelper.ApiCallback<Object[]>() {
                    @Override
                    public void onSuccess(Object[] result) {
                        if (result == null) {
                            Toast.makeText(StudentLessonVideoActivity.this,
                                    "Không tìm thấy bài học",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        lesson = (Lesson) result[0];
                        LessonProgress progress = (LessonProgress) result[1];

                        courseId = lesson.getCourseId();
                        tvLessonTitle.setText(lesson.getTitle());
                        tvLessonDescription.setText(lesson.getDescription());

                        startSecond = progress != null ? progress.getCurrentSecond() : 0f;
                        updateProgressUI(progress);

                        prepareNextLesson();
                        setupYouTubePlayer();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(StudentLessonVideoActivity.this,
                                "Lỗi tải bài học",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    /**
     * Tìm bài học tiếp theo trong cùng khóa học dựa trên order
     * Nếu không có bài tiếp theo -> ẩn / disable nút Next.
     *
     * UPDATED: nếu lesson có quiz -> nút sẽ là "Làm quiz" và business rule khác.
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

        // Kiểm tra quiz cho lesson hiện tại
        lessonHasQuiz = false;
        try {
            if (lessonQuizApi != null) {
                lessonHasQuiz = lessonQuizApi.getQuizForLesson(lessonId) != null;
            }
        } catch (Exception ignored) {
        }

        if (lessonHasQuiz) {
            // Nếu có quiz, đổi text nút sang làm quiz. Bật/tắt dựa trên lesson completed (updateNextButtonState sẽ handle)
            btnNextLesson.setText("Làm quiz");
            btnNextLesson.setEnabled(false); // enable khi lesson completed (updateNextButtonState sẽ xử lý)
            btnNextLesson.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bgnav));
        } else {
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

                    // Gọi API cập nhật tiến độ với studentId. UI sẽ được cập nhật via listener.
                    User currentUser = SessionManager.getInstance(StudentLessonVideoActivity.this).getCurrentUser();
                    String studentId = currentUser != null ? currentUser.getId() : null;
                    lessonProgressApi.updateLessonProgress(
                            lessonId,
                            second,
                            videoDurationSeconds,
                            studentId
                    );

                    // *** Không gọi getLessonProgress() + updateProgressUI trực tiếp ở đây ***
                    // để tránh duplicate update; listener sẽ nhận notify và cập nhật UI.
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

                // Cập nhật tổng thời lượng vào progress (nếu chưa có) với studentId
                User currentUser = SessionManager.getInstance(StudentLessonVideoActivity.this).getCurrentUser();
                String studentId = currentUser != null ? currentUser.getId() : null;
                lessonProgressApi.updateLessonProgress(
                        lessonId,
                        startSecond,
                        videoDurationSeconds,
                        studentId
                );

                // *** Không gọi getLessonProgress() + updateProgressUI trực tiếp ở đây ***
                // listener sẽ nhận notify và cập nhật UI.
            }

            /**
             * Khi video kết thúc (hoặc người dùng skip đến cuối)
             */
            @Override
            public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
                super.onStateChange(youTubePlayer, state);

                if (state == PlayerConstants.PlayerState.ENDED) {
                    // Đánh dấu bài học là hoàn thành với studentId
                    if (lesson != null) {
                        User currentUser = SessionManager.getInstance(StudentLessonVideoActivity.this).getCurrentUser();
                        String studentId = currentUser != null ? currentUser.getId() : null;
                        lessonProgressApi.markLessonAsCompleted(lessonId, studentId);

                        // *** Không gọi getLessonProgress() ở đây — listener sẽ nhận notify và cập nhật UI. ***

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
     *  - Nếu lesson có quiz: chỉ bật khi lesson.isCompleted() == true (business)
     *  - Nếu không có quiz: Giữ rule cũ: completion >=90% hoặc isCompleted = true
     */
    private void updateNextButtonState(LessonProgress progress) {
        if (nextLesson == null && !lessonHasQuiz) {
            btnNextLesson.setEnabled(false);
            btnNextLesson.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bgnav)
            );
            return;
        }

        boolean canInteract = false;
        if (lessonHasQuiz) {
            // Business: chỉ được làm quiz khi lesson completed
            if (progress != null && progress.isCompleted()) {
                canInteract = true;
            }
        } else {
            // Giữ rule trước: completion>=90% hoặc isCompleted true
            if (progress != null) {
                canInteract = progress.isCompleted() || progress.getCompletionPercentage() >= 90;
            }
        }

        btnNextLesson.setEnabled(canInteract);
        btnNextLesson.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this,
                        canInteract ? R.color.colorSecondary : R.color.bgnav
                )
        );
    }

    private void setupActions() {
        // btnBack handled in setupCommentSection()

        // Nút chuyển bài tiếp theo (hoặc làm quiz)
        btnNextLesson.setOnClickListener(v -> {
            User currentUser = SessionManager.getInstance(this).getCurrentUser();
            String studentId = currentUser != null ? currentUser.getId() : null;
            LessonProgress progress = lessonProgressApi.getLessonProgress(lessonId, studentId);

            boolean canInteract;
            if (lessonHasQuiz) {
                canInteract = progress != null && progress.isCompleted();
            } else {
                canInteract = progress != null && (progress.isCompleted() || progress.getCompletionPercentage() >= 90);
            }

            if (!canInteract) {
                String msg = lessonHasQuiz ? "Bạn cần hoàn thành bài học trước khi làm quiz." :
                        "Bạn cần xem ít nhất 90% thời lượng video trước khi chuyển bài.";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                return;
            }

            if (lessonHasQuiz) {
                // Start Quiz Activity
                Intent intent = new Intent(this, StudentLessonQuizActivity.class);
                intent.putExtra("lesson_id", lessonId);
                if (nextLesson != null) intent.putExtra("next_lesson_id", nextLesson.getId());
                startActivity(intent);
                // don't finish() — user may return to replay video
                return;
            }

            // original next-lesson flow
            if (nextLesson == null) {
                Toast.makeText(this,
                        "Đây là bài cuối cùng trong khóa học.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, StudentLessonVideoActivity.class);
            intent.putExtra("lesson_id", nextLesson.getId());
            startActivity(intent);
            finish();
        });
    }

    /**
     * Load danh sách bình luận từ API
     */
    private void loadComments() {
        AsyncApiHelper.execute(
                () -> lessonCommentApi.getCommentsForLesson(lessonId),
                new AsyncApiHelper.ApiCallback<List<LessonComment>>() {
                    @Override
                    public void onSuccess(List<LessonComment> comments) {
                        commentAdapter.submitList(comments);
                        int count = comments != null ? comments.size() : 0;
                        updateCommentCount(count);

                        tvEmptyComments.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                        rvComments.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(StudentLessonVideoActivity.this,
                                "Không tải được bình luận",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    /**
     * Cập nhật số lượng bình luận hiển thị
     */
    private void updateCommentCount(int count) {
        if (count == 0) {
            tvCommentCount.setText(R.string.no_comments);
        } else if (count == 1) {
            tvCommentCount.setText(R.string.comment_count_one);
        } else {
            tvCommentCount.setText(getString(R.string.comment_count_many, count));
        }
    }

    /**
     * Gửi bình luận mới
     */
    private void sendComment() {
        String content = edtCommentInput.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this,
                    "Vui lòng nhập nội dung bình luận",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập để bình luận",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    return lessonCommentApi.addComment(
                            lessonId,
                            currentUser.getId(),
                            currentUser.getName(),
                            content
                    );
                },
                new AsyncApiHelper.ApiCallback<LessonComment>() {
                    @Override
                    public void onSuccess(LessonComment newComment) {
                        // ===== MAIN THREAD =====
                        if (newComment == null) {
                            Toast.makeText(
                                    StudentLessonVideoActivity.this,
                                    "Không thể gửi bình luận",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        // Thêm comment mới vào đầu list
                        commentAdapter.addComment(newComment);

                        // Clear input
                        edtCommentInput.setText("");

                        // Cập nhật số lượng bình luận
                        int newCount = lessonCommentApi.getCommentCount(lessonId);
                        updateCommentCount(newCount);

                        // Hiển thị danh sách nếu trước đó empty
                        tvEmptyComments.setVisibility(View.GONE);
                        rvComments.setVisibility(View.VISIBLE);

                        // Scroll lên đầu
                        rvComments.smoothScrollToPosition(0);

                        // 🔔 Tạo thông báo cho teacher (không block UI)
                        createNotificationForTeacher(newComment, currentUser);

                        Toast.makeText(
                                StudentLessonVideoActivity.this,
                                "Đã gửi bình luận",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                StudentLessonVideoActivity.this,
                                "Lỗi khi gửi bình luận",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    /**
     * Hiển thị dialog xác nhận xóa bình luận
     */
    private void showDeleteCommentDialog(LessonComment comment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_comment)
                .setMessage(R.string.delete_comment_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteComment(comment))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Xóa bình luận
     */
    private void deleteComment(LessonComment comment) {
        User currentUser = SessionManager.getInstance(this).getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====
                    return lessonCommentApi.deleteComment(
                            comment.getId(),
                            currentUser.getId()
                    );
                },
                new AsyncApiHelper.ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        // ===== MAIN THREAD =====
                        if (success == null || !success) {
                            Toast.makeText(
                                    StudentLessonVideoActivity.this,
                                    "Không thể xóa bình luận",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        // Xóa khỏi adapter
                        commentAdapter.removeComment(comment.getId());

                        // Cập nhật số lượng
                        int newCount = lessonCommentApi.getCommentCount(lessonId);
                        updateCommentCount(newCount);

                        // Hiển thị empty state nếu không còn comment
                        if (newCount == 0) {
                            tvEmptyComments.setVisibility(View.VISIBLE);
                            rvComments.setVisibility(View.GONE);
                        }

                        Toast.makeText(
                                StudentLessonVideoActivity.this,
                                "Đã xóa bình luận",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                StudentLessonVideoActivity.this,
                                "Lỗi khi xóa bình luận",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    /**
     * Tạo thông báo cho teacher khi student comment
     */
    private void createNotificationForTeacher(LessonComment newComment, User student) {
        try {
            // Lấy thông tin course để có course title và teacher name
            Course course = ApiProvider.getCourseApi().getCourseDetail(courseId);
            if (course == null || lesson == null) return;

            // 🔔 Tạo thông báo cho teacher
            // NOTE: Vì Course model không có teacherId, dùng helper method map tên → userId
            // Trong RemoteApiService sẽ cần query từ database để lấy đúng teacherId
            NotificationApi notificationApi = ApiProvider.getNotificationApi();
            String teacherId = ((NotificationFakeApiService) notificationApi)
                    .getTeacherIdByName(course.getTeacher());

            notificationApi.createStudentLessonCommentNotification(
                    teacherId,              // teacherId - map từ teacher name
                    student.getName(),      // tên student
                    lessonId,               // ID bài học
                    lesson.getTitle(),      // tên bài học
                    courseId,               // ID khóa học
                    course.getTitle(),      // tên khóa học
                    newComment.getId()      // ID comment
            );
        } catch (Exception e) {
            // Không crash app nếu tạo notification thất bại
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }
}
