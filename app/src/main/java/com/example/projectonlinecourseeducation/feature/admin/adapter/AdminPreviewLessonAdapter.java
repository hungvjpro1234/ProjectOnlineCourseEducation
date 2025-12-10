package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách lessons trong preview (admin)
 * Click vào lesson sẽ mở video player
 */
public class AdminPreviewLessonAdapter extends RecyclerView.Adapter<AdminPreviewLessonAdapter.LessonViewHolder> {

    private final List<Lesson> lessons = new ArrayList<>();
    private final OnLessonClickListener onLessonClick;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public AdminPreviewLessonAdapter(OnLessonClickListener onLessonClick) {
        this.onLessonClick = onLessonClick;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons.clear();
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_preview_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson, position + 1);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardLesson;
        private final TextView tvOrder;
        private final TextView tvTitle;
        private final TextView tvDuration;
        private final View playIcon;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            cardLesson = itemView.findViewById(R.id.cardLesson);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            playIcon = itemView.findViewById(R.id.playIcon);
        }

        void bind(Lesson lesson, int displayOrder) {
            tvOrder.setText(String.valueOf(displayOrder));
            tvTitle.setText(lesson.getTitle());
            tvDuration.setText(lesson.getDuration());

            // Click to play video
            cardLesson.setOnClickListener(v -> {
                if (onLessonClick != null) {
                    onLessonClick.onLessonClick(lesson);
                }
            });
        }
    }
}