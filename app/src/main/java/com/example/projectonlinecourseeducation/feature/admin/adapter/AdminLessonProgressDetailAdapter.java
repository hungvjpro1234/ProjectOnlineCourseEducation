package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Lesson Progress Detail Adapter - Nested RecyclerView cho chi tiết progress
 */
public class AdminLessonProgressDetailAdapter extends RecyclerView.Adapter<AdminLessonProgressDetailAdapter.ProgressViewHolder> {

    private List<AdminCourseStudentAdapter.LessonProgressDetail> items;

    public AdminLessonProgressDetailAdapter(List<AdminCourseStudentAdapter.LessonProgressDetail> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void updateItems(List<AdminCourseStudentAdapter.LessonProgressDetail> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lesson_progress_detail, parent, false);
        return new ProgressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        if (items == null || position < 0 || position >= items.size()) return;
        AdminCourseStudentAdapter.LessonProgressDetail detail = items.get(position);
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvLessonTitle;
        private final ProgressBar progressBar;
        private final TextView tvPercent;
        private final ImageView imgCompleted;

        ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            imgCompleted = itemView.findViewById(R.id.imgCompleted);
        }

        void bind(AdminCourseStudentAdapter.LessonProgressDetail detail) {
            String title = detail.getOrder() + ". " + safe(detail.getLessonTitle());
            tvLessonTitle.setText(title);

            int progress = detail.getProgressPercentage();
            try {
                progressBar.setMax(100);
                progressBar.setProgress(progress);
            } catch (Exception ignored) {}

            tvPercent.setText(progress + "%");

            // Hiển thị icon check nếu completed
            if (detail.isCompleted()) {
                imgCompleted.setVisibility(View.VISIBLE);
                tvPercent.setTextColor(0xFF4CAF50); // Green
            } else {
                imgCompleted.setVisibility(View.GONE);
                tvPercent.setTextColor(0xFF757575); // Gray
            }
        }

        private String safe(String s) {
            return s == null ? "" : s;
        }
    }
}