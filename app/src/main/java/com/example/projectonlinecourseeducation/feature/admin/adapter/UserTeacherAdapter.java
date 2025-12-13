package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.admin.model.TeacherStats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserTeacherAdapter extends RecyclerView.Adapter<UserTeacherAdapter.TeacherViewHolder> {

    private List<TeacherStats> teacherList = new ArrayList<>();
    private OnTeacherClickListener listener;

    public interface OnTeacherClickListener {
        void onViewDetailsClick(TeacherStats teacherStats);
    }

    public UserTeacherAdapter(OnTeacherClickListener listener) {
        this.listener = listener;
    }

    public void setTeachers(List<TeacherStats> teachers) {
        this.teacherList = teachers != null ? teachers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_manage_user_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        holder.bind(teacherList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {

        LinearLayout collapsedView, expandedView;
        ImageView imgExpand;
        TextView tvTeacherName, tvTeacherEmail, tvTotalCourses, tvTotalRevenue;
        boolean isExpanded = false;

        TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            collapsedView = itemView.findViewById(R.id.collapsedView);
            expandedView = itemView.findViewById(R.id.expandedView);
            imgExpand = itemView.findViewById(R.id.imgExpandTeacher);

            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvTeacherEmail = itemView.findViewById(R.id.tvTeacherEmail);
            tvTotalCourses = itemView.findViewById(R.id.tvTotalCourses);
            tvTotalRevenue = itemView.findViewById(R.id.tvTotalRevenue);
        }

        void bind(TeacherStats stats, OnTeacherClickListener listener) {
            tvTeacherName.setText(stats.getUser().getName());
            tvTeacherEmail.setText(stats.getUser().getEmail());
            tvTotalCourses.setText(stats.getTotalCourses() + " khóa");

            NumberFormat format = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvTotalRevenue.setText(format.format(stats.getTotalRevenue()) + " VNĐ");

            collapsedView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                expandedView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                imgExpand.setRotation(isExpanded ? 180 : 0);
            });

            itemView.findViewById(R.id.btnViewDetails).setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailsClick(stats);
            });
        }
    }
}
