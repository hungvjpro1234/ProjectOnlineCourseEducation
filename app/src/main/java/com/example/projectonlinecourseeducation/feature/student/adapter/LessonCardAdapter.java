package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentLessonQuizActivity;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentLessonVideoActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class LessonCardAdapter extends RecyclerView.Adapter<LessonCardAdapter.VH> {

    public static class LessonItemUiModel {
        public final Lesson lesson;
        public final int completionPercentage;
        public final boolean locked;
        public final boolean hasQuiz;
        public final boolean completed; // video completion flag (LessonProgress.isCompleted())
        public final boolean quizPassed; // whether student already passed quiz for this lesson

        public LessonItemUiModel(Lesson lesson, int completionPercentage, boolean locked, boolean hasQuiz, boolean completed, boolean quizPassed) {
            this.lesson = lesson;
            this.completionPercentage = completionPercentage;
            this.locked = locked;
            this.hasQuiz = hasQuiz;
            this.completed = completed;
            this.quizPassed = quizPassed;
        }

        // backward-compatible constructors
        public LessonItemUiModel(Lesson lesson, int completionPercentage, boolean locked, boolean hasQuiz) {
            this(lesson, completionPercentage, locked, hasQuiz, false, false);
        }

        public LessonItemUiModel(Lesson lesson, int completionPercentage, boolean locked) {
            this(lesson, completionPercentage, locked, false, false, false);
        }
    }

    private final List<LessonItemUiModel> data = new ArrayList<>();
    private final Context context;

    public LessonCardAdapter(Context context) {
        this.context = context;
    }

    public void submitItems(List<LessonItemUiModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_lesson_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LessonItemUiModel item = data.get(position);
        Lesson lesson = item.lesson;

        holder.tvLessonNumber.setText("Bài " + (position + 1));
        holder.tvLessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "(Không có tiêu đề)");
        String duration = lesson.getDuration();
        holder.tvLessonDuration.setText(duration != null && !duration.isEmpty() ? duration : "00:00");

        int[] placeholderDrawables = {
                R.drawable.ic_lesson_placeholder_purple,
                R.drawable.ic_lesson_placeholder_blue,
                R.drawable.ic_lesson_placeholder_cyan
        };
        holder.ivLessonThumbnail.setImageResource(placeholderDrawables[position % placeholderDrawables.length]);

        int percent = Math.max(0, Math.min(100, item.completionPercentage));
        holder.progressBar.setProgress(percent);
        holder.tvLessonProgressPercent.setText(percent + "%");

        // compute nextLessonId
        final String nextLessonId;
        if (position + 1 < data.size() && data.get(position + 1) != null && data.get(position + 1).lesson != null) {
            nextLessonId = data.get(position + 1).lesson.getId();
        } else {
            nextLessonId = null;
        }

        /* =============================== */
        /* ========= STATE COLORS ========= */
        /* =============================== */

        ColorStateList colorGray = ContextCompat.getColorStateList(context, R.color.text_secondary);
        ColorStateList colorEnabled = ContextCompat.getColorStateList(context, R.color.colorSecondary);
        ColorStateList colorPassedBg = ContextCompat.getColorStateList(context, R.color.green_success);
        ColorStateList colorPassedText = ContextCompat.getColorStateList(context, R.color.white);
        ColorStateList colorNormalBg = ContextCompat.getColorStateList(context, R.color.white);

        /* =============================== */
        /* ========== HANDLE LOCKED ====== */
        /* =============================== */

        if (item.locked) {
            holder.itemView.setAlpha(0.6f);

            // Play lesson icon disabled
            holder.ivPlay.setColorFilter(ContextCompat.getColor(context, R.color.divider));
            holder.ivPlay.setOnClickListener(v ->
                    Toast.makeText(context, "Hãy hoàn thành bài học trước đó để mở bài này", Toast.LENGTH_SHORT).show()
            );

            // Quiz button but LOCKED
            holder.btnQuiz.setVisibility(item.hasQuiz ? View.VISIBLE : View.GONE);
            holder.btnQuiz.setText("Làm quiz");
            holder.btnQuiz.setTextColor(colorGray);
            holder.btnQuiz.setBackgroundTintList(colorNormalBg);
            holder.btnQuiz.setAlpha(0.5f);

            holder.btnQuiz.setOnClickListener(v ->
                    Toast.makeText(context, "Hoàn thành bài học trước khi làm quiz", Toast.LENGTH_SHORT).show()
            );

            return;
        }

        /* =============================== */
        /* ===== LESSON UNLOCKED ========= */
        /* =============================== */

        holder.itemView.setAlpha(1f);

        // Play button active
        holder.ivPlay.setColorFilter(ContextCompat.getColor(context, R.color.colorSecondary));
        holder.ivPlay.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentLessonVideoActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("lesson_title", lesson.getTitle());
            intent.putExtra("course_id", lesson.getCourseId());
            if (nextLessonId != null) intent.putExtra("next_lesson_id", nextLessonId);
            context.startActivity(intent);
        });

        /* =============================== */
        /* ====== QUIZ BUTTON STATES ===== */
        /* =============================== */

        if (item.hasQuiz) {
            // ensure visible and consistent
            holder.btnQuiz.setVisibility(View.VISIBLE);
            holder.btnQuiz.setText("Làm quiz");
            holder.btnQuiz.setAllCaps(false);
            holder.btnQuiz.setIcon(null); // remove any icon that may affect layout
            holder.btnQuiz.bringToFront();

            // stroke color (so button outline visible on white background)
            ColorStateList strokeColor = ContextCompat.getColorStateList(context, R.color.divider);
            int strokeWidthPx = (int) (1 * context.getResources().getDisplayMetrics().density); // 1dp

            if (!item.completed) {
                // LOCKED: gray text, subdued background, not clickable
                holder.btnQuiz.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                holder.btnQuiz.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
                holder.btnQuiz.setAlpha(0.6f);
                holder.btnQuiz.setStrokeWidth(strokeWidthPx);
                holder.btnQuiz.setStrokeColor(strokeColor);

                holder.btnQuiz.setOnClickListener(v ->
                        Toast.makeText(context, "Bạn cần hoàn thành video trước khi làm quiz", Toast.LENGTH_SHORT).show()
                );

            } else if (item.quizPassed) {
                // PASSED: green background, white text
                holder.btnQuiz.setAlpha(1f);
                holder.btnQuiz.setStrokeWidth(0);
                holder.btnQuiz.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_success));
                holder.btnQuiz.setTextColor(ContextCompat.getColor(context, R.color.white));

                holder.btnQuiz.setOnClickListener(v -> {
                    Intent intent = new Intent(context, StudentLessonQuizActivity.class);
                    intent.putExtra("lesson_id", lesson.getId());
                    intent.putExtra("course_id", lesson.getCourseId());
                    if (nextLessonId != null) intent.putExtra("next_lesson_id", nextLessonId);
                    context.startActivity(intent);
                });

            } else {
                // READY: white background, outline + colored text
                holder.btnQuiz.setAlpha(1f);
                holder.btnQuiz.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
                holder.btnQuiz.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                holder.btnQuiz.setStrokeWidth(strokeWidthPx);
                holder.btnQuiz.setStrokeColor(strokeColor);

                holder.btnQuiz.setOnClickListener(v -> {
                    Intent intent = new Intent(context, StudentLessonQuizActivity.class);
                    intent.putExtra("lesson_id", lesson.getId());
                    intent.putExtra("course_id", lesson.getCourseId());
                    if (nextLessonId != null) intent.putExtra("next_lesson_id", nextLessonId);
                    context.startActivity(intent);
                });
            }
        } else {
            holder.btnQuiz.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLessonNumber, tvLessonTitle, tvLessonDuration;
        ImageView ivLessonThumbnail;
        ImageView ivPlay;
        ProgressBar progressBar;
        TextView tvLessonProgressPercent;
        MaterialButton btnQuiz;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDuration = itemView.findViewById(R.id.tvLessonDuration);
            ivLessonThumbnail = itemView.findViewById(R.id.ivLessonThumbnail);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            progressBar = itemView.findViewById(R.id.progressLesson);
            tvLessonProgressPercent = itemView.findViewById(R.id.tvLessonProgressPercent);
            btnQuiz = itemView.findViewById(R.id.btnQuiz);
        }
    }
}
