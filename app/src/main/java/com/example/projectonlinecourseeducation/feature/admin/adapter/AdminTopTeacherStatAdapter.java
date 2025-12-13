package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách Top giảng viên (thống kê)
 */
public class AdminTopTeacherStatAdapter extends RecyclerView.Adapter<AdminTopTeacherStatAdapter.ViewHolder> {

    public static class TeacherStats {
        public String teacherName;
        public int courseCount;
        public double totalRevenue;
        public int totalStudents;

        public TeacherStats(String teacherName) {
            this.teacherName = teacherName;
            this.courseCount = 0;
            this.totalRevenue = 0;
            this.totalStudents = 0;
        }
    }

    private List<TeacherStats> teachers = new ArrayList<>();

    public void setTeachers(List<TeacherStats> teachers) {
        this.teachers = teachers != null ? teachers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_top_teacher_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherStats stats = teachers.get(position);
        holder.bind(stats, position + 1);
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTeacherName, tvCourseCount, tvRevenue;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvCourseCount = itemView.findViewById(R.id.tvCourseCount);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
        }

        void bind(TeacherStats stats, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvTeacherName.setText(stats.teacherName);
            tvCourseCount.setText(stats.courseCount + " khóa học");

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvRevenue.setText("• " + formatter.format(stats.totalRevenue / 1000000) + "M VNĐ");

            // Change rank badge color for top 3
            if (rank == 1) {
                tvRank.setBackgroundResource(R.drawable.bg_circle_gold);
            } else if (rank == 2) {
                tvRank.setBackgroundResource(R.drawable.bg_circle_silver);
            } else if (rank == 3) {
                tvRank.setBackgroundResource(R.drawable.bg_circle_bronze);
            } else {
                tvRank.setBackgroundResource(R.drawable.bg_circle_blue);
            }
        }
    }
}
