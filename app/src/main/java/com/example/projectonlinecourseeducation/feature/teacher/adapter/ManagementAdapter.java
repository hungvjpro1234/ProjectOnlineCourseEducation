// File: ManagementAdapter.java
package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ManagementAdapter extends RecyclerView.Adapter<ManagementAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onCourseClick(Course course);
    }

    public ManagementAdapter(OnCourseActionListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<Course> courseList) {
        this.courses = courseList != null ? new ArrayList<>(courseList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addCourse(Course course) {
        courses.add(0, course);
        notifyItemInserted(0);
    }

    public void removeCourse(int position) {
        if (position >= 0 && position < courses.size()) {
            courses.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update an existing course (by id) or insert at top if not found.
     */
    public void updateOrInsertCourse(Course course) {
        if (course == null) return;
        for (int i = 0; i < courses.size(); i++) {
            if (course.getId() != null && course.getId().equals(courses.get(i).getId())) {
                courses.set(i, course);
                notifyItemChanged(i);
                return;
            }
        }
        // not found -> add to top
        addCourse(course);
    }

    public void removeCourseById(String courseId) {
        if (courseId == null) return;
        for (int i = 0; i < courses.size(); i++) {
            if (courseId.equals(courses.get(i).getId())) {
                courses.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgCourseThumbnail;
        private TextView tvCourseTitle;
        private TextView tvCategory;
        private TextView tvStudentCount;
        private TextView tvRating;
        private TextView tvLectureCount;
        private TextView tvPrice;

        public CourseViewHolder(View itemView) {
            super(itemView);

            imgCourseThumbnail = itemView.findViewById(R.id.imgCourseThumbnail);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvLectureCount = itemView.findViewById(R.id.tvLectureCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            // item click -> mở detail
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onCourseClick(courses.get(pos));
                }
            });
        }

        public void bind(Course course) {
            tvCourseTitle.setText(course.getTitle());
            tvCategory.setText(course.getCategory());
            tvStudentCount.setText(formatNumber(course.getStudents()));
            tvRating.setText(String.valueOf(course.getRating()));
            tvLectureCount.setText(String.valueOf(course.getLectures()));
            tvPrice.setText(formatPrice(course.getPrice()));

            // Load image via ImageLoader (project's minimal image loader)
            // NOTE: replace R.drawable.ic_image_placeholder with your actual placeholder drawable if available
            ImageLoader.getInstance().display(course.getImageUrl(), imgCourseThumbnail, R.drawable.ic_image_placeholder);
        }

        private String formatNumber(int number) {
            if (number >= 1000) {
                return (number / 1000) + "." + ((number % 1000) / 100) + "K";
            }
            return String.valueOf(number);
        }

        private String formatPrice(double price) {
            // format price with thousand separator and no decimal
            DecimalFormat df = new DecimalFormat("###,###");
            return df.format(Math.round(price)) + " VNĐ";
        }
    }
}