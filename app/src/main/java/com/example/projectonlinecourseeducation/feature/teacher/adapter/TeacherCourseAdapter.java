// app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/adapter/TeacherCourseAdapter.java
package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
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
 * Adapter danh sách khóa học cho màn Teacher.
 * - An toàn với null (không crash khi thiếu view).
 * - Có log debug để phát hiện nhanh id layout bị thiếu/không khớp.
 */
public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.VH> {

    private static final String TAG = "TeacherCourseAdapter";

    public interface OnCourseActionListener {
        void onEditCourse(Course course);
        void onDeleteCourse(Course course);
    }

    private OnCourseActionListener actionListener;
    private final List<Course> data = new ArrayList<>();

    public TeacherCourseAdapter() {}

    public TeacherCourseAdapter(List<Course> initial, OnCourseActionListener listener) {
        if (initial != null) data.addAll(initial);
        this.actionListener = listener;
    }

    public void setOnCourseActionListener(OnCourseActionListener l) {
        this.actionListener = l;
    }

    public void submitList(List<Course> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Quan trọng: inflate với parent, false
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_course_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        if (pos < 0 || pos >= data.size()) return;

        Course c = data.get(pos);
        if (c == null) return;

        // Image (safe)
        if (h.img != null) {
            ImageLoader.getInstance().display(c.getImageUrl(), h.img, R.drawable.ic_image_placeholder);
        } else {
            Log.w(TAG, "onBind: img == null at pos " + pos);
        }

        // Title
        if (h.tvTitle != null) {
            h.tvTitle.setText(c.getTitle() != null ? c.getTitle() : "Không tên");
        } else {
            Log.e(TAG, "onBind: tvTitle == null at pos " + pos + " (check item_teacher_course_card.xml)");
        }

        // Category
        if (h.tvCategory != null) {
            h.tvCategory.setText(c.getCategory() != null ? c.getCategory() : "");
        }

        // Lectures / Students
        if (h.tvLectures != null) {
            h.tvLectures.setText("📚 " + c.getLectures() + " bài");
        }
        if (h.tvStudents != null) {
            h.tvStudents.setText("👥 " + c.getStudents() + " học viên");
        }

        // Rating
        float rating = (float) c.getRating();
        if (h.ratingBar != null) {
            h.ratingBar.setRating(rating);
        }
        if (h.tvRatingValue != null) {
            h.tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        }

        // Price
        if (h.tvPrice != null) {
            try {
                h.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(c.getPrice()));
            } catch (Exception e) {
                Log.w(TAG, "onBind: formatting price failed", e);
                h.tvPrice.setText(String.valueOf(c.getPrice()));
            }
        }

        // Buttons
        if (h.btnEdit != null) {
            h.btnEdit.setOnClickListener(view -> {
                if (actionListener != null) actionListener.onEditCourse(c);
            });
        }
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(view -> {
                if (actionListener != null) actionListener.onDeleteCourse(c);
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvCategory, tvLectures, tvStudents, tvPrice, tvRatingValue;
        RatingBar ratingBar;
        Button btnEdit, btnDelete;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgCourse);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvLectures = v.findViewById(R.id.tvLectures);
            tvStudents = v.findViewById(R.id.tvStudents);
            tvPrice = v.findViewById(R.id.tvPrice);
            ratingBar = v.findViewById(R.id.ratingBar);
            tvRatingValue = v.findViewById(R.id.tvRatingValue);
            btnEdit = v.findViewById(R.id.btnEditCourse);
            btnDelete = v.findViewById(R.id.btnDeleteCourse);

            // Debug logs để nhanh chóng nhận biết view nào bị null
            if (tvTitle == null) Log.e(TAG, "VH ctor: tvTitle == null (R.id.tvTitle)");
            if (img == null) Log.w(TAG, "VH ctor: imgCourse == null (R.id.imgCourse)");
            if (tvCategory == null) Log.w(TAG, "VH ctor: tvCategory == null (R.id.tvCategory)");
            if (tvPrice == null) Log.w(TAG, "VH ctor: tvPrice == null (R.id.tvPrice)");
            if (ratingBar == null) Log.w(TAG, "VH ctor: ratingBar == null (R.id.ratingBar)");
            // bạn có thể thêm log cho view khác nếu cần
        }
    }
}
