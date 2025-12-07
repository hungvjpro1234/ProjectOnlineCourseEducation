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
 * Adapter đơn giản cho danh sách chi tiết tiến độ bài học (nested RecyclerView).
 * Sử dụng DTO ManagementCourseStudentAdapter.LessonProgressDetail
 */
public class ManagementLessonProgressDetailAdapter extends RecyclerView.Adapter<ManagementLessonProgressDetailAdapter.VH> {

    // Use a local DTO to avoid tight coupling
    private List<ManagementCourseStudentAdapter.LessonProgressDetail> items;

    public ManagementLessonProgressDetailAdapter(List<ManagementCourseStudentAdapter.LessonProgressDetail> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void updateItems(List<ManagementCourseStudentAdapter.LessonProgressDetail> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_lesson_progress_detail, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (items == null || position < 0 || position >= items.size()) return;
        ManagementCourseStudentAdapter.LessonProgressDetail d = items.get(position);
        holder.tvTitle.setText(d.getOrder() + ". " + safe(d.getLessonTitle()));
        int p = d.getProgressPercentage();
        try { holder.progressBar.setMax(100); } catch (Exception ignored) {}
        holder.progressBar.setProgress(p);
        holder.tvPercent.setText(p + "%");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ProgressBar progressBar;
        TextView tvPercent;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLessonProgressTitle);
            progressBar = itemView.findViewById(R.id.pbLessonProgress);
            tvPercent = itemView.findViewById(R.id.tvLessonProgressPercent);
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
