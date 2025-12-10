package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Admin Course Review Adapter - Thiết kế card đẹp với avatar, rating, và verified badge
 * + Long-click để xóa review
 */
public class AdminCourseReviewAdapter extends RecyclerView.Adapter<AdminCourseReviewAdapter.ReviewViewHolder> {

    private List<CourseReview> reviews = new ArrayList<>();
    private OnReviewClickListener clickListener;
    private OnReviewLongClickListener longClickListener;

    public interface OnReviewClickListener {
        void onReviewClick(CourseReview review);
    }

    public interface OnReviewLongClickListener {
        boolean onReviewLongClick(CourseReview review);
    }

    public AdminCourseReviewAdapter(OnReviewClickListener clickListener,
                                    OnReviewLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // Backward compatibility constructors
    public AdminCourseReviewAdapter(OnReviewClickListener clickListener) {
        this(clickListener, null);
    }

    public AdminCourseReviewAdapter() {
        this(null, null);
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
                .inflate(R.layout.item_admin_course_review, parent, false);
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

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardReview;
        private final ImageView imgAvatar;
        private final TextView tvReviewerName;
        private final TextView tvVerifiedBadge;
        private final RatingBar rbRating;
        private final TextView tvRatingText;
        private final TextView tvReviewDate;
        private final TextView tvReviewComment;
        private final TextView tvHelpfulCount;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReview = itemView.findViewById(R.id.cardReview);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            rbRating = itemView.findViewById(R.id.rbRating);
            tvRatingText = itemView.findViewById(R.id.tvRatingText);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
            tvHelpfulCount = itemView.findViewById(R.id.tvHelpfulCount);

            // Click listener
            if (clickListener != null) {
                cardReview.setOnClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.onReviewClick(reviews.get(pos));
                    }
                });
            }

            // Long-click listener
            if (longClickListener != null) {
                cardReview.setOnLongClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        return longClickListener.onReviewLongClick(reviews.get(pos));
                    }
                    return false;
                });
            }
        }

        public void bind(CourseReview review) {
            if (review == null) return;

            // Reviewer name
            tvReviewerName.setText(safe(review.getStudentName(), "Người dùng"));

            // Verified badge (giả sử user đã mua khóa học thì verified)
            tvVerifiedBadge.setVisibility(View.VISIBLE);

            // Rating
            rbRating.setRating((float) review.getRating());
            tvRatingText.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));

            // Review date
            tvReviewDate.setText(formatDate(review.getCreatedAt()));

            // Comment
            String comment = safe(review.getComment(), "");
            if (!comment.isEmpty()) {
                tvReviewComment.setVisibility(View.VISIBLE);
                tvReviewComment.setText(comment);
            } else {
                tvReviewComment.setVisibility(View.GONE);
            }

            // Helpful count (nếu có trong model)
            // tvHelpfulCount.setText("12 người thấy hữu ích");
            tvHelpfulCount.setVisibility(View.GONE); // Ẩn nếu chưa có feature này

            // Load avatar (nếu có)
            loadAvatar(review);
        }

        private void loadAvatar(CourseReview review) {
            // Giả sử CourseReview có studentAvatarUrl
            // Nếu không có, dùng avatar mặc định
            try {
                // Thử dùng reflection hoặc getter nếu có
                String avatarUrl = null;
                // avatarUrl = review.getStudentAvatarUrl(); // Nếu model có field này

                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    ImageLoader.getInstance().display(avatarUrl, imgAvatar,
                            R.drawable.ic_person_circle, success -> {});
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_person_circle);
                }
            } catch (Exception e) {
                imgAvatar.setImageResource(R.drawable.ic_person_circle);
            }
        }

        private String safe(String s, String fallback) {
            return s == null || s.trim().isEmpty() ? fallback : s;
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