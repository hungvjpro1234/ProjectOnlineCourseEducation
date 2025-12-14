package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.feature.admin.model.CourseProgressStats;
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageCourseDetailActivity;

import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách khóa học đã mua của student với progress (admin view)
 *
 * - Khi expand 1 item, sẽ load danh sách lesson cho course và progress per-lesson của student,
 *   sau đó hiển thị nested list chi tiết.
 * - Đã mở rộng nested row để hiển thị quiz summary (correct/total) trong một "box" bên phải.
 */
public class UserStudentPurchasedCourseAdapter extends RecyclerView.Adapter<UserStudentPurchasedCourseAdapter.PurchasedCourseViewHolder> {

    private List<CourseProgressStats> courseList = new ArrayList<>();
    private OnCourseClickListener listener;
    private final String studentId;

    public interface OnCourseClickListener {
        void onViewDetailsClick(CourseProgressStats courseStats);
    }

    /**
     * @param listener click callback
     * @param studentId id của student - cần để gọi LessonProgressApi.getLessonProgress(lessonId, studentId)
     */
    public UserStudentPurchasedCourseAdapter(OnCourseClickListener listener, String studentId) {
        this.listener = listener;
        this.studentId = studentId;
    }

    /**
     * Set danh sách courses đã mua với progress (course-level summary)
     */
    public void setCourses(List<CourseProgressStats> courses) {
        this.courseList = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PurchasedCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_manage_user_student_purchased_course, parent, false);
        return new PurchasedCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasedCourseViewHolder holder, int position) {
        CourseProgressStats stats = courseList.get(position);
        holder.bind(stats, listener, studentId);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class PurchasedCourseViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layoutCollapsed;
        private final LinearLayout layoutExpanded;
        private final ImageView imgExpand;
        private final ImageView imgCourseAvatar;
        private final TextView tvCourseName;
        private final TextView tvPurchaseDate;
        private final ProgressBar progressBar;
        private final TextView tvProgressText;
        private final TextView tvLessonsCompleted;
        private final TextView tvAssignmentsCompleted;
        private final TextView tvScore;
        private final Button btnViewDetails;

        // nested list
        private final RecyclerView rvLessonProgressDetail;
        private final LessonDetailAdapter lessonDetailAdapter;

        private boolean isExpanded = false;

        // bound student id for this viewholder (set when bind)
        private String boundStudentId = null;

        // apis
        private final LessonApi lessonApi = ApiProvider.getLessonApi();
        private final LessonProgressApi lessonProgressApi = ApiProvider.getLessonProgressApi();
        private final LessonQuizApi lessonQuizApi = ApiProvider.getLessonQuizApi();

        public PurchasedCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCollapsed = itemView.findViewById(R.id.layoutCollapsed);
            layoutExpanded = itemView.findViewById(R.id.layoutExpanded);
            imgExpand = itemView.findViewById(R.id.imgExpand);
            imgCourseAvatar = itemView.findViewById(R.id.imgCourseAvatar);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvPurchaseDate = itemView.findViewById(R.id.tvPurchaseDate);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvLessonsCompleted = itemView.findViewById(R.id.tvLessonsCompleted);
            tvAssignmentsCompleted = itemView.findViewById(R.id.tvAssignmentsCompleted);
            tvScore = itemView.findViewById(R.id.tvScore);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);

            rvLessonProgressDetail = itemView.findViewById(R.id.rvLessonProgressDetail);
            if (rvLessonProgressDetail.getLayoutManager() == null) {
                rvLessonProgressDetail.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
            lessonDetailAdapter = new LessonDetailAdapter(new ArrayList<>());
            rvLessonProgressDetail.setAdapter(lessonDetailAdapter);

            setupClickListeners();
        }

        private void setupClickListeners() {
            layoutCollapsed.setOnClickListener(v -> toggleExpand());
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) expand();
            else collapse();
        }

        private void expand() {
            layoutExpanded.setVisibility(View.VISIBLE);
            rotateIcon(0f, 180f);
            // If nested adapter empty -> try load details (we will load only once)
            if (lessonDetailAdapter.getItemCount() == 0) {
                Object tag = itemView.getTag();
                if (tag instanceof CourseProgressStats) {
                    CourseProgressStats stats = (CourseProgressStats) tag;
                    // use boundStudentId saved during bind
                    loadLessonProgressForCourse(stats.getCourse(), boundStudentId);
                }
            }
        }

        private void collapse() {
            layoutExpanded.setVisibility(View.GONE);
            rotateIcon(180f, 0f);
        }

        private void rotateIcon(float fromDegrees, float toDegrees) {
            RotateAnimation animation = new RotateAnimation(
                    fromDegrees,
                    toDegrees,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f
            );
            animation.setDuration(300);
            animation.setFillAfter(true);
            imgExpand.startAnimation(animation);
        }

        /**
         * Bind with stats and studentId (needed to load per-student lesson progress)
         */
        public void bind(CourseProgressStats stats, OnCourseClickListener listener, String studentId) {
            if (stats == null || stats.getCourse() == null) return;

            // store stats as tag so expand() can access
            itemView.setTag(stats);

            // remember studentId for later use in expand/load
            this.boundStudentId = studentId;

            // Course name
            tvCourseName.setText(stats.getCourse().getTitle());

            // Price
            NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvPurchaseDate.setText("Giá: " + currencyFormat.format(stats.getCourse().getPrice()) + " VNĐ");

            // Compute percent from stats (do not rely on a missing setter/getter)
            int total = stats.getTotalLessons();
            int completed = stats.getCompletedLessons();
            int percent = total > 0 ? (completed * 100) / total : 0;
            try { progressBar.setProgress(percent); } catch (Exception ignored) {}

            // Progress text
            String progressText = String.format(Locale.getDefault(),
                    "%d%% hoàn thành (%d/%d bài)",
                    percent,
                    completed,
                    total);
            tvProgressText.setText(progressText);

            // Lessons completed
            tvLessonsCompleted.setText(String.format(Locale.getDefault(),
                    "%d/%d",
                    completed,
                    total));

            // Placeholders
            tvAssignmentsCompleted.setText("N/A");
            tvScore.setText("N/A");

            // Course avatar
            if (stats.getCourse().getImageUrl() != null && !stats.getCourse().getImageUrl().isEmpty()) {
                ImageLoader.getInstance().display(stats.getCourse().getImageUrl(), imgCourseAvatar, R.drawable.ic_image_placeholder);
            } else {
                imgCourseAvatar.setImageResource(R.drawable.ic_image_placeholder);
            }

            // View details button -> open AdminManageCourseDetailActivity
            btnViewDetails.setOnClickListener(v -> {
                // first call callback if provided
                if (listener != null) listener.onViewDetailsClick(stats);

                // then open course detail activity
                try {
                    Context ctx = itemView.getContext();
                    Intent intent = new Intent(ctx, AdminManageCourseDetailActivity.class);
                    if (stats.getCourse() != null) {
                        intent.putExtra("courseId", stats.getCourse().getId());
                        intent.putExtra("courseTitle", stats.getCourse().getTitle());
                    }
                    if (!(ctx instanceof android.app.Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    ctx.startActivity(intent);
                } catch (Exception ignored) {}
            });

            // Preload lesson progress only if adapter already empty and expanded
            if (isExpanded && lessonDetailAdapter.getItemCount() == 0) {
                loadLessonProgressForCourse(stats.getCourse(), boundStudentId);
            }
        }

        /**
         * Load lesson list for the course and per-student progress, on background thread.
         * This method needs studentId to call lessonProgressApi.getLessonProgress(lessonId, studentId).
         *
         * ENHANCEMENT: also reads LessonQuizApi to get latest attempt correctCount and quiz total questions,
         * and passes that to nested row model so the nested item can display "Quiz: correct/total".
         */
        private void loadLessonProgressForCourse(Course course, String studentId) {
            if (course == null) return;
            new Thread(() -> {
                List<Lesson> lessons = null;
                try {
                    if (lessonApi != null) lessons = lessonApi.getLessonsForCourse(course.getId());
                } catch (Exception ignored) {}
                if (lessons == null) lessons = new ArrayList<>();

                List<LessonProgressRow> rows = new ArrayList<>();
                int sumPercent = 0;
                int completedCount = 0;

                for (int i = 0; i < lessons.size(); i++) {
                    Lesson lesson = lessons.get(i);
                    int percent = 0;
                    boolean completed = false;
                    try {
                        if (lessonProgressApi != null && studentId != null) {
                            LessonProgress lp = lessonProgressApi.getLessonProgress(lesson.getId(), studentId);
                            if (lp != null) {
                                try {
                                    percent = lp.getCompletionPercentage();
                                } catch (Exception ignored) {
                                    percent = 0;
                                }
                                completed = lp.isCompleted();
                            }
                        }
                    } catch (Exception ignored) {}

                    // --- NEW: get quiz latest attempt (correct count) and quiz total ---
                    int quizCorrect = 0;
                    int quizTotal = 0; // 0 -> no quiz
                    try {
                        if (lessonQuizApi != null) {
                            Quiz quiz = lessonQuizApi.getQuizForLesson(lesson.getId());
                            if (quiz != null && quiz.getQuestions() != null) {
                                quizTotal = quiz.getQuestions().size();
                            }
                            if (quizTotal > 0 && studentId != null) {
                                List<QuizAttempt> attempts = lessonQuizApi.getAttemptsForLesson(lesson.getId(), studentId);
                                if (attempts != null && !attempts.isEmpty() && attempts.get(0) != null) {
                                    QuizAttempt latest = attempts.get(0);
                                    try {
                                        quizCorrect = latest.getCorrectCount();
                                    } catch (Exception ignored) {
                                        // fallback: try other getter names if model differs
                                        try {
                                            // If the model uses 'getCorrect' or 'getCorrectAnswers'
                                            java.lang.reflect.Method m = latest.getClass().getMethod("getCorrect");
                                            Object v = m.invoke(latest);
                                            if (v instanceof Integer) quizCorrect = (Integer) v;
                                        } catch (Exception e) {
                                            // ignore - keep quizCorrect = 0
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {}

                    rows.add(new LessonProgressRow(i + 1,
                            lesson.getTitle() == null ? ("Bài " + (i + 1)) : lesson.getTitle(),
                            percent,
                            completed,
                            quizCorrect,
                            quizTotal,
                            lesson.getId()
                    ));
                    sumPercent += percent;
                    if (completed) completedCount++;
                }

                final int aggPercent = lessons.size() > 0 ? (sumPercent / lessons.size()) : 0;
                final int totalLessons = lessons.size();
                final int finalCompleted = completedCount;

                // update UI on main thread
                itemView.post(() -> {
                    // update nested adapter
                    lessonDetailAdapter.updateItems(rows);

                    // update progress summary UI
                    try { progressBar.setProgress(aggPercent); } catch (Exception ignored) {}
                    tvProgressText.setText(String.format(Locale.getDefault(), "%d%% hoàn thành (%d/%d bài)", aggPercent, finalCompleted, totalLessons));
                    tvLessonsCompleted.setText(String.format(Locale.getDefault(), "%d/%d", finalCompleted, totalLessons));
                });
            }).start();
        }

        // Model for nested row (extended with quiz info and lessonId)
        private static class LessonProgressRow {
            final int order;
            final String title;
            final int percent;
            final boolean completed;
            final int quizCorrect; // số câu đúng trong latest attempt
            final int quizTotal;   // tổng câu trong quiz (0 means no quiz)
            final String lessonId;

            LessonProgressRow(int order, String title, int percent, boolean completed,
                              int quizCorrect, int quizTotal, String lessonId) {
                this.order = order;
                this.title = title;
                this.percent = percent;
                this.completed = completed;
                this.quizCorrect = quizCorrect;
                this.quizTotal = quizTotal;
                this.lessonId = lessonId;
            }
        }

        // Adapter for nested lesson details
        private static class LessonDetailAdapter extends RecyclerView.Adapter<LessonDetailAdapter.LessonVH> {
            private final List<LessonProgressRow> items;
            LessonDetailAdapter(List<LessonProgressRow> items) {
                this.items = items != null ? items : new ArrayList<>();
            }
            void updateItems(List<LessonProgressRow> newItems) {
                items.clear();
                if (newItems != null) items.addAll(newItems);
                notifyDataSetChanged();
            }
            @NonNull
            @Override
            public LessonVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_lesson_progress, parent, false);
                return new LessonVH(v);
            }
            @Override
            public void onBindViewHolder(@NonNull LessonVH holder, int position) {
                LessonProgressRow r = items.get(position);
                holder.bind(r);
            }
            @Override
            public int getItemCount() { return items.size(); }
            static class LessonVH extends RecyclerView.ViewHolder {
                private final TextView tvLessonTitle, tvLessonPercent, tvLessonCourse, tvLessonQuiz;
                private final ProgressBar pbLesson;
                LessonVH(@NonNull View itemView) {
                    super(itemView);
                    tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
                    tvLessonPercent = itemView.findViewById(R.id.tvLessonPercent);
                    tvLessonCourse = itemView.findViewById(R.id.tvLessonCourse);
                    pbLesson = itemView.findViewById(R.id.pbLesson);
                    tvLessonQuiz = itemView.findViewById(R.id.tvLessonQuiz); // must exist in layout
                }
                void bind(LessonProgressRow r) {
                    tvLessonTitle.setText((r.order > 0 ? r.order + ". " : "") + (r.title != null ? r.title : "Bài"));

                    // Slightly smaller title for compact UI
                    tvLessonTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    tvLessonPercent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                    tvLessonPercent.setText(r.percent + "%");
                    tvLessonCourse.setVisibility(View.GONE); // we don't have course context here
                    try { pbLesson.setMax(100); pbLesson.setProgress(r.percent); } catch (Exception ignored) {}

                    // Show quiz info if quiz exists (quizTotal > 0)
                    if (r.quizTotal > 0) {
                        // display "Quiz: 6/10" where 6 is correct count
                        tvLessonQuiz.setText("Quiz: " + r.quizCorrect + "/" + r.quizTotal);
                        tvLessonQuiz.setVisibility(View.VISIBLE);
                        tvLessonQuiz.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    } else {
                        // no quiz for this lesson -> hide
                        tvLessonQuiz.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
