package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonProgressApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách khóa học trong My Course của student.
 * - Tính tiến độ khóa học trên item bằng cách đọc Lesson + LessonProgress từ LessonApi & LessonProgressApi.
 * - Đăng ký LessonProgressUpdateListener để refresh khi tiến độ thay đổi.
 *
 * Note: adapter có phương thức dispose() để hủy listener (gọi từ Fragment.onDestroyView()).
 */
public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.MyCourseViewHolder> {

    public interface MyCourseActionListener {
        void onItemClicked(Course course);
        void onLearnClicked(Course course);
    }

    private final List<Course> data = new ArrayList<>();
    private MyCourseActionListener listener;

    // APIs
    private final LessonApi lessonApi;
    private final LessonProgressApi lessonProgressApi;

    // Listener để refresh khi có update progress
    private final LessonProgressApi.LessonProgressUpdateListener progressListener;

    public MyCourseAdapter() {
        this.lessonApi = ApiProvider.getLessonApi();
        this.lessonProgressApi = ApiProvider.getLessonProgressApi();

        // Khi có cập nhật tiến độ của 1 lesson, refresh adapter để cập nhật progress item tương ứng.
        this.progressListener = new LessonProgressApi.LessonProgressUpdateListener() {
            @Override
            public void onLessonProgressChanged(String lessonId) {
                // Simple approach: refresh whole list (OK cho số lượng item vừa phải)
                // If you want more efficient update, map lessonId -> courseId then notifyItemChanged for specific items.
                MyCourseAdapter.this.notifyDataSetChanged();
            }
        };

        // register listener (will be removed by dispose())
        if (this.lessonProgressApi != null) {
            this.lessonProgressApi.addLessonProgressUpdateListener(progressListener);
        }
    }

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

    /**
     * Hủy đăng ký listener khi fragment/activity bị destroy để tránh leak.
     * Gọi adapter.dispose() từ fragment.onDestroyView().
     */
    public void dispose() {
        if (this.lessonProgressApi != null) {
            this.lessonProgressApi.removeLessonProgressUpdateListener(progressListener);
        }
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

        // NEW: progress views
        private final ProgressBar progressCourseItemBar;
        private final TextView tvCourseItemProgressPercent;
        private final TextView tvCourseItemCompleted;

        public MyCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourseThumb = itemView.findViewById(R.id.imgCourseThumb);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseTeacher = itemView.findViewById(R.id.tvCourseTeacher);
            tvCourseInfo = itemView.findViewById(R.id.tvCourseInfo);
            tvStatusPurchased = itemView.findViewById(R.id.tvStatusPurchased);
            btnLearnNow = itemView.findViewById(R.id.btnLearnNow);

            progressCourseItemBar = itemView.findViewById(R.id.progressCourseItemBar);
            tvCourseItemProgressPercent = itemView.findViewById(R.id.tvCourseItemProgressPercent);
            tvCourseItemCompleted = itemView.findViewById(R.id.tvCourseItemCompleted);
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

            // --- Calculate course-level progress for this item ---
            calculateAndBindCourseProgress(course);
        }

        private void calculateAndBindCourseProgress(Course course) {
            // Use ApiProvider to access LessonApi & LessonProgressApi (same approach as Activity)
            LessonApi lessonApi = ApiProvider.getLessonApi();
            LessonProgressApi progressApi = ApiProvider.getLessonProgressApi();

            List<Lesson> lessons = null;
            if (lessonApi != null) {
                lessons = lessonApi.getLessonsForCourse(course.getId());
            }

            if (lessons == null || lessons.isEmpty()) {
                progressCourseItemBar.setProgress(0);
                tvCourseItemProgressPercent.setText("0%");
                tvCourseItemCompleted.setText("0 / 0 bài hoàn thành");
                return;
            }

            // check if all lessons have totalSecond
            boolean allHaveDuration = true;
            for (Lesson l : lessons) {
                LessonProgress p = progressApi != null ? progressApi.getLessonProgress(l.getId()) : null;
                if (p == null || p.getTotalSecond() <= 0f) {
                    allHaveDuration = false;
                    break;
                }
            }

            int percent = 0;
            int completedCount = 0;

            if (allHaveDuration) {
                double totalSecondsSum = 0.0;
                double watchedSecondsSum = 0.0;
                for (Lesson l : lessons) {
                    LessonProgress p = progressApi != null ? progressApi.getLessonProgress(l.getId()) : null;
                    if (p != null) {
                        double t = p.getTotalSecond();
                        double c = Math.min(p.getCurrentSecond(), t);
                        totalSecondsSum += t;
                        watchedSecondsSum += c;
                        if (p.isCompleted()) completedCount++;
                    } else {
                        // if missing progress treat as zero
                    }
                }
                if (totalSecondsSum > 0) {
                    percent = (int) Math.round((watchedSecondsSum / totalSecondsSum) * 100.0);
                } else {
                    percent = 0;
                }
            } else {
                int sumPerc = 0;
                int count = 0;
                for (Lesson l : lessons) {
                    LessonProgress p = progressApi != null ? progressApi.getLessonProgress(l.getId()) : null;
                    int cp = 0;
                    if (p != null) {
                        cp = p.getCompletionPercentage();
                        if (p.isCompleted()) completedCount++;
                    }
                    sumPerc += cp;
                    count++;
                }
                if (count > 0) {
                    percent = Math.round((float) sumPerc / (float) count);
                } else {
                    percent = 0;
                }
            }

            percent = Math.max(0, Math.min(100, percent));

            progressCourseItemBar.setProgress(percent);
            tvCourseItemProgressPercent.setText(percent + "%");
            tvCourseItemCompleted.setText(completedCount + " / " + lessons.size() + " bài hoàn thành");
        }
    }
}
