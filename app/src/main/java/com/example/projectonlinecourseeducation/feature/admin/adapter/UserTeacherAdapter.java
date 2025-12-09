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

/**
 * Adapter hiển thị danh sách giảng viên với real data (expandable)
 */
public class UserTeacherAdapter extends RecyclerView.Adapter<UserTeacherAdapter.TeacherViewHolder> {

    private List<TeacherStats> teacherList = new ArrayList<>();
    private OnTeacherClickListener listener;

    public interface OnTeacherClickListener {
        void onViewDetailsClick(TeacherStats teacherStats);
    }

    public UserTeacherAdapter(OnTeacherClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set danh sách teachers (đã có stats)
     */
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
        TeacherStats stats = teacherList.get(position);
        holder.bind(stats, listener);
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public class TeacherViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout collapsedView;
        private LinearLayout expandedView;
        private ImageView imgExpand;
        private boolean isExpanded = false;

        private TextView tvTeacherName;
        private TextView tvTeacherEmail;
        private TextView tvTotalCourses;
        private TextView tvTotalRevenue;
        private TextView tvAverageRating;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            collapsedView = itemView.findViewById(R.id.collapsedView);
            expandedView = itemView.findViewById(R.id.expandedView);
            imgExpand = itemView.findViewById(R.id.imgExpandTeacher);

            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvTeacherEmail = itemView.findViewById(R.id.tvTeacherEmail);
            tvTotalCourses = itemView.findViewById(R.id.tvTotalCourses);
            tvTotalRevenue = itemView.findViewById(R.id.tvTotalRevenue);
            tvAverageRating = itemView.findViewById(R.id.tvAverageRating);
        }

        public void bind(TeacherStats stats, OnTeacherClickListener listener) {
            if (stats == null || stats.getUser() == null) return;

            // Teacher name and email
            tvTeacherName.setText(stats.getUser().getName());
            tvTeacherEmail.setText(stats.getUser().getEmail());

            // Total courses
            tvTotalCourses.setText(stats.getTotalCourses() + " khóa");

            // Total revenue with currency format
            NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvTotalRevenue.setText(currencyFormat.format(stats.getTotalRevenue()) + " VNĐ");

            // Average rating
            String ratingText = String.format(Locale.getDefault(), "%.1f ⭐", stats.getAverageRating());
            tvAverageRating.setText(ratingText);

            // Toggle expand/collapse
            collapsedView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    expandedView.setVisibility(View.VISIBLE);
                    imgExpand.setRotation(180);
                } else {
                    expandedView.setVisibility(View.GONE);
                    imgExpand.setRotation(0);
                }
            });

            // View details button
            itemView.findViewById(R.id.btnViewDetails).setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(stats);
                }
            });
        }
    }
}
