package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách Top khóa học bán chạy (thống kê)
 */
public class AdminTopCourseStatAdapter extends RecyclerView.Adapter<AdminTopCourseStatAdapter.ViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private OnCourseClickListener clickListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnCourseClickListener(OnCourseClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_top_course_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, position + 1);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvCourseName, tvEnrollments, tvRevenue;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvEnrollments = itemView.findViewById(R.id.tvEnrollments);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onCourseClick(courses.get(pos));
                }
            });
        }

        void bind(Course course, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvCourseName.setText(course.getTitle());
            tvEnrollments.setText(course.getStudents() + " học viên");

            // Revenue = price * students
            double revenue = course.getPrice() * course.getStudents();
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvRevenue.setText("• " + formatter.format(revenue) + " VNĐ");

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
