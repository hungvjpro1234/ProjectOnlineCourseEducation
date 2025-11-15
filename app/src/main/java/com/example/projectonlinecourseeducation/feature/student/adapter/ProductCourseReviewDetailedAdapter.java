package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.CourseReview;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductCourseReviewDetailedAdapter extends RecyclerView.Adapter<ProductCourseReviewDetailedAdapter.VH> {

    private final List<CourseReview> data = new ArrayList<>();

    public void submitList(List<CourseReview> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_product_course_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CourseReview r = data.get(position);
        holder.tvStudentName.setText(r.getStudentName());
        holder.ratingBar.setRating(r.getRating());
        holder.tvRatingValue.setText(
                String.format(Locale.US, "%.1f", r.getRating())
        );
        holder.tvComment.setText(r.getComment());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvRatingValue, tvComment;
        RatingBar ratingBar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
            tvRatingValue = itemView.findViewById(R.id.tvReviewRatingValue);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
        }
    }
}
