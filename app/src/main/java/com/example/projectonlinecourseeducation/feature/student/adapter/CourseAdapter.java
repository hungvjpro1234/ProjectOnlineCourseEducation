// app/src/main/java/com/example/projectonlinecourseeducation/feature/student/adapter/CourseAdapter.java
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
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.core.model.Course;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {

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

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Course c = data.get(pos);

        ImageLoader.getInstance().display(c.getImageUrl(), h.img, R.drawable.ic_image_placeholder);

        h.tvTitle.setText(c.getTitle());
        h.tvTeacher.setText("GV: " + c.getTeacher());
        h.tvLectures.setText("Bài giảng: " + c.getLectures());
        h.tvStudents.setText("Học viên: " + c.getStudents());

        float rating = (float) c.getRating();
        h.ratingBar.setRating(rating);
        h.tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));

        h.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(c.getPrice()));

        // >>> Click item
        h.itemView.setOnClickListener(view -> {
            if (clickListener != null) clickListener.onCourseClick(c);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvTeacher, tvLectures, tvStudents, tvPrice, tvRatingValue;
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
        }
    }
}
