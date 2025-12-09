package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Import activity để compiler resolve được tên lớp
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageCourseDetailActivity;

/**
 * Adapter hiển thị danh sách khóa học trong giỏ hàng của student (admin view)
 */
public class UserStudentInCartCourseAdapter extends RecyclerView.Adapter<UserStudentInCartCourseAdapter.CartCourseViewHolder> {

    private List<Course> courseList = new ArrayList<>();

    public UserStudentInCartCourseAdapter() {
    }

    /**
     * Set danh sách courses trong giỏ hàng
     */
    public void setCourses(List<Course> courses) {
        this.courseList = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_manage_user_student_in_cart_course, parent, false);
        return new CartCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartCourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CartCourseViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView imgCourseAvatar;
        private final android.widget.TextView tvCourseName;
        private final android.widget.TextView tvRating;
        private final android.widget.TextView tvStudentCount;
        private final android.widget.TextView tvPrice;

        public CartCourseViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            imgCourseAvatar = itemView.findViewById(R.id.imgCourseAvatar);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Course course) {
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

            // Open course management detail when clicking the item
            itemView.setOnClickListener(v -> {
                try {
                    Context ctx = itemView.getContext();
                    Intent intent = new Intent(ctx, AdminManageCourseDetailActivity.class);
                    if (course != null) {
                        intent.putExtra("courseId", course.getId());
                        intent.putExtra("courseTitle", course.getTitle());
                    }
                    // Nếu context không phải Activity, thêm flag để tránh crash
                    if (!(ctx instanceof android.app.Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    ctx.startActivity(intent);
                } catch (Exception ignored) {}
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
