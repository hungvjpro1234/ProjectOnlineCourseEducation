package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách khóa học sở hữu của teacher (admin view)
 */
public class UserTeacherOwnedCourseAdapter extends RecyclerView.Adapter<UserTeacherOwnedCourseAdapter.OwnedCourseViewHolder> {

    private List<Course> courseList = new ArrayList<>();
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onViewDetailsClick(Course course);
    }

    public UserTeacherOwnedCourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set danh sách courses sở hữu
     */
    public void setCourses(List<Course> courses) {
        this.courseList = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnedCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_manage_user_teacher_owned_course, parent, false);
        return new OwnedCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnedCourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class OwnedCourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCourseAvatar;
        private final TextView tvCourseName;
        private final TextView tvRating;
        private final TextView tvStudentCount;
        private final TextView tvPrice;
        private final Button btnViewDetails;

        public OwnedCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourseAvatar = itemView.findViewById(R.id.imgCourseAvatar);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Course course, OnCourseClickListener listener) {
            if (course == null) return;

            // Course name
            tvCourseName.setText(course.getTitle());

            // Rating
            String ratingText = String.format(Locale.getDefault(), "⭐ %.1f", course.getRating());
            tvRating.setText(ratingText);

            // Student count
            String studentCountText = "👥 " + formatStudentCount(course.getStudents());
            tvStudentCount.setText(studentCountText);

            // Price
            NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvPrice.setText(currencyFormat.format(course.getPrice()) + " VNĐ");

            // Avatar
            if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                ImageLoader.getInstance().display(course.getImageUrl(), imgCourseAvatar, R.drawable.ic_image_placeholder);
            } else {
                imgCourseAvatar.setImageResource(R.drawable.ic_image_placeholder);
            }

            // View details button
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(course);
                }
            });
        }

        private String formatStudentCount(int count) {
            if (count >= 1000) {
                return String.format(Locale.getDefault(), "%.1fK", count / 1000.0);
            }
            return String.valueOf(count);
        }
    }
}
