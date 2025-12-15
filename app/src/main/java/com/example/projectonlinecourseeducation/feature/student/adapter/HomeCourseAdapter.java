package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.course.CourseStatus;
import com.example.projectonlinecourseeducation.core.utils.CourseStatusResolver;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeCourseAdapter extends RecyclerView.Adapter<HomeCourseAdapter.VH> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private OnCourseClickListener clickListener;
    public void setOnCourseClickListener(OnCourseClickListener l) { this.clickListener = l; }

    private final List<Course> data = new ArrayList<>();

    public void submitList(List<Course> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Update a single course in adapter by id (replace and notify the item).
     * Useful when Course updated via listener.
     */
    public void updateCourseInList(Course updated) {
        if (updated == null) return;
        for (int i = 0; i < data.size(); i++) {
            Course c = data.get(i);
            if (c != null && updated.getId() != null && updated.getId().equals(c.getId())) {
                // Replace reference so onBind will show new values
                data.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_home_course_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Course c = data.get(pos);
        if (c == null) return;

        // ===== Bind static data =====
        ImageLoader.getInstance().display(
                c.getImageUrl(), h.img, R.drawable.ic_image_placeholder
        );

        h.tvTitle.setText(c.getTitle());
        h.tvTeacher.setText("Giảng viên: " + c.getTeacher());
        h.tvLectures.setText("📚 " + c.getLectures() + " bài");
        h.tvStudents.setText("👥 " + c.getStudents() + " học viên");

        float rating = (float) c.getRating();
        h.ratingBar.setRating(rating);
        h.tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));

        // ===== RESET state (RẤT QUAN TRỌNG) =====
        h.tvPurchasedBadge.setVisibility(View.GONE);
        h.tvPrice.setVisibility(View.VISIBLE);
        h.tvPrice.setText(
                NumberFormat.getCurrencyInstance(
                        new Locale("vi", "VN")
                ).format(c.getPrice())
        );

        // Lưu courseId hiện tại của ViewHolder
        final String bindCourseId = c.getId();

        // ===== ASYNC resolve status =====
        CourseStatusResolver.resolveStatus(bindCourseId, status -> {

            // ⚠️ Chống bind nhầm ViewHolder
            if (!bindCourseId.equals(c.getId())) return;

            if (status == CourseStatus.PURCHASED) {
                h.tvPurchasedBadge.setVisibility(View.VISIBLE);
                h.tvPrice.setVisibility(View.GONE);
            } else {
                h.tvPurchasedBadge.setVisibility(View.GONE);
                h.tvPrice.setVisibility(View.VISIBLE);
            }
        });

        // ===== Click =====
        h.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onCourseClick(c);
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvTeacher, tvLectures, tvStudents, tvPrice, tvRatingValue;
        TextView tvPurchasedBadge;
        RatingBar ratingBar;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgCourse);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTeacher = v.findViewById(R.id.tvTeacher);
            tvLectures = v.findViewById(R.id.tvLectures);
            tvStudents = v.findViewById(R.id.tvStudents);
            tvPrice = v.findViewById(R.id.tvPrice);
            ratingBar = v.findViewById(R.id.ratingBar);
            tvRatingValue = v.findViewById(R.id.tvRatingValue);
            tvPurchasedBadge = v.findViewById(R.id.tvPurchasedBadge);
        }
    }
}
