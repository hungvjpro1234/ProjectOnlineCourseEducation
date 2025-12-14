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
 * Admin Lesson Adapter - Giao diện đẹp hơn với CardView và màu sắc phân biệt
 * + Long-click để xóa lesson
 */
public class AdminCourseLessonAdapter extends RecyclerView.Adapter<AdminCourseLessonAdapter.LessonViewHolder> {

    private List<LessonItem> lessons = new ArrayList<>();
    private OnLessonClickListener clickListener;
    private OnLessonLongClickListener longClickListener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public interface OnLessonLongClickListener {
        boolean onLessonLongClick(Lesson lesson);
    }

    public AdminCourseLessonAdapter(OnLessonClickListener clickListener,
                                    OnLessonLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // Backward compatibility constructor
    public AdminCourseLessonAdapter(OnLessonClickListener clickListener) {
        this(clickListener, null);
    }

    public void setLessons(List<Lesson> lessonList) {
        this.lessons.clear();
        if (lessonList != null) {
            for (int i = 0; i < lessonList.size(); i++) {
                this.lessons.add(new LessonItem(lessonList.get(i), i + 1));
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        LessonItem item = lessons.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardLesson;
        private final TextView tvLessonNumber;
        private final TextView tvLessonTitle;
        private final TextView tvDuration;
        private final TextView tvLessonType;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLesson = itemView.findViewById(R.id.cardLesson);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvLessonType = itemView.findViewById(R.id.tvLessonType);

            // Click listener
            cardLesson.setOnClickListener(v -> {
                if (clickListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    Lesson lesson = lessons.get(getBindingAdapterPosition()).lesson;
                    clickListener.onLessonClick(lesson);
                }
            });

            // Long-click listener
            cardLesson.setOnLongClickListener(v -> {
                if (longClickListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    Lesson lesson = lessons.get(getBindingAdapterPosition()).lesson;
                    return longClickListener.onLessonLongClick(lesson);
                }
                return false;
            });
        }

        public void bind(LessonItem item) {
            Lesson lesson = item.lesson;
            int lessonOrder = item.order;

            tvLessonNumber.setText(String.valueOf(lessonOrder));
            tvLessonTitle.setText(safe(lesson.getTitle()));
            tvDuration.setText(safe(lesson.getDuration()));

            // Hiển thị loại bài học (Video/Reading)
            String type = lesson.getVideoUrl() != null && !lesson.getVideoUrl().isEmpty() ? "Video" : "Đọc";
            tvLessonType.setText(type);
        }

        private String safe(String s) {
            return s == null || s.isEmpty() ? "-" : s;
        }
    }

    public static class LessonItem {
        public Lesson lesson;
        public int order;

        public LessonItem(Lesson lesson, int order) {
            this.lesson = lesson;
            this.order = order;
        }
    }
}