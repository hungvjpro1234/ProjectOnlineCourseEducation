package com.example.projectonlinecourseeducation.feature.student.adapter;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách khóa học trong My Course của student.
 */
public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.MyCourseViewHolder> {

    public interface MyCourseActionListener {
        void onItemClicked(Course course);
        void onLearnClicked(Course course);
    }

    private final List<Course> data = new ArrayList<>();
    private MyCourseActionListener listener;

    public void submitList(List<Course> courses) {
        data.clear();
        if (courses != null) {
            data.addAll(courses);
        }
        notifyDataSetChanged();
    }

    public void setMyCourseActionListener(MyCourseActionListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public MyCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_my_course, parent, false);
        return new MyCourseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCourseViewHolder holder, int position) {
        Course c = data.get(position);
        holder.bind(c, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyCourseViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgCourseThumb;
        private final TextView tvCourseTitle;
        private final TextView tvCourseTeacher;
        private final TextView tvCourseInfo;
        private final TextView tvStatusPurchased;
        private final Button btnLearnNow;

        public MyCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourseThumb = itemView.findViewById(R.id.imgCourseThumb);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseTeacher = itemView.findViewById(R.id.tvCourseTeacher);
            tvCourseInfo = itemView.findViewById(R.id.tvCourseInfo);
            tvStatusPurchased = itemView.findViewById(R.id.tvStatusPurchased);
            btnLearnNow = itemView.findViewById(R.id.btnLearnNow);
        }

        public void bind(Course course, MyCourseActionListener listener) {
            if (course == null) return;

            ImageLoader.getInstance().display(
                    course.getImageUrl(),
                    imgCourseThumb,
                    R.drawable.ic_image_placeholder
            );

            tvCourseTitle.setText(course.getTitle());
            tvCourseTeacher.setText("GV: " + course.getTeacher());

            String info = course.getLectures() + " bài";
            if (course.getTotalDurationMinutes() > 0) {
                int minutes = course.getTotalDurationMinutes();
                if (minutes >= 60) {
                    int h = minutes / 60;
                    int m = minutes % 60;
                    info += " • " + h + " giờ" + (m > 0 ? " " + m + " phút" : "");
                } else {
                    info += " • " + minutes + " phút";
                }
            }
            tvCourseInfo.setText(info);

            // Badge "ĐÃ MUA" luôn hiển thị trong My Course
            tvStatusPurchased.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClicked(course);
            });

            btnLearnNow.setOnClickListener(v -> {
                if (listener != null) listener.onLearnClicked(course);
            });
        }
    }
}
