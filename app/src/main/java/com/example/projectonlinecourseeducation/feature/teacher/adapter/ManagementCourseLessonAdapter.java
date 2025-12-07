package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter rút gọn: không có expand/collapse, không có nút edit/delete.
 * Bắt sự kiện click trên chính item để chuyển Activity (ví dụ: edit).
 */
public class ManagementCourseLessonAdapter extends RecyclerView.Adapter<ManagementCourseLessonAdapter.LessonViewHolder> {

    private List<LessonItem> lessons = new ArrayList<>();
    private OnLessonActionListener listener;

    public interface OnLessonActionListener {
        // Called when the whole item is clicked (use to open edit activity)
        void onLessonClick(Lesson lesson);
    }

    public ManagementCourseLessonAdapter(OnLessonActionListener listener) {
        this.listener = listener;
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

    @Override
    public LessonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_course_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LessonViewHolder holder, int position) {
        LessonItem item = lessons.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLessonNumber;
        private TextView tvLessonTitle;
        private TextView tvDuration;

        public LessonViewHolder(View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);

            // Bắt sự kiện click cho toàn item (dùng để mở activity chỉnh sửa / chi tiết)
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Lesson lesson = lessons.get(getAdapterPosition()).lesson;
                    listener.onLessonClick(lesson);
                }
            });
        }

        public void bind(LessonItem item) {
            Lesson lesson = item.lesson;
            int lessonOrder = item.order;

            tvLessonNumber.setText(String.valueOf(lessonOrder));
            tvLessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "");
            tvDuration.setText("⏱ " + (lesson.getDuration() != null ? lesson.getDuration() : ""));
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
