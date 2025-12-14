package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách chi tiết tiến độ từng bài học (nested recycler)
 * Hiển thị:
 * - title
 * - progress bar + percent
 * - quiz score (ví dụ 8/10 hoặc —/10 nếu chưa làm)
 */
public class ManagementLessonProgressDetailAdapter extends RecyclerView.Adapter<ManagementLessonProgressDetailAdapter.Holder> {

    private List<ManagementCourseStudentAdapter.LessonProgressDetail> items = new ArrayList<>();

    public ManagementLessonProgressDetailAdapter(List<ManagementCourseStudentAdapter.LessonProgressDetail> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void updateItems(List<ManagementCourseStudentAdapter.LessonProgressDetail> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_lesson_progress_detail, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position < 0 || position >= items.size()) return;
        ManagementCourseStudentAdapter.LessonProgressDetail d = items.get(position);
        holder.bind(d);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final ProgressBar pb;
        private final TextView tvPercent;
        private final TextView tvQuizScore;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLessonProgressTitle);
            pb = itemView.findViewById(R.id.pbLessonProgress);
            tvPercent = itemView.findViewById(R.id.tvLessonProgressPercent);
            tvQuizScore = itemView.findViewById(R.id.tvLessonQuizScore);

            try { pb.setMax(100); } catch (Exception ignored) {}
        }

        public void bind(ManagementCourseStudentAdapter.LessonProgressDetail d) {
            if (d == null) {
                tvTitle.setText("Bài ẩn danh");
                tvPercent.setText("0%");
                pb.setProgress(0);
                tvQuizScore.setText("—/10");
                return;
            }

            tvTitle.setText(d.getLessonTitle() != null ? d.getLessonTitle() : "Bài " + d.getOrder());
            int progress = d.getProgressPercentage();
            tvPercent.setText(progress + "%");
            try { pb.setProgress(progress); } catch (Exception ignored) {}

            tvQuizScore.setText(d.getQuizScoreDisplay());
        }
    }
}
