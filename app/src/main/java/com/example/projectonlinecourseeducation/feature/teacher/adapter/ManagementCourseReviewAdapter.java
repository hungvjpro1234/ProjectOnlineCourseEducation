package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter đơn giản hiển thị danh sách review (chỉ đọc).
 * Loại bỏ expand/collapse, avatar, nút more và mọi phần trả lời.
 */
public class ManagementCourseReviewAdapter extends RecyclerView.Adapter<ManagementCourseReviewAdapter.ReviewViewHolder> {

    private List<CourseReview> reviews = new ArrayList<>();

    public ManagementCourseReviewAdapter() {
    }

    public void setReviews(List<CourseReview> reviewList) {
        this.reviews.clear();
        if (reviewList != null) {
            this.reviews.addAll(reviewList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_course_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        CourseReview review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView tvReviewerName;
        private RatingBar rbRating;
        private TextView tvReviewDate;
        private TextView tvReviewComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            rbRating = itemView.findViewById(R.id.rbRating);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
        }

        public void bind(CourseReview review) {
            tvReviewerName.setText(safeString(review.getStudentName(), "Người dùng"));
            rbRating.setRating((float) review.getRating());
            tvReviewDate.setText(formatDate(review.getCreatedAt()));
            tvReviewComment.setText(safeString(review.getComment(), ""));
        }

        private String safeString(String s, String fallback) {
            return s != null ? s : fallback;
        }

        private String formatDate(long timestamp) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } catch (Exception e) {
                return "";
            }
        }
    }
}
