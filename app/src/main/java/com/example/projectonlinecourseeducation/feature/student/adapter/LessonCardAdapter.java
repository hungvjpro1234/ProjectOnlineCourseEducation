package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.projectonlinecourseeducation.feature.student.activity.StudentLessonVideoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách bài học dưới dạng card
 * Mỗi card chứa: thumbnail, tiêu đề, duration, play icon, thanh progress (% hoàn thành)
 *
 * Rule:
 *   - Chỉ cho phép bấm nút Play nếu bài học không bị khóa.
 *   - Các bài sau sẽ bị khóa nếu bài trước chưa completed (>= 90%) - phần này được tính ở Activity.
 *
 * UI chỉ nhận dữ liệu từ StudentCourseLessonActivity thông qua LessonItemUiModel,
 * không quan tâm dữ liệu đó đến từ Fake API hay Backend.
 */
public class LessonCardAdapter extends RecyclerView.Adapter<LessonCardAdapter.VH> {

    /**
     * UI model cho từng item bài học:
     *  - lesson: thông tin bài học
     *  - completionPercentage: 0-100%
     *  - locked: true nếu không được phép học (bài trước chưa đạt 90%)
     */
    public static class LessonItemUiModel {
        public final Lesson lesson;
        public final int completionPercentage;
        public final boolean locked;

        public LessonItemUiModel(Lesson lesson, int completionPercentage, boolean locked) {
            this.lesson = lesson;
            this.completionPercentage = completionPercentage;
            this.locked = locked;
        }
    }

    private final List<LessonItemUiModel> data = new ArrayList<>();
    private final Context context;

    public LessonCardAdapter(Context context) {
        this.context = context;
    }

    /**
     * Nhận list LessonItemUiModel (Lesson + progress + trạng thái khóa)
     * (được build ở StudentCourseLessonActivity)
     */
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

        // Lesson number
        holder.tvLessonNumber.setText("Bài " + (position + 1));

        // Lesson title
        holder.tvLessonTitle.setText(lesson.getTitle());

        // Lesson duration (nếu có)
        String duration = lesson.getDuration();
        holder.tvLessonDuration.setText(duration != null && !duration.isEmpty() ? duration : "00:00");

        // Placeholder màu theo vị trí
        int[] placeholderDrawables = {
                R.drawable.ic_lesson_placeholder_purple,
                R.drawable.ic_lesson_placeholder_blue,
                R.drawable.ic_lesson_placeholder_cyan
        };
        int placeholderIndex = position % placeholderDrawables.length;
        holder.ivLessonThumbnail.setImageResource(placeholderDrawables[placeholderIndex]);

        // ====== Bind progress bar + % ======
        int percent = Math.max(0, Math.min(100, item.completionPercentage));
        holder.progressBar.setProgress(percent);
        holder.tvLessonProgressPercent.setText(percent + "%");

        // ====== Trạng thái khóa/mở bài học ======
        if (item.locked) {
            // Bị khóa: mờ card + icon play xám, chỉ cho bấm để báo message
            holder.itemView.setAlpha(0.6f);
            holder.ivPlay.setColorFilter(
                    ContextCompat.getColor(context, R.color.divider)
            );
            holder.ivPlay.setOnClickListener(v -> {
                Toast.makeText(context,
                        "Hãy hoàn thành bài học trước đó (>= 90%) để mở bài này",
                        Toast.LENGTH_SHORT
                ).show();
            });
            // Không cho click cả item
            holder.itemView.setOnClickListener(null);
        } else {
            // Mở: card bình thường, icon play theo màu secondary
            holder.itemView.setAlpha(1f);
            holder.ivPlay.setColorFilter(
                    ContextCompat.getColor(context, R.color.colorSecondary)
            );

            // Chỉ bắt click trên nút play, bỏ click toàn bộ item (theo yêu cầu)
            View.OnClickListener playClickListener = v -> {
                Intent intent = new Intent(context, StudentLessonVideoActivity.class);
                intent.putExtra("lesson_id", lesson.getId());
                intent.putExtra("lesson_title", lesson.getTitle());
                intent.putExtra("course_id", lesson.getCourseId());
                context.startActivity(intent);
            };
            holder.ivPlay.setOnClickListener(playClickListener);
            holder.itemView.setOnClickListener(null);
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

        VH(@NonNull View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDuration = itemView.findViewById(R.id.tvLessonDuration);
            ivLessonThumbnail = itemView.findViewById(R.id.ivLessonThumbnail);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            progressBar = itemView.findViewById(R.id.progressLesson);
            tvLessonProgressPercent = itemView.findViewById(R.id.tvLessonProgressPercent);
        }
    }
}
